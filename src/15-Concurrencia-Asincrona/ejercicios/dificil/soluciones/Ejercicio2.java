import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Ejercicio2 {
    static <T> CompletableFuture<T> conRetry(Supplier<CompletableFuture<T>> operacion, int maxIntentos) {
        return operacion.get().exceptionallyCompose(ex -> {
            if (maxIntentos <= 1) {
                CompletableFuture<T> f = new CompletableFuture<>();
                f.completeExceptionally(ex);
                return f;
            }
            long delay = 100L * (long)Math.pow(2, 3 - maxIntentos);
            System.out.println("Reintentando en " + delay + "ms (intentos restantes: " + (maxIntentos-1) + ")");
            return CompletableFuture.runAsync(() -> { try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } })
                .thenCompose(v -> conRetry(operacion, maxIntentos - 1));
        });
    }

    public static void main(String[] args) throws Exception {
        AtomicInteger intentos = new AtomicInteger(0);
        String resultado = conRetry(() -> CompletableFuture.supplyAsync(() -> {
            int n = intentos.incrementAndGet();
            System.out.println("Intento #" + n);
            if (n < 3) throw new RuntimeException("Servicio no disponible");
            return "Éxito en intento #" + n;
        }), 4).get();
        System.out.println("Resultado: " + resultado);
    }
}
