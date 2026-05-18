import java.util.List;
import java.util.function.Function;

public class Ejercicio3 {

    // Compone una lista de funciones aplicándolas en orden (la primera se aplica primero)
    static <T> Function<T, T> componerTodas(List<Function<T, T>> funciones) {
        return funciones.stream()
                        .reduce(Function.identity(), Function::andThen);
    }

    public static void main(String[] args) {

        List<Function<String, String>> pasos = List.of(
            String::trim,
            String::toLowerCase,
            s -> s.replace(" ", "-"),
            s -> s + "-processed"
        );

        Function<String, String> pipeline = componerTodas(pasos);

        List<String> entradas = List.of(
            "  Hola Mundo  ",
            " Java Stream API ",
            "  Funciones Puras  "
        );

        System.out.println("Entrada → Salida:");
        entradas.forEach(e -> System.out.println("  '" + e + "' → '" + pipeline.apply(e) + "'"));
    }
}
