import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio1 {
    static final AtomicInteger fallosConsecutivos = new AtomicInteger(0);
    static final int MAX_FALLOS = 3;

    static <T> CompletableFuture<T> conCircuitBreaker(Callable<T> tarea, ExecutorService exec) {
        if (fallosConsecutivos.get() >= MAX_FALLOS) {
            CompletableFuture<T> f = new CompletableFuture<>();
            f.completeExceptionally(new RuntimeException("Circuit OPEN"));
            return f;
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                T r = tarea.call();
                fallosConsecutivos.set(0);
                return r;
            } catch (Exception e) {
                fallosConsecutivos.incrementAndGet();
                throw new RuntimeException(e);
            }
        }, exec);
    }

    public static void main(String[] args) throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(4);
        AtomicInteger contador = new AtomicInteger(0);

        for (int i = 0; i < 8; i++) {
            final int id = i;
            try {
                String r = conCircuitBreaker(() -> {
                    if (contador.incrementAndGet() <= 3) throw new RuntimeException("Fallo");
                    return "OK-" + id;
                }, exec).get();
                System.out.println("Tarea-" + id + ": " + r);
            } catch (Exception e) {
                System.out.println("Tarea-" + id + ": " + e.getCause().getMessage() + " (fallos=" + fallosConsecutivos.get() + ")");
            }
        }
        exec.shutdown();
    }
}
