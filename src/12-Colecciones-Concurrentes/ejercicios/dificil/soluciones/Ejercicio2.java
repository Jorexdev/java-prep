import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class Ejercicio2 {

    // Mapa con lock global unico
    static class GlobalLockMap<K, V> {
        private final HashMap<K, V> map = new HashMap<>();
        private final ReentrantLock lock = new ReentrantLock();

        public void put(K key, V value) {
            lock.lock();
            try { map.put(key, value); } finally { lock.unlock(); }
        }

        public V get(K key) {
            lock.lock();
            try { return map.get(key); } finally { lock.unlock(); }
        }
    }

    // Mapa con lock striping: 8 buckets independientes
    static class StripedMap<K, V> {
        private static final int STRIPES = 8;
        @SuppressWarnings("unchecked")
        private final HashMap<K, V>[] buckets = new HashMap[STRIPES];
        private final ReentrantLock[] locks = new ReentrantLock[STRIPES];

        StripedMap() {
            for (int i = 0; i < STRIPES; i++) {
                buckets[i] = new HashMap<>();
                locks[i] = new ReentrantLock();
            }
        }

        private int stripeFor(K key) {
            return Math.abs(key.hashCode() % STRIPES);
        }

        public void put(K key, V value) {
            int s = stripeFor(key);
            locks[s].lock();
            try { buckets[s].put(key, value); } finally { locks[s].unlock(); }
        }

        public V get(K key) {
            int s = stripeFor(key);
            locks[s].lock();
            try { return buckets[s].get(key); } finally { locks[s].unlock(); }
        }
    }

    static long benchmark(Runnable r) throws InterruptedException {
        // Warm up
        r.run();
        long start = System.currentTimeMillis();
        r.run();
        return System.currentTimeMillis() - start;
    }

    static long runTest(Object map, int numThreads, int opsPerThread) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(numThreads);
        long start = System.currentTimeMillis();
        for (int t = 0; t < numThreads; t++) {
            final int tid = t;
            new Thread(() -> {
                java.util.Random rng = new java.util.Random(tid);
                for (int i = 0; i < opsPerThread; i++) {
                    int key = rng.nextInt(1000);
                    if (rng.nextInt(10) < 3) { // 30% put
                        if (map instanceof StripedMap) ((StripedMap<Integer,Integer>)map).put(key, key);
                        else ((GlobalLockMap<Integer,Integer>)map).put(key, key);
                    } else {                    // 70% get
                        if (map instanceof StripedMap) ((StripedMap<Integer,Integer>)map).get(key);
                        else ((GlobalLockMap<Integer,Integer>)map).get(key);
                    }
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 8;
        int opsPerThread = 10000;
        long totalOps = (long) numThreads * opsPerThread;

        GlobalLockMap<Integer, Integer> globalMap = new GlobalLockMap<>();
        StripedMap<Integer, Integer> stripedMap = new StripedMap<>();

        // Warm up
        runTest(globalMap, 2, 1000);
        runTest(stripedMap, 2, 1000);

        long globalMs = runTest(globalMap, numThreads, opsPerThread);
        long stripedMs = runTest(stripedMap, numThreads, opsPerThread);

        System.out.println("=== Lock Striping vs Global Lock ===");
        System.out.println("Threads: " + numThreads + ", ops/thread: " + opsPerThread +
                           ", total: " + totalOps);
        System.out.println();
        System.out.printf("%-20s %8s %12s%n", "Implementacion", "Tiempo", "Ops/ms");
        System.out.println("-".repeat(42));
        System.out.printf("%-20s %6dms %12.0f%n", "Global Lock",
                          globalMs, globalMs > 0 ? totalOps / (double) globalMs : 0);
        System.out.printf("%-20s %6dms %12.0f%n", "Striped (8 buckets)",
                          stripedMs, stripedMs > 0 ? totalOps / (double) stripedMs : 0);
        System.out.println();
        if (globalMs > 0 && stripedMs > 0) {
            System.out.printf("Speedup StripedMap: %.2fx%n", (double) globalMs / stripedMs);
        }
        System.out.println("StripedMap reduce contención: solo 1/8 de las operaciones compiten.");
    }
}
