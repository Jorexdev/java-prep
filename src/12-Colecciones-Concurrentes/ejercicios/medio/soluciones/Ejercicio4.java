import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Ejercicio4 {

    static class DataStore {
        private final Map<String, String> data = new HashMap<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public String read(String key) {
            lock.readLock().lock();
            try {
                return data.get(key);
            } finally {
                lock.readLock().unlock();
            }
        }

        public void write(String key, String value) {
            lock.writeLock().lock();
            try {
                data.put(key, value);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DataStore store = new DataStore();
        // Inicializar con algunos datos
        for (int i = 0; i < 10; i++) {
            store.write("key" + i, "val" + i);
        }

        int numReaders = 8;
        int numWriters = 2;
        AtomicLong readOps = new AtomicLong(0);
        AtomicLong writeOps = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(numReaders + numWriters);

        long start = System.currentTimeMillis();

        // Readers (80%)
        for (int r = 0; r < numReaders; r++) {
            final int rid = r;
            new Thread(() -> {
                for (int i = 0; i < 500; i++) {
                    String val = store.read("key" + (i % 10));
                    readOps.incrementAndGet();
                }
                System.out.println("[Reader-" + rid + "] completadas 500 lecturas");
                latch.countDown();
            }, "Reader-" + r).start();
        }

        // Writers (20%)
        for (int w = 0; w < numWriters; w++) {
            final int wid = w;
            new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    store.write("key" + (i % 10), "nuevo-val-w" + wid + "-" + i);
                    writeOps.incrementAndGet();
                }
                System.out.println("[Writer-" + wid + "] completadas 100 escrituras");
                latch.countDown();
            }, "Writer-" + w).start();
        }

        latch.await();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("\n=== ReadWriteLock DataStore ===");
        System.out.println("Lecturas totales : " + readOps.get());
        System.out.println("Escrituras totales: " + writeOps.get());
        System.out.println("Tiempo total     : " + elapsed + "ms");
        System.out.println("Readers concurrentes: multiples (ReadLock compartido)");
        System.out.println("Writers exclusivos : 1 a la vez (WriteLock exclusivo)");
    }
}
