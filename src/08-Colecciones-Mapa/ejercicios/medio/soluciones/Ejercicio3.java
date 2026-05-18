import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    public static void main(String[] args) {
        Map<String, Integer> original = new HashMap<>();
        original.put("Ana", 25);
        original.put("Luis", 30);
        original.put("Marta", 25);
        original.put("Jorge", 30);
        original.put("Eva", 25);

        System.out.println("Original (nombre -> edad):");
        original.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

        // Invertir: edad -> [nombres]
        Map<Integer, List<String>> invertido = new HashMap<>();
        original.forEach((nombre, edad) ->
            invertido.computeIfAbsent(edad, k -> new ArrayList<>()).add(nombre)
        );

        System.out.println("\nInvertido (edad -> nombres):");
        invertido.forEach((edad, nombres) ->
            System.out.println("  " + edad + " -> " + nombres)
        );
    }
}
