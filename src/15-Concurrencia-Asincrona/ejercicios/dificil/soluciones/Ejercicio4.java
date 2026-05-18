import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio4 {
    static final int MAX_POR_SEGUNDO = 5;
    static final Semaphore semaforo = new Semaphore(MAX_POR_SEGUNDO);
    static final AtomicInteger procesadasEsteSeg = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        ScheduledExecutorService reponedor = Executors.newSingleThreadScheduledExecutor();
        reponedor.scheduleAtFixedRate(() -> {
            int procesadas = procesadasEsteSeg.getAndSet(0);
            System.out.println("[Rate limiter] Procesadas en el último segundo: " + procesadas + " — reponiendo permisos");
            semaforo.release(MAX_POR_SEGUNDO - semaforo.availablePermits());
        }, 1, 1, TimeUnit.SECONDS);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 1; i <= 20; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    semaforo.acquire();
                    int n = procesadasEsteSeg.incrementAndGet();
                    System.out.println("Procesando tarea-" + id + " (en segundo actual: " + n + ")");
                    Thread.sleep(200);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        reponedor.shutdown();
    }
}
