import java.util.LinkedHashMap;
import java.util.Map;

public class Ejercicio1 {

    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacidad;

        public LRUCache(int capacidad) {
            // accessOrder=true: el orden refleja el acceso (MRU al final)
            super(capacidad, 0.75f, true);
            this.capacidad = capacidad;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacidad;
        }
    }

    public static void main(String[] args) {
        LRUCache<Integer, String> cache = new LRUCache<>(3);

        cache.put(1, "uno");
        cache.put(2, "dos");
        cache.put(3, "tres");
        System.out.println("Tras put 1,2,3: " + cache); // {1=uno, 2=dos, 3=tres}

        // Acceder a la clave 1 la convierte en la más recientemente usada
        cache.get(1);
        System.out.println("Tras get(1):     " + cache); // {2=dos, 3=tres, 1=uno}

        // Al insertar 4, debe eliminarse el LRU (2)
        cache.put(4, "cuatro");
        System.out.println("Tras put(4):     " + cache); // {3=tres, 1=uno, 4=cuatro}
        System.out.println("¿Contiene 2? " + cache.containsKey(2)); // false
        System.out.println("¿Contiene 1? " + cache.containsKey(1)); // true
    }
}
