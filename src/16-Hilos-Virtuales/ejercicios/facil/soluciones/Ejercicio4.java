import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Ejercicio4 {

    static long medirExecutor(ExecutorService executor, int tareas, int sleepMs)
            throws Exception {
        List<Future<?>> futures = new ArrayList<>(tareas);
        long start = System.currentTimeMillis();
        for (int i = 0; i < tareas; i++) {
            futures.add(executor.submit(() -> {
                try { Thread.sleep(sleepMs); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        for (Future<?> f : futures) f.get();
        long elapsed = System.currentTimeMillis() - start;
        executor.shutdown();
        return elapsed;
    }

    public static void main(String[] args) throws Exception {
        int tareas = 200;
        int sleepMs = 10;

        System.out.println("=== ExecutorService: Virtual vs Fixed Pool ===");
        System.out.printf("Tareas: %d, sleep por tarea: %dms%n%n", tareas, sleepMs);

        // Fixed thread pool (10 threads)
        long fixedMs = medirExecutor(Executors.newFixedThreadPool(10), tareas, sleepMs);

        // Virtual thread per task executor
        long virtualMs = medirExecutor(Executors.newVirtualThreadPerTaskExecutor(), tareas, sleepMs);

        System.out.println("=== Resultados ===");
        System.out.printf("%-30s %8dms%n", "Fixed pool (10 threads):", fixedMs);
        System.out.printf("%-30s %8dms%n", "Virtual thread per task:", virtualMs);
        System.out.println();

        long teorico = (long) Math.ceil((double) tareas / 10) * sleepMs;
        System.out.println("Tiempo teorico fixed(10): ~" + teorico + "ms (" + tareas + "/" + 10 + " batches x " + sleepMs + "ms)");
        System.out.println("Tiempo teorico virtual  : ~" + sleepMs + "ms (todas en paralelo)");
        System.out.println();
        if (virtualMs > 0) {
            System.out.printf("Speedup virtual threads: %.1fx%n", (double) fixedMs / virtualMs);
        }
        System.out.println();
        System.out.println("newVirtualThreadPerTaskExecutor(): crea un nuevo virtual thread por cada submit().");
        System.out.println("No hay limite de concurrencia; perfecto para I/O-bound.");
    }
}
