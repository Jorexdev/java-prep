import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Ejercicio6 {

    sealed interface Option<T> permits Option.Some, Option.None {

        record Some<T>(T value) implements Option<T> {}
        final class None<T> implements Option<T> {
            private static final None<?> INSTANCE = new None<>();
            private None() {}
            @Override public String toString() { return "None"; }
        }

        @SuppressWarnings("unchecked")
        static <T> Option<T> empty() {
            return (Option<T>) None.INSTANCE;
        }

        static <T> Option<T> of(T value) {
            return value == null ? empty() : new Some<>(value);
        }

        default boolean isPresent() {
            return this instanceof Some<T>;
        }

        default T get() {
            return switch (this) {
                case Some<T> s -> s.value();
                case None<T> n -> throw new NoSuchElementException("Option.None.get()");
            };
        }

        default T getOrElse(T defaultValue) {
            return switch (this) {
                case Some<T> s -> s.value();
                case None<T> n -> defaultValue;
            };
        }

        default <U> Option<U> map(Function<T, U> fn) {
            return switch (this) {
                case Some<T> s -> Option.of(fn.apply(s.value()));
                case None<T> n -> Option.empty();
            };
        }

        default Option<T> filter(Predicate<T> pred) {
            return switch (this) {
                case Some<T> s when pred.test(s.value()) -> this;
                case Some<T> s                           -> Option.empty();
                case None<T> n                           -> this;
            };
        }
    }

    static Option<Integer> parsearEntero(String s) {
        try {
            return Option.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Option.empty();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Option<T> sealed ===\n");

        // Parseo de entero
        System.out.println("--- Parseo ---");
        Option<Integer> num = parsearEntero("42");
        Option<Integer> fallo = parsearEntero("abc");
        System.out.println("parsear '42': " + num + " isPresent=" + num.isPresent());
        System.out.println("parsear 'abc': " + fallo + " isPresent=" + fallo.isPresent());
        System.out.println("getOrElse -1: " + fallo.getOrElse(-1));

        // Encadenamiento de map
        System.out.println("\n--- Encadenamiento map ---");
        Option<String> resultado = parsearEntero("10")
            .map(n -> n * 3)        // Some(30)
            .map(n -> "x" + n);     // Some("x30")
        System.out.println("10 * 3 -> prefijo: " + resultado);

        Option<String> falloPipeline = parsearEntero("no")
            .map(n -> n * 3)
            .map(n -> "x" + n);
        System.out.println("fallo pipeline: " + falloPipeline);

        // filter
        System.out.println("\n--- filter ---");
        Option<Integer> par = parsearEntero("8").filter(n -> n % 2 == 0);
        Option<Integer> impar = parsearEntero("7").filter(n -> n % 2 == 0);
        System.out.println("8 filtrado (par): " + par);
        System.out.println("7 filtrado (par): " + impar);

        // of con null
        System.out.println("\n--- of con null ---");
        Option<String> conNull = Option.of(null);
        Option<String> conValor = Option.of("hola");
        System.out.println("Option.of(null): " + conNull);
        System.out.println("Option.of('hola'): " + conValor);

        // Comparacion con Optional<T> de Java
        System.out.println("\n--- Comparacion con Optional<T> de Java ---");
        Optional<Integer> optJava = Optional.ofNullable(null);
        Option<Integer> optPropio = Option.of(null);
        System.out.println("Optional.ofNullable(null).isPresent(): " + optJava.isPresent());
        System.out.println("Option.of(null).isPresent(): " + optPropio.isPresent());
        // Diferencia clave: Option es sealed -> el compilador verifica exhaustiveness en switch
        // Optional no es sealed -> siempre necesita default o ifPresent
        System.out.println("Option es sealed: switch puede ser exhaustivo sin default");
        System.out.println("Optional NO es sealed: necesita orElse/ifPresent para acceder al valor");
    }
}
