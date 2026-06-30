import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Ejercicio 1 (Difícil) — Distributed cache con locking anti-stampede
 *
 * Problema: cuando una key popular expira, múltiples threads detectan miss
 * simultáneamente y todos van a DB → thundering herd / dog piling.
 *
 * Solución: el primer thread en detectar el miss adquiere un lock.
 * Los demás threads esperan y releen del caché cuando el primero termina.
 * Solo 1 llamada a DB, N threads obtienen el resultado.
 */
public class Ejercicio1 {

    static class DistributedCache {
        private final Map<String, String> cache = new ConcurrentHashMap<>();
        // Un lock por key para evitar stampede
        private final ConcurrentHashMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();
        private final AtomicInteger dbCalls = new AtomicInteger(0);

        String get(String key) throws InterruptedException {
            // Intento rápido: si está en caché, devolver sin lock
            String cached = cache.get(key);
            if (cached != null) {
                return cached;
            }

            // Miss: adquirir lock para esta key específica
            ReentrantLock lock = keyLocks.computeIfAbsent(key, k -> new ReentrantLock());
            lock.lock();
            try {
                // Double-check: puede que otro thread haya cargado mientras esperábamos
                cached = cache.get(key);
                if (cached != null) {
                    System.out.printf("  [Thread-%s] Double-check hit para '%s'%n",
                            Thread.currentThread().getName(), key);
                    return cached;
                }

                // Realmente hay miss: este thread va a DB
                int callNum = dbCalls.incrementAndGet();
                System.out.printf("  [Thread-%s] DB CALL #%d para '%s'%n",
                        Thread.currentThread().getName(), callNum, key);
                Thread.sleep(100); // simula latencia de DB
                String value = "dato_de_" + key + "_v1";
                cache.put(key, value);
                return value;

            } finally {
                lock.unlock();
                keyLocks.remove(key); // limpieza del lock (opcional en producción)
            }
        }

        int getDbCalls() { return dbCalls.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Anti-stampede: 10 threads, 1 key ===\n");

        DistributedCache cache = new DistributedCache();
        int numThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);  // sincroniza el inicio
        CountDownLatch doneLatch  = new CountDownLatch(numThreads);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();  // todos esperan para lanzarse simultáneamente
                    String value = cache.get("producto:popular");
                    results.add(value);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();  // dispara todos los threads a la vez
        doneLatch.await();
        executor.shutdown();

        System.out.printf("%nThreads que obtuvieron resultado: %d%n", results.size());
        System.out.printf("Llamadas reales a DB:             %d (de %d threads)%n",
                cache.getDbCalls(), numThreads);
        System.out.printf("Todos obtuvieron el mismo valor:  %s%n",
                results.stream().distinct().count() == 1);

        // Verificación: solo 1 DB call aunque 10 threads compitieron
        assert cache.getDbCalls() == 1 : "Debería haber exactamente 1 DB call";
    }
}
