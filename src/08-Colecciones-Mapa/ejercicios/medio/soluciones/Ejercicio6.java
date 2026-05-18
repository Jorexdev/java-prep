import java.util.LinkedHashMap;
import java.util.Map;

public class Ejercicio6 {

    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacidad;

        public LRUCache(int capacidad) {
            // accessOrder=true: al hacer get, el elemento pasa al final (MRU)
            super(capacidad, 0.75f, true);
            this.capacidad = capacidad;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacidad;
        }
    }

    public static void main(String[] args) {
        LRUCache<String, Integer> cache = new LRUCache<>(3);

        cache.put("A", 1);
        cache.put("B", 2);
        cache.put("C", 3);
        System.out.println("Tras put A, B, C: " + cache.keySet()); // [A, B, C]

        // Acceder a A: A se convierte en MRU (se mueve al final)
        cache.get("A");
        System.out.println("Tras get(A):      " + cache.keySet()); // [B, C, A]

        // Insertar D: se elimina el LRU que es B (el menos recientemente usado)
        cache.put("D", 4);
        System.out.println("Tras put D:       " + cache.keySet()); // [C, A, D]
        System.out.println("¿B fue eliminado? " + !cache.containsKey("B")); // true
        System.out.println("¿A sigue? " + cache.containsKey("A"));          // true
    }
}
