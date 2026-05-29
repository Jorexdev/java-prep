import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Ejercicio5 {

    // --- Entidad de dominio ---

    static class Producto {
        final int id;
        String nombre;
        double precio;

        Producto(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }

        @Override
        public String toString() {
            return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
        }
    }

    // --- Entrada de caché con TTL ---

    static class CacheEntry<V> {
        final V value;
        final long expiryTs; // tiempo simulado de expiración
        long lastAccess;     // para política LRU

        CacheEntry(V value, long expiryTs, long now) {
            this.value = value;
            this.expiryTs = expiryTs;
            this.lastAccess = now;
        }

        boolean isExpired(long now) { return now >= expiryTs; }
    }

    // --- CacheL2 con TTL + LRU + estadísticas ---

    static class CacheL2<K, V> {
        private final int maxSize;
        private final long ttlMs;
        private final AtomicLong clock;

        // LinkedHashMap con accessOrder=true implementa LRU de forma nativa
        private final LinkedHashMap<K, CacheEntry<V>> store;

        private long hits = 0;
        private long misses = 0;
        private long evictions = 0;

        CacheL2(int maxSize, long ttlMs, AtomicLong clock) {
            this.maxSize = maxSize;
            this.ttlMs   = ttlMs;
            this.clock   = clock;
            this.store   = new LinkedHashMap<>(maxSize, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                    if (size() > maxSize) {
                        evictions++;
                        System.out.println("[CacheL2] eviction LRU: key=" + eldest.getKey());
                        return true;
                    }
                    return false;
                }
            };
        }

        Optional<V> get(K key) {
            long now = clock.get();
            CacheEntry<V> entry = store.get(key);

            if (entry == null) {
                misses++;
                return Optional.empty();
            }

            if (entry.isExpired(now)) {
                store.remove(key);
                misses++;
                System.out.println("[CacheL2] MISS (expirado) key=" + key);
                return Optional.empty();
            }

            entry.lastAccess = now;
            hits++;
            System.out.println("[CacheL2] HIT key=" + key);
            return Optional.of(entry.value);
        }

        void put(K key, V value) {
            long now = clock.get();
            store.put(key, new CacheEntry<>(value, now + ttlMs, now));
        }

        void printStats(int totalRequests) {
            System.out.println("\n=== Estadísticas CacheL2 ===");
            System.out.println("  Hits:      " + hits);
            System.out.println("  Misses:    " + misses);
            System.out.println("  Evictions: " + evictions);
            System.out.printf( "  Hit rate:  %.1f%%%n",
                totalRequests > 0 ? (hits * 100.0 / totalRequests) : 0.0);
        }
    }

    // --- EntityManager simulado ---

    static class EntityManager {
        private final Map<Integer, Producto> db = new HashMap<>();
        private final CacheL2<Integer, Producto> cache;
        private int dbReads = 0;

        EntityManager(CacheL2<Integer, Producto> cache) {
            this.cache = cache;
            // Poblar BD simulada
            db.put(1, new Producto(1, "Teclado",  49.99));
            db.put(2, new Producto(2, "Ratón",    29.99));
            db.put(3, new Producto(3, "Monitor",  349.99));
            db.put(4, new Producto(4, "Auriculares", 89.99));
            db.put(5, new Producto(5, "Webcam",   59.99));
        }

        Producto findById(int id) {
            Optional<Producto> cached = cache.get(id);
            if (cached.isPresent()) {
                return cached.get();
            }

            // Simular latencia de BD
            Producto p = db.get(id);
            dbReads++;
            if (p != null) {
                System.out.println("[BD] SELECT id=" + id + " → " + p.nombre);
                cache.put(id, p);
            } else {
                System.out.println("[BD] SELECT id=" + id + " → NOT FOUND");
            }
            return p;
        }

        int getDbReads() { return dbReads; }
    }

    public static void main(String[] args) {
        AtomicLong clock = new AtomicLong(0);

        // Caché: máx 3 entradas, TTL de 500 ms simulados
        CacheL2<Integer, Producto> cache = new CacheL2<>(3, 500L, clock);
        EntityManager em = new EntityManager(cache);

        int totalRequests = 0;

        System.out.println("=== Fase 1: lecturas iniciales ===");
        int[] ids = {1, 2, 3, 1, 2, 4};
        for (int id : ids) {
            em.findById(id);
            totalRequests++;
        }

        System.out.println("\n=== Fase 2: eviction LRU (añadir id=5, maxSize=3) ===");
        // Actualmente en caché: 1(LRU), 2, 3 — id=4 entró y el más antiguo es 1 (accedido antes)
        // Al añadir 5, se desaloja el menos usado
        em.findById(5);
        totalRequests++;
        em.findById(1); // fue eviccionado → miss + nueva query BD
        totalRequests++;

        System.out.println("\n=== Fase 3: avanzar reloj para expirar entradas (TTL=500) ===");
        clock.addAndGet(600L); // superamos el TTL
        System.out.println("[Reloj] avanzado a t=" + clock.get() + "ms");

        int[] idsExpired = {2, 3, 1};
        for (int id : idsExpired) {
            em.findById(id);
            totalRequests++;
        }

        System.out.println("\n=== Fase 4: lecturas tras recarga ===");
        em.findById(2);
        totalRequests++;
        em.findById(2); // hit garantizado
        totalRequests++;

        System.out.println("\n[BD] Total lecturas a BD: " + em.getDbReads());
        cache.printStats(totalRequests);
    }
}
