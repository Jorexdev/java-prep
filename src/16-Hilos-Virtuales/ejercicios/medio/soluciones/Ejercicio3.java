import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Ejercicio3 {

    // Objeto para demostrar pinning con synchronized
    static final Object MONITOR = new Object();
    // Lock alternativo sin pinning
    static final ReentrantLock REENTRANT = new ReentrantLock();

    static long medirSynchronized(int numThreads, int sleepMs) throws InterruptedException {
        List<Thread> threads = new ArrayList<>(numThreads);
        long start = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            Thread t = Thread.ofVirtual().start(() -> {
                synchronized (MONITOR) {
                    // PINNING: el virtual thread mantiene ocupado el carrier thread
                    // durante el sleep porque está dentro de un bloque synchronized.
                    // El carrier no puede ejecutar otro virtual thread.
                    try { Thread.sleep(sleepMs); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            threads.add(t);
        }
        for (Thread t : threads) t.join();
        return System.currentTimeMillis() - start;
    }

    static long medirReentrantLock(int numThreads, int sleepMs) throws InterruptedException {
        List<Thread> threads = new ArrayList<>(numThreads);
        long start = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            Thread t = Thread.ofVirtual().start(() -> {
                REENTRANT.lock();
                try {
                    // SIN PINNING: ReentrantLock permite al virtual thread ceder
                    // el carrier durante el sleep. El carrier puede ejecutar otros.
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    REENTRANT.unlock();
                }
            });
            threads.add(t);
        }
        for (Thread t : threads) t.join();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 50;
        int sleepMs = 20;

        System.out.println("=== Pinning: synchronized vs ReentrantLock ===");
        System.out.printf("Virtual threads: %d, sleep dentro del lock: %dms%n%n", numThreads, sleepMs);

        // Warm up
        medirSynchronized(5, 5);
        medirReentrantLock(5, 5);

        long syncMs = medirSynchronized(numThreads, sleepMs);
        long lockMs = medirReentrantLock(numThreads, sleepMs);

        System.out.println("=== Resultados ===");
        System.out.printf("%-30s %8dms%n", "synchronized (con pinning):", syncMs);
        System.out.printf("%-30s %8dms%n", "ReentrantLock (sin pinning):", lockMs);
        System.out.println();
        System.out.printf("Speedup ReentrantLock: %.1fx%n", (double) syncMs / lockMs);
        System.out.println();
        System.out.println("Explicacion:");
        System.out.println("  synchronized pina el carrier: " + numThreads +
                           " threads secuenciales = ~" + (numThreads * sleepMs) + "ms");
        System.out.println("  ReentrantLock libera carrier: todos en paralelo = ~" + sleepMs + "ms");
        System.out.println();
        System.out.println("Regla: con virtual threads, preferir ReentrantLock sobre synchronized.");
        System.out.println("JDK 23+ esta eliminando el pinning para synchronized tambien.");
    }
}
