import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Ejercicio1 {

    static class LRUCache<K, V> {
        private final int capacity;
        private final LinkedHashMap<K, V> map;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private final AtomicInteger evictions = new AtomicInteger(0);
        private final AtomicInteger hits = new AtomicInteger(0);
        private final AtomicInteger misses = new AtomicInteger(0);

        LRUCache(int capacity) {
            this.capacity = capacity;
            // access-order=true: el ultimo accedido va al final; al evictar, se elimina el primero
            this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    boolean evict = size() > capacity;
                    if (evict) evictions.incrementAndGet();
                    return evict;
                }
            };
        }

        public V get(K key) {
            lock.writeLock().lock(); // write porque LinkedHashMap modifica orden en get
            try {
                V val = map.get(key);
                if (val != null) hits.incrementAndGet(); else misses.incrementAndGet();
                return val;
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void put(K key, V value) {
            lock.writeLock().lock();
            try {
                map.put(key, value);
            } finally {
                lock.writeLock().unlock();
            }
        }

        public int size() {
            lock.readLock().lock();
            try { return map.size(); } finally { lock.readLock().unlock(); }
        }

        public int getEvictions() { return evictions.get(); }
        public int getHits() { return hits.get(); }
        public int getMisses() { return misses.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        int capacity = 20;
        LRUCache<Integer, String> cache = new LRUCache<>(capacity);
        int numThreads = 8;
        int opsPerThread = 500;
        AtomicInteger totalOps = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int tid = t;
            new Thread(() -> {
                java.util.Random rng = new java.util.Random(tid);
                for (int i = 0; i < opsPerThread; i++) {
                    int key = rng.nextInt(50); // keys 0-49, mayor que capacity -> evictions
                    if (rng.nextBoolean()) {
                        cache.put(key, "val-" + tid + "-" + key);
                    } else {
                        cache.get(key);
                    }
                    totalOps.incrementAndGet();

                    // Verificar invariante: tamano nunca supera capacity
                    int sz = cache.size();
                    if (sz > capacity) {
                        System.out.println("ERROR: tamano " + sz + " supera capacidad " + capacity);
                    }
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        System.out.println("=== LRU Cache thread-safe ===");
        System.out.println("Capacidad maxima : " + capacity);
        System.out.println("Tamano final     : " + cache.size() + " (<= " + capacity + " [" +
                           (cache.size() <= capacity ? "OK" : "FALLO") + "])");
        System.out.println("Operaciones total: " + totalOps.get());
        System.out.println("Hits             : " + cache.getHits());
        System.out.println("Misses           : " + cache.getMisses());
        System.out.println("Evictions        : " + cache.getEvictions());
    }
}
