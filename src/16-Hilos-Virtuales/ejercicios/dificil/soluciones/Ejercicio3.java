import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Ejercicio3 {

    record Resultado(String nombre, long ms) {}

    static Resultado medirExecutor(String nombre, ExecutorService executor, int tareas, int sleepMs)
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
        executor.shutdown();
        long ms = System.currentTimeMillis() - start;
        return new Resultado(nombre, ms);
    }

    public static void main(String[] args) throws Exception {
        int tareas = 500;
        int sleepMs = 50;

        System.out.println("=== Virtual vs Platform: Benchmark Completo ===");
        System.out.printf("Tareas: %d, I/O por tarea: %dms, carga total: %,dms%n%n",
                          tareas, sleepMs, (long) tareas * sleepMs);

        // Warm up
        medirExecutor("warmup", Executors.newFixedThreadPool(10), 20, 5);
        medirExecutor("warmup", Executors.newVirtualThreadPerTaskExecutor(), 20, 5);

        List<Resultado> resultados = new ArrayList<>();
        resultados.add(medirExecutor("Fixed(10)",   Executors.newFixedThreadPool(10),   tareas, sleepMs));
        resultados.add(medirExecutor("Fixed(100)",  Executors.newFixedThreadPool(100),  tareas, sleepMs));
        resultados.add(medirExecutor("Cached",      Executors.newCachedThreadPool(),     tareas, sleepMs));
        resultados.add(medirExecutor("Virtual",     Executors.newVirtualThreadPerTaskExecutor(), tareas, sleepMs));

        System.out.printf("%-15s %10s %15s %15s%n", "Implementacion", "Tiempo", "Throughput", "vs Fixed(10)");
        System.out.println("-".repeat(57));

        long fixedMs = resultados.get(0).ms();
        for (Resultado r : resultados) {
            double throughput = tareas / (r.ms() / 1000.0);
            System.out.printf("%-15s %8dms %12.0f t/s %12.1fx%n",
                              r.nombre(), r.ms(), throughput, (double) fixedMs / r.ms());
        }

        System.out.println();
        System.out.println("=== Analisis: cuando usar cada uno ===");
        System.out.println();
        System.out.println("Fixed(10)   : control estricto de recursos. Para tareas CPU-bound");
        System.out.println("              o cuando el recurso externo admite pocos concurrentes.");
        System.out.println();
        System.out.println("Fixed(100)  : mas paralelismo I/O. Pero sigue limitado y consume");
        System.out.println("              ~100MB de stack OS. Numeros magicos en la config.");
        System.out.println();
        System.out.println("Cached      : crea platform threads bajo demanda. Rapido para picos");
        System.out.println("              cortos, pero puede crear miles de OS threads y agotar");
        System.out.println("              memoria. Riesgo de thread explosion.");
        System.out.println();
        System.out.println("Virtual     : el mejor para I/O-bound moderno. Sin limite artificial,");
        System.out.println("              memoria minima (~KB por thread), maxima concurrencia.");
        System.out.println("              No usar para CPU-bound (no mejora) ni con synchronized");
        System.out.println("              + I/O (pinning). Usar con ReentrantLock en su lugar.");
    }
}
