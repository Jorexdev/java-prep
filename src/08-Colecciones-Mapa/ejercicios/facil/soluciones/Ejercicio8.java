import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Ejercicio8 {

    public static void main(String[] args) {
        String[] elementos = {"banana", "apple", "cherry", "date", "elderberry"};

        // HashMap: orden no garantizado
        Map<String, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < elementos.length; i++) {
            hashMap.put(elementos[i], i + 1);
        }

        // LinkedHashMap: mantiene orden de inserción
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < elementos.length; i++) {
            linkedHashMap.put(elementos[i], i + 1);
        }

        System.out.println("HashMap (orden NO garantizado):");
        hashMap.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

        System.out.println("\nLinkedHashMap (orden de INSERCIÓN garantizado):");
        linkedHashMap.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

        System.out.println("\nDiferencia: LinkedHashMap preserva el orden de inserción.");
        System.out.println("HashMap puede mostrar las entradas en cualquier orden.");
    }
}
