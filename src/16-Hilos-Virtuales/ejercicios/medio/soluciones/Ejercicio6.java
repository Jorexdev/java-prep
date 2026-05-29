import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio6 {

    // VirtualThreadPool: limita concurrencia de virtual threads via Semaphore
    static class VirtualThreadPool {
        private final int maxConcurrent;
        private final Semaphore semaphore;
        private final BlockingQueue<Runnable> queue;
        private Thread dispatcher;
        private volatile boolean running = false;
        private volatile boolean shutdown = false;

        private final AtomicInteger completed = new AtomicInteger(0);
        private final AtomicInteger active = new AtomicInteger(0);
        private final AtomicInteger maxActive = new AtomicInteger(0);

        VirtualThreadPool(int maxConcurrent) {
            this.maxConcurrent = maxConcurrent;
            this.semaphore = new Semaphore(maxConcurrent);
            this.queue = new LinkedBlockingQueue<>();
        }

        void submit(Runnable task) {
            if (shutdown) throw new IllegalStateException("Pool cerrado");
            queue.offer(task);
        }

        void start() {
            running = true;
            dispatcher = Thread.ofVirtual().name("dispatcher").start(() -> {
                while (running || !queue.isEmpty()) {
                    try {
                        Runnable task = queue.poll(50, TimeUnit.MILLISECONDS);
                        if (task == null) continue;

                        semaphore.acquire(); // bloquea si ya hay maxConcurrent activos
                        int cur = active.incrementAndGet();
                        maxActive.updateAndGet(prev -> Math.max(prev, cur));

                        Thread.ofVirtual().start(() -> {
                            try {
                                task.run();
                            } finally {
                                active.decrementAndGet();
                                completed.incrementAndGet();
                                semaphore.release();
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        void shutdown() {
            running = false;
        }

        void awaitTermination() throws InterruptedException {
            if (dispatcher != null) dispatcher.join();
        }

        int getCompleted() { return completed.get(); }
        int getMaxActive() { return maxActive.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== VirtualThreadPool con Semaphore ===");
        System.out.println("Max concurrentes: 5 | Tareas: 20 | Sleep por tarea: 100ms");
        System.out.println("-".repeat(50));

        int totalTareas = 20;
        int maxConcurrentes = 5;
        long taskDuration = 100;

        VirtualThreadPool pool = new VirtualThreadPool(maxConcurrentes);
        pool.start();

        long inicio = System.currentTimeMillis();

        for (int i = 1; i <= totalTareas; i++) {
            final int id = i;
            pool.submit(() -> {
                try {
                    System.out.printf("  [tarea-%02d] inicio  | activos: %d%n",
                            id, pool.active.get());
                    Thread.sleep(taskDuration);
                    System.out.printf("  [tarea-%02d] fin%n", id);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination();

        long total = System.currentTimeMillis() - inicio;

        System.out.println("-".repeat(50));
        System.out.println();
        System.out.printf("Tareas completadas : %d/%d%n", pool.getCompleted(), totalTareas);
        System.out.printf("Max activos simult.: %d (limite: %d)%n",
                pool.getMaxActive(), maxConcurrentes);
        System.out.printf("Tiempo total       : %d ms%n", total);
        System.out.printf("Throughput         : %.1f tareas/seg%n",
                totalTareas * 1000.0 / total);
        System.out.println();

        // Con maxConcurrentes=5 y 20 tareas de 100ms, esperamos ~4 rondas = ~400ms
        // Si fueran sin limite: todas concurrentes = ~100ms
        long tiempoSinLimite = taskDuration;
        long tiempoTeorico = (long) Math.ceil((double) totalTareas / maxConcurrentes) * taskDuration;
        System.out.printf("Tiempo sin limite  : ~%d ms%n", tiempoSinLimite);
        System.out.printf("Tiempo con limite=5: ~%d ms (teorico)%n", tiempoTeorico);
        System.out.println();
        System.out.println("El Semaphore garantiza que nunca haya mas de "
                + maxConcurrentes + " VTs activos.");
        System.out.println("Util para limitar concurrencia en accesos a BD, APIs externas, etc.");

        // Verificacion de invariante
        boolean ok = pool.getMaxActive() <= maxConcurrentes;
        System.out.println("Invariante cumplida (max_activos <= " + maxConcurrentes + "): " + ok);
    }
}
