import java.util.List;

public class Ejercicio3 {

    static String describir(Object obj) {
        // Pattern matching con instanceof: test + binding en una sola expresión
        if (obj instanceof String s) {
            return "String: " + s + " (len=" + s.length() + ")";
        } else if (obj instanceof Integer i) {
            String signo = i > 0 ? "positivo" : i < 0 ? "negativo" : "cero";
            return "Integer: " + i + " (" + signo + ")";
        } else if (obj instanceof Double d) {
            return "Double: " + d + " (redondeado a " + d.intValue() + ")";
        } else if (obj instanceof List<?> lista) {
            return "List de " + lista.size() + " elementos";
        }
        return "Desconocido: " + obj;
    }

    public static void main(String[] args) {
        List<Object> elementos = List.of(
            "Hola",
            42,
            -7,
            0,
            3.7,
            9.99,
            List.of("a", "b", "c"),
            List.of()
        );

        System.out.println("=== Pattern Matching con instanceof ===");
        elementos.forEach(e -> System.out.println("  " + describir(e)));
    }
}
