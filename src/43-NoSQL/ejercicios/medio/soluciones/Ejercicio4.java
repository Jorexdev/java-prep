import java.util.*;

/**
 * Ejercicio 4 (Medio) — LRU Cache
 * LinkedHashMap con accessOrder=true evicta automáticamente el eldest entry.
 */
public class Ejercicio4 {

    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacidad;
        private final List<K> evicted = new ArrayList<>();

        LRUCache(int capacidad) {
            // accessOrder=true: get() y put() mueven el entry al final (más reciente)
            super(capacidad, 0.75f, true);
            this.capacidad = capacidad;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            if (size() > capacidad) {
                System.out.printf("  [EVICT] '%s' (menos usado recientemente)%n", eldest.getKey());
                evicted.add(eldest.getKey());
                return true;
            }
            return false;
        }

        List<K> getEvicted() { return evicted; }
    }

    public static void main(String[] args) {
        LRUCache<String, String> cache = new LRUCache<>(3);

        System.out.println("Insertando A, B, C (capacidad 3):");
        cache.put("A", "valor-A");
        cache.put("B", "valor-B");
        cache.put("C", "valor-C");
        System.out.println("  Cache: " + cache.keySet());

        // Acceder a A para que quede como "más reciente"
        System.out.println("\nAccediendo a A (queda como más reciente):");
        cache.get("A");
        System.out.println("  Orden interno (más antiguo → más reciente): " + cache.keySet());

        System.out.println("\nInsertando D → evicta el eldest (B, ya que A se accedió):");
        cache.put("D", "valor-D");
        System.out.println("  Cache: " + cache.keySet());

        System.out.println("\nInsertando E → evicta C:");
        cache.put("E", "valor-E");
        System.out.println("  Cache: " + cache.keySet());

        System.out.println("\nKeys eviccionadas en orden: " + cache.getEvicted());
    }
}
