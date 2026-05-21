import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Ejercicio2 {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 10_000;
        int maxConcurrentes = 100;
        int workMs = 5;

        Semaphore semaphore = new Semaphore(maxConcurrentes);
        AtomicInteger activos = new AtomicInteger(0);
        AtomicInteger maxActivos = new AtomicInteger(0);
        AtomicInteger completados = new AtomicInteger(0);

        System.out.println("=== Semaphore Throttling con Virtual Threads ===");
        System.out.printf("Total enviados: %,d | Max concurrentes: %d | Work: %dms%n%n",
                          numThreads, maxConcurrentes, workMs);

        List<Thread> threads = new ArrayList<>(numThreads);
        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            Thread t = Thread.ofVirtual().start(() -> {
                try {
                    semaphore.acquire(); // bloquea si ya hay 100 activos
                    int current = activos.incrementAndGet();

                    // Actualizar maximo atomicamente
                    AtomicLong maxGuard = new AtomicLong(current);
                    maxActivos.updateAndGet(prev -> Math.max(prev, current));

                    Thread.sleep(workMs); // trabajo simulado

                    activos.decrementAndGet();
                    completados.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release();
                }
            });
            threads.add(t);
        }

        for (Thread t : threads) t.join();

        long elapsed = System.currentTimeMillis() - start;
        double throughput = numThreads / (elapsed / 1000.0);

        System.out.println("=== Resultados ===");
        System.out.printf("Completados      : %,d%n", completados.get());
        System.out.printf("Max activos      : %d (limite: %d) %s%n",
                          maxActivos.get(), maxConcurrentes,
                          maxActivos.get() <= maxConcurrentes ? "[OK]" : "[EXCEDIDO]");
        System.out.printf("Tiempo total     : %dms%n", elapsed);
        System.out.printf("Throughput       : %,.0f req/seg%n", throughput);
        System.out.println();
        long teorico = (long) Math.ceil((double) numThreads / maxConcurrentes) * workMs;
        System.out.println("Tiempo teorico ~= " + teorico + "ms (" +
                           numThreads + "/" + maxConcurrentes + " batches x " + workMs + "ms)");
        System.out.println();
        System.out.println("Semaphore permite controlar la presion sobre recursos externos");
        System.out.println("(ej: conexiones a BD, APIs con rate limit) sin rechazar peticiones.");
    }
}
