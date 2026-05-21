import java.util.concurrent.ConcurrentHashMap;

public class Ejercicio3 {

    static class Entry<V> {
        final V value;
        final long timestamp;

        Entry(V value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }

    static class SimpleCache<K, V> {
        private final ConcurrentHashMap<K, Entry<V>> store = new ConcurrentHashMap<>();

        public void put(K key, V value) {
            store.put(key, new Entry<>(value));
        }

        public V get(K key, long ttlMs) {
            Entry<V> entry = store.get(key);
            if (entry == null) return null;
            if (System.currentTimeMillis() - entry.timestamp > ttlMs) {
                return null; // expirado
            }
            return entry.value;
        }

        public int evictExpired(long ttlMs) {
            long now = System.currentTimeMillis();
            int[] removidos = {0};
            store.entrySet().removeIf(e -> {
                boolean expired = now - e.getValue().timestamp > ttlMs;
                if (expired) removidos[0]++;
                return expired;
            });
            return removidos[0];
        }

        public int size() { return store.size(); }
    }

    public static void main(String[] args) throws InterruptedException {
        SimpleCache<String, String> cache = new SimpleCache<>();
        long ttl = 100; // 100ms TTL

        // Insertar 5 entradas
        cache.put("key1", "valor1");
        cache.put("key2", "valor2");
        cache.put("key3", "valor3");
        Thread.sleep(60);
        // Insertar 2 mas tarde (no han expirado aun)
        cache.put("key4", "valor4");
        cache.put("key5", "valor5");

        System.out.println("=== Cache con TTL ===");
        System.out.println("TTL: " + ttl + "ms");
        System.out.println("Tamano inicial: " + cache.size());
        System.out.println();

        // Antes de expirar
        System.out.println("--- Antes de expirar key1-3 ---");
        System.out.println("key1: " + cache.get("key1", ttl));
        System.out.println("key4: " + cache.get("key4", ttl));

        Thread.sleep(60); // total: ~120ms para key1-3, ~60ms para key4-5

        System.out.println();
        System.out.println("--- Despues de 120ms (key1-3 expiradas, key4-5 no) ---");
        System.out.println("key1: " + cache.get("key1", ttl) + " (esperado: null)");
        System.out.println("key2: " + cache.get("key2", ttl) + " (esperado: null)");
        System.out.println("key4: " + cache.get("key4", ttl) + " (esperado: valor4)");
        System.out.println("key5: " + cache.get("key5", ttl) + " (esperado: valor5)");

        int evicted = cache.evictExpired(ttl);
        System.out.println();
        System.out.println("Entradas eliminadas por evictExpired: " + evicted + " (esperado: 3)");
        System.out.println("Tamano tras evict: " + cache.size() + " (esperado: 2)");
    }
}
