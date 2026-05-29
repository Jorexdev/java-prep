import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

// Rate limiter reactivo: limita el flujo a N elementos/segundo con backpressure
// Implementación con token bucket: se acumulan N tokens/segundo; cada item consume 1 token
public class Ejercicio3 {

    // ======================= TOKEN BUCKET RATE LIMITER =======================
    static class TokenBucketRateLimiter {
        private final int tokensPerSecond;
        private final Semaphore tokens;
        private volatile boolean running = true;

        TokenBucketRateLimiter(int tokensPerSecond) {
            this.tokensPerSecond = tokensPerSecond;
            this.tokens = new Semaphore(0); // empieza vacío

            // Hilo que añade N tokens cada segundo (rellena el bucket)
            Thread refiller = Thread.ofVirtual().start(() -> {
                while (running) {
                    try {
                        Thread.sleep(1000 / tokensPerSecond); // distribuir tokens uniformemente
                        tokens.release(1); // añadir 1 token
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // Adquirir un token (bloquea si no hay tokens disponibles → backpressure)
        void acquire() throws InterruptedException {
            tokens.acquire();
        }

        void stop() {
            running = false;
        }
    }

    // ======================= PIPELINE CON RATE LIMITING =======================
    static class RateLimitedPipeline {
        private final TokenBucketRateLimiter limiter;

        RateLimitedPipeline(int ratePerSecond) {
            this.limiter = new TokenBucketRateLimiter(ratePerSecond);
        }

        void process(Iterable<Integer> items, Consumer<Integer> handler) throws InterruptedException {
            for (Integer item : items) {
                limiter.acquire(); // esperar token → backpressure

                long ts = System.currentTimeMillis();
                System.out.printf("  [t=%dms] Item %2d procesado%n", ts % 100000, item);
                handler.accept(item);
            }
            limiter.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        int totalEventos   = 20;
        int ratePerSecond  = 5;

        System.out.println("=== Rate Limiter Reactivo ===\n");
        System.out.println("Eventos a procesar: " + totalEventos);
        System.out.println("Rate limit: " + ratePerSecond + " eventos/segundo");
        System.out.println("Tiempo esperado: ~" + (totalEventos / ratePerSecond) + " segundos\n");

        RateLimitedPipeline pipeline = new RateLimitedPipeline(ratePerSecond);

        long start = System.currentTimeMillis();
        AtomicLong processed = new AtomicLong(0);

        // Emitir 20 eventos tan rápido como sea posible — el rate limiter aplica backpressure
        java.util.List<Integer> eventos = new java.util.ArrayList<>();
        for (int i = 1; i <= totalEventos; i++) eventos.add(i);

        pipeline.process(eventos, item -> processed.incrementAndGet());

        long elapsed = System.currentTimeMillis() - start;

        System.out.println();
        System.out.println("=== Resultados ===");
        System.out.printf("Procesados:  %d/%d%n", processed.get(), totalEventos);
        System.out.printf("Tiempo real: %dms%n", elapsed);
        System.out.printf("Rate real:   %.1f eventos/s%n", processed.get() / (elapsed / 1000.0));
        System.out.println();
        System.out.println("=== Backpressure ===");
        System.out.println("El producer quería emitir 20 eventos instantáneamente.");
        System.out.println("El rate limiter aplicó backpressure: acquire() bloqueó hasta tener token.");
        System.out.println("Resultado: el flujo respetó el límite de " + ratePerSecond + "/s.");
        System.out.println();
        System.out.println("=== En Project Reactor ===");
        System.out.println("Flux.range(1, 20)");
        System.out.println("    .delayElements(Duration.ofMillis(200))  // 5/s = 1 cada 200ms");
        System.out.println("    .subscribe(System.out::println);");
    }
}
