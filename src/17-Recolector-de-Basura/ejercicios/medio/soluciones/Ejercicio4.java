import java.util.HashMap;
import java.util.WeakHashMap;

public class Ejercicio4 {

    static class HashMapCache {
        private final HashMap<String, String> map = new HashMap<>();

        void put(String key, String value) { map.put(key, value); }
        int size() { return map.size(); }
    }

    static class WeakMapCache {
        private final WeakHashMap<String, String> map = new WeakHashMap<>();

        void put(String key, String value) { map.put(key, value); }
        int size() { return map.size(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Memory Leak Detection: HashMap vs WeakHashMap ===");
        System.out.println();

        HashMapCache hashCache = new HashMapCache();
        WeakMapCache weakCache = new WeakMapCache();

        System.out.printf("%-10s %-15s %-15s%n", "Acceso", "HashMap size", "WeakHashMap size");
        System.out.println("-".repeat(42));

        for (int i = 1; i <= 100; i++) {
            // Clave creada con new String para evitar interning
            // La referencia local desaparece al final de cada iteración
            String key = new String("key-" + i);
            String value = "value-" + i;

            hashCache.put(key, value);
            weakCache.put(key, value);
            // key queda fuera de scope después de cada iteración
            // (la variable local es elegible para GC en la siguiente iteración)

            if (i % 20 == 0) {
                System.gc();
                Thread.sleep(100);
                System.out.printf("%-10d %-15d %-15d%n", i, hashCache.size(), weakCache.size());
            }
        }

        System.out.println();
        System.out.println("=== Resultado ===");
        System.out.println("HashMap final:     " + hashCache.size() + " entradas (retiene todo)");
        System.out.println("WeakHashMap final: " + weakCache.size() + " entradas (libera claves sin referencia)");
        System.out.println();
        System.out.println("El HashMap retiene las N entradas: MEMORY LEAK.");
        System.out.println("El WeakHashMap reduce su tamaño cuando el GC recolecta las claves.");
    }
}
