import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// CompletableFuture pipeline: timeout + retry con backoff + circuit breaker

public class Ejercicio5 {

    // ── Circuit Breaker ──────────────────────────────────────────────────────
    static class CircuitOpenException extends RuntimeException {
        CircuitOpenException(String msg) { super(msg); }
    }

    static class CircuitBreaker {
        private final int umbral;
        private final AtomicInteger fallosConsecutivos = new AtomicInteger(0);
        private volatile boolean abierto = false;

        CircuitBreaker(int umbral) { this.umbral = umbral; }

        public void registrarExito() {
            fallosConsecutivos.set(0);
            abierto = false;
        }

        public void registrarFallo() {
            int n = fallosConsecutivos.incrementAndGet();
            if (n >= umbral) {
                abierto = true;
            }
        }

        public boolean estaAbierto() { return abierto; }

        public String estado() {
            return abierto ? "ABIERTO (bloqueando)" : "CERRADO (operativo) fallos=" + fallosConsecutivos.get();
        }
    }

    // ── Operación simulada ───────────────────────────────────────────────────
    static final Random RNG = new Random(42);

    static CompletableFuture<String> fetchData(int id, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            // 40% de probabilidad de fallo
            if (RNG.nextDouble() < 0.40) {
                throw new RuntimeException("Error en fetchData(" + id + ")");
            }
            // Latencia simulada 50-150ms
            try { Thread.sleep(50 + RNG.nextInt(100)); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrumpido");
            }
            return "Dato-" + id;
        }, executor);
    }

    // ── Retry con backoff exponencial ────────────────────────────────────────
    static <T> CompletableFuture<T> withRetry(
            Supplier<CompletableFuture<T>> supplier,
            int intentosRestantes,
            long delayMs,
            ExecutorService executor,
            int id) {

        return supplier.get().thenApply(result -> {
            System.out.println("  [" + id + "] Exito en intento (quedan " + (intentosRestantes) + ")");
            return result;
        }).exceptionallyCompose(ex -> {
            if (intentosRestantes <= 1) {
                return CompletableFuture.failedFuture(ex);
            }
            System.out.printf("  [%d] Fallo → reintento en %dms (intentos restantes: %d)%n",
                              id, delayMs, intentosRestantes - 1);
            return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS))
                .thenCompose(ignored -> withRetry(supplier, intentosRestantes - 1, delayMs * 2, executor, id));
        });
    }

    // ── Timeout ─────────────────────────────────────────────────────────────
    static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> cf, long timeoutMs) {
        // orTimeout disponible desde Java 9
        return cf.orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
    }

    // ── Pipeline completo ────────────────────────────────────────────────────
    static CompletableFuture<String> pipeline(int id, ExecutorService executor, CircuitBreaker cb) {
        if (cb.estaAbierto()) {
            System.out.printf("  [%2d] Circuit ABIERTO — rechazado sin ejecutar%n", id);
            return CompletableFuture.failedFuture(
                new CircuitOpenException("Circuit breaker abierto para id=" + id));
        }

        CompletableFuture<String> cf = withRetry(
            () -> fetchData(id, executor),
            3,       // max 3 intentos
            50,      // primer delay 50ms → 100ms → 200ms
            executor,
            id
        );

        cf = withTimeout(cf, 800); // timeout de 800ms

        return cf.whenComplete((resultado, ex) -> {
            if (ex == null) {
                cb.registrarExito();
            } else {
                cb.registrarFallo();
                String causa = ex.getCause() != null ? ex.getCause().getClass().getSimpleName()
                                                     : ex.getClass().getSimpleName();
                System.out.printf("  [%2d] FALLO definitivo: %s — CB estado: %s%n",
                                  id, causa, cb.estado());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== CompletableFuture pipeline: timeout + retry + circuit breaker ===\n");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CircuitBreaker cb = new CircuitBreaker(3); // abre al 3er fallo consecutivo

        List<CompletableFuture<String>> futuros = IntStream.rangeClosed(1, 10)
            .mapToObj(id -> pipeline(id, executor, cb)
                .handle((res, ex) -> {
                    if (ex != null) {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        if (causa instanceof CircuitOpenException) {
                            return "[" + id + "] CIRCUIT_OPEN";
                        } else if (causa instanceof TimeoutException) {
                            return "[" + id + "] TIMEOUT";
                        }
                        return "[" + id + "] ERROR: " + causa.getMessage();
                    }
                    return "[" + id + "] OK: " + res;
                }))
            .collect(Collectors.toList());

        // Esperar todos
        CompletableFuture<Void> todos = CompletableFuture.allOf(
            futuros.toArray(new CompletableFuture[0]));
        todos.get(15, TimeUnit.SECONDS);

        System.out.println("\n=== Resultados finales ===");
        futuros.forEach(f -> {
            try { System.out.println("  " + f.get()); } catch (Exception ignored) {}
        });

        System.out.println("\n=== Estado final del Circuit Breaker ===");
        System.out.println("  " + cb.estado());

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\n=== Componentes del pipeline ===");
        System.out.println("  fetchData()  : operacion asincrónica con 40% fallo, 50-150ms latencia");
        System.out.println("  withRetry()  : reintenta max 3 veces con backoff exponencial 50/100/200ms");
        System.out.println("  withTimeout(): orTimeout(800ms) — cancela si tarda demasiado (Java 9+)");
        System.out.println("  CircuitBreaker: se abre al 3er fallo consecutivo, se cierra con primer exito");
    }
}
