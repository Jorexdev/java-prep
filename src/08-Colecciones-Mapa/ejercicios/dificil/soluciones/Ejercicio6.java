import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Ejercicio6 {

    public static void main(String[] args) {
        int N = 100_000;

        // --- HashMap ---
        Map<Integer, String> hashMap = new HashMap<>();

        long inicio = System.nanoTime();
        for (int i = 0; i < N; i++) {
            hashMap.put(i, "valor-" + i);
        }
        long insertHashMap = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        for (int i = 0; i < N; i++) {
            hashMap.get(i);
        }
        long busqHashMap = System.nanoTime() - inicio;

        // --- TreeMap ---
        Map<Integer, String> treeMap = new TreeMap<>();

        inicio = System.nanoTime();
        for (int i = 0; i < N; i++) {
            treeMap.put(i, "valor-" + i);
        }
        long insertTreeMap = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        for (int i = 0; i < N; i++) {
            treeMap.get(i);
        }
        long busqTreeMap = System.nanoTime() - inicio;

        // --- Resultados ---
        System.out.printf("%-20s %12s %12s%n", "Operación", "HashMap", "TreeMap");
        System.out.println("-".repeat(46));
        System.out.printf("%-20s %10.2f ms %10.2f ms%n",
            "Insertar " + N, insertHashMap / 1e6, insertTreeMap / 1e6);
        System.out.printf("%-20s %10.2f ms %10.2f ms%n",
            "Buscar " + N, busqHashMap / 1e6, busqTreeMap / 1e6);

        System.out.println("\nConclusiones:");
        System.out.println("  HashMap  — O(1) amortizado. Sin orden garantizado.");
        System.out.println("             Ideal cuando la velocidad importa más que el orden.");
        System.out.println("  TreeMap  — O(log n). Claves ordenadas en todo momento.");
        System.out.println("             Ideal cuando necesitas rango, firstKey, lastKey,");
        System.out.println("             subMap, headMap o iteración ordenada.");
    }
}
