import java.util.HashMap;
import java.util.Map;

public class Ejercicio2 {

    public static void main(String[] args) {
        Map<String, String> capitales = new HashMap<>();
        capitales.put("España", "Madrid");
        capitales.put("Francia", "París");
        capitales.put("Alemania", "Berlín");
        capitales.put("Italia", "Roma");
        capitales.put("Portugal", "Lisboa");

        // Iteración con entrySet en for-each
        System.out.println("=== entrySet() for-each ===");
        for (Map.Entry<String, String> entrada : capitales.entrySet()) {
            System.out.println("  " + entrada.getKey() + " -> " + entrada.getValue());
        }

        // Iteración con forEach (BiConsumer)
        System.out.println("\n=== forEach(BiConsumer) ===");
        capitales.forEach((pais, capital) ->
            System.out.println("  " + pais + " -> " + capital)
        );
    }
}
