import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Ejercicio1 {

    static long medirPool(ExecutorService executor, int tareas, int sleepMs) throws Exception {
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
        executor.shutdown();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws Exception {
        int tareas = 100;
        int sleepMs = 20;

        System.out.println("=== I/O-bound Benchmark: Virtual vs Fixed Pool ===");
        System.out.printf("Tareas: %d, I/O simulado por tarea: %dms%n%n", tareas, sleepMs);

        // Warm up
        medirPool(Executors.newFixedThreadPool(10), 20, 5);
        medirPool(Executors.newVirtualThreadPerTaskExecutor(), 20, 5);

        long fixedMs = medirPool(Executors.newFixedThreadPool(10), tareas, sleepMs);
        long virtualMs = medirPool(Executors.newVirtualThreadPerTaskExecutor(), tareas, sleepMs);

        double fixedThroughput = tareas / (fixedMs / 1000.0);
        double virtualThroughput = tareas / (virtualMs / 1000.0);

        System.out.println("=== Resultados ===");
        System.out.printf("%-30s %8s %15s%n", "Implementacion", "Tiempo", "Throughput");
        System.out.println("-".repeat(55));
        System.out.printf("%-30s %6dms %12.1f t/s%n", "Fixed pool (10 threads)", fixedMs, fixedThroughput);
        System.out.printf("%-30s %6dms %12.1f t/s%n", "Virtual per task", virtualMs, virtualThroughput);
        System.out.println();

        long teorico10 = (long) Math.ceil((double) tareas / 10) * sleepMs;
        System.out.println("Tiempo teorico fixed(10): ~" + teorico10 + "ms (10 batches de 10 x 20ms)");
        System.out.println("Tiempo teorico virtual  : ~" + sleepMs + "ms (100 en paralelo)");
        System.out.println();
        if (virtualMs > 0) {
            System.out.printf("Speedup: %.1fx%n", (double) fixedMs / virtualMs);
        }
        System.out.println("\nCon I/O-bound, virtual threads permiten maxima concurrencia sin OS threads.");
    }
}
