import java.util.concurrent.*;

public class Ejercicio3 {

    static class BulkheadFullException extends RuntimeException {
        BulkheadFullException(String name) {
            super("Bulkhead '" + name + "' lleno — llamada rechazada");
        }
    }

    static class Bulkhead {
        private final String name;
        private final Semaphore semaphore;

        Bulkhead(String name, int maxConcurrent) {
            this.name = name;
            this.semaphore = new Semaphore(maxConcurrent);
        }

        <T> T execute(Callable<T> task) throws Exception {
            if (!semaphore.tryAcquire()) {
                throw new BulkheadFullException(name);
            }
            try {
                return task.call();
            } finally {
                semaphore.release();
            }
        }
    }

    static class BulkheadRegistry {
        private final ConcurrentHashMap<String, Bulkhead> registry = new ConcurrentHashMap<>();

        Bulkhead getOrCreate(String name, int maxConcurrent) {
            return registry.computeIfAbsent(name, k -> new Bulkhead(name, maxConcurrent));
        }

        Bulkhead get(String name) {
            return registry.get(name);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BulkheadRegistry bRegistry = new BulkheadRegistry();
        Bulkhead bulkhead = bRegistry.getOrCreate("inventario-service", 3);

        ExecutorService pool = Executors.newFixedThreadPool(8);
        CyclicBarrier barrier = new CyclicBarrier(8);
        CountDownLatch latch = new CountDownLatch(8);

        int[] success = {0};
        int[] rejected = {0};

        for (int i = 1; i <= 8; i++) {
            final int threadId = i;
            pool.submit(() -> {
                try {
                    barrier.await();
                    bulkhead.execute(() -> {
                        synchronized (success) { success[0]++; }
                        System.out.println("Thread-" + threadId + " ejecutando...");
                        Thread.sleep(200);
                        System.out.println("Thread-" + threadId + " completado.");
                        return "ok";
                    });
                } catch (BulkheadFullException e) {
                    synchronized (rejected) { rejected[0]++; }
                    System.out.println("Thread-" + threadId + " RECHAZADO: " + e.getMessage());
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        System.out.println("\nResultados:");
        System.out.println("  Exitosos : " + success[0]);
        System.out.println("  Rechazados: " + rejected[0]);
    }
}
