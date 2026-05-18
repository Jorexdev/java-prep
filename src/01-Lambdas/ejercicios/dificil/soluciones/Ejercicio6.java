import java.util.List;
import java.util.function.Function;

public class Ejercicio6 {

    public static void main(String[] args) {

        // Pipeline de 4 pasos encadenados con andThen
        Function<String, String> pipeline = ((Function<String, String>) Ejercicio6::soloLetrasYEspacios)
            .andThen(String::toLowerCase)
            .andThen(Ejercicio6::capitalizarPalabras)
            .andThen(s -> "Procesado: " + s);

        List<String> entradas = List.of(
            "h3llo w0rld!!",
            "java 21 es genial!",
            "  100% funcional  ",
            "API REST con spring-boot"
        );

        entradas.forEach(entrada ->
            System.out.println("'" + entrada + "'\n  → '" + pipeline.apply(entrada) + "'\n")
        );
    }

    private static String soloLetrasYEspacios(String s) {
        return s.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]", "").trim();
    }

    private static String capitalizarPalabras(String s) {
        if (s.isEmpty()) return s;
        String[] palabras = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : palabras) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0)))
                  .append(p.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
