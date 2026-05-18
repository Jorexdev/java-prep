import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Ejercicio6 {

    static final Map<String, String> cache = new HashMap<>();

    // Simula una operación costosa de cálculo
    static String calcularValor(String clave) {
        System.out.println("  [calcularValor] Calculando para clave: " + clave);
        return "valor-de-" + clave;
    }

    // Busca en caché; si no existe, calcula, guarda y devuelve
    static Optional<String> obtener(String clave) {
        String resultado = cache.computeIfAbsent(clave, k -> calcularValor(k));
        return Optional.ofNullable(resultado);
    }

    public static void main(String[] args) {
        System.out.println("=== Primera llamada a 'config' ===");
        Optional<String> v1 = obtener("config");
        System.out.println("Resultado: " + v1.orElse("vacío"));

        System.out.println("\n=== Segunda llamada a 'config' (desde caché) ===");
        Optional<String> v2 = obtener("config");
        System.out.println("Resultado: " + v2.orElse("vacío"));

        System.out.println("\n=== Primera llamada a 'timeout' (clave nueva) ===");
        Optional<String> v3 = obtener("timeout");
        System.out.println("Resultado: " + v3.orElse("vacío"));

        System.out.println("\n=== Segunda llamada a 'timeout' (desde caché) ===");
        Optional<String> v4 = obtener("timeout");
        System.out.println("Resultado: " + v4.orElse("vacío"));

        System.out.println("\n=== Estado del caché ===");
        cache.forEach((k, v) -> System.out.println("  " + k + " → " + v));
    }
}
