import java.util.concurrent.Callable;
import java.util.function.Function;

public class Ejercicio1 {

    sealed interface Result<T> permits Result.Success, Result.Failure {

        record Success<T>(T value) implements Result<T> {}
        record Failure<T>(String error) implements Result<T> {}

        default boolean isSuccess() {
            return this instanceof Success<T>;
        }

        default T getOrElse(T defaultValue) {
            return switch (this) {
                case Success<T> s -> s.value();
                case Failure<T> f -> defaultValue;
            };
        }

        default <U> Result<U> map(Function<T, U> fn) {
            return switch (this) {
                case Success<T> s -> {
                    try {
                        yield new Success<>(fn.apply(s.value()));
                    } catch (Exception e) {
                        yield new Failure<>(e.getMessage());
                    }
                }
                case Failure<T> f -> new Failure<>(f.error());
            };
        }

        static <T> Result<T> of(Callable<T> fn) {
            try {
                return new Success<>(fn.call());
            } catch (Exception e) {
                return new Failure<>(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    static Result<Integer> dividir(int a, int b) {
        if (b == 0) return new Result.Failure<>("division por cero");
        return new Result.Success<>(a / b);
    }

    static Result<Integer> parsearEntero(String s) {
        return Result.of(() -> Integer.parseInt(s));
    }

    public static void main(String[] args) {

        System.out.println("=== Result<T> sealed ===\n");

        // Division
        System.out.println("--- Division ---");
        Result<Integer> ok = dividir(10, 2);
        Result<Integer> err = dividir(10, 0);
        System.out.println("10/2 = " + ok);
        System.out.println("isSuccess: " + ok.isSuccess());
        System.out.println("getOrElse: " + ok.getOrElse(-1));
        System.out.println("10/0 = " + err);
        System.out.println("isSuccess: " + err.isSuccess());
        System.out.println("getOrElse: " + err.getOrElse(-1));

        // Parseo
        System.out.println("\n--- Parseo de entero ---");
        Result<Integer> num = parsearEntero("42");
        Result<Integer> numErr = parsearEntero("abc");
        System.out.println("parsear '42': " + num);
        System.out.println("parsear 'abc': " + numErr);

        // Encadenamiento de map: String -> Integer -> String
        System.out.println("\n--- Encadenamiento de map ---");
        Result<String> pipeline = parsearEntero("15")
            .map(n -> n * 2)               // Result<Integer>: 30
            .map(n -> "resultado: " + n);  // Result<String>: "resultado: 30"
        System.out.println("pipeline: " + pipeline);

        Result<String> pipelineErr = parsearEntero("xyz")
            .map(n -> n * 2)
            .map(n -> "resultado: " + n);
        System.out.println("pipeline con error: " + pipelineErr);

        // Result.of con lambda
        System.out.println("\n--- Result.of ---");
        Result<String> fromCallable = Result.of(() -> "hola".substring(0, 3));
        System.out.println("substring(0,3): " + fromCallable);

        Result<String> fromCallableErr = Result.of(() -> "hola".substring(0, 10));
        System.out.println("substring(0,10): " + fromCallableErr);
    }
}
