import java.util.function.Supplier;

// Retry con backoff exponencial: falla 3 veces, tiene éxito a la 4ª
public class Ejercicio4 {

    // Servicio que falla N veces antes de responder
    static class ServicioExterno {
        private int intentos = 0;
        private final int fallosAntes;

        ServicioExterno(int fallosAntes) {
            this.fallosAntes = fallosAntes;
        }

        String llamar() throws Exception {
            intentos++;
            if (intentos <= fallosAntes) {
                throw new RuntimeException("Error 503 - Servicio no disponible (intento " + intentos + ")");
            }
            return "OK - datos del servicio (intento " + intentos + ")";
        }

        void reset() { intentos = 0; }
    }

    // Retry con backoff exponencial
    static <T> T retryWithBackoff(Supplier<T> operation, int maxRetries, long initialDelayMs) throws Exception {
        long delay = initialDelayMs;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                System.out.println("  Intento " + attempt + "/" + (maxRetries + 1) + "...");
                T result = operation.get();
                System.out.println("  Éxito en intento " + attempt + ": " + result);
                return result;
            } catch (Exception e) {
                lastException = e;
                System.out.println("  Fallo: " + e.getMessage());

                if (attempt <= maxRetries) {
                    System.out.println("  Esperando " + delay + "ms antes del reintento...");
                    Thread.sleep(delay);
                    delay *= 2; // backoff exponencial: 100ms → 200ms → 400ms
                }
            }
        }

        throw new Exception("Agotados " + maxRetries + " reintentos. Último error: " + lastException.getMessage(), lastException);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Retry con backoff exponencial ===\n");

        ServicioExterno servicio = new ServicioExterno(3);

        System.out.println("Caso 1: servicio falla 3 veces, éxito a la 4ª (maxRetries=5)");
        System.out.println("Backoff: 100ms → 200ms → 400ms\n");

        try {
            String resultado = retryWithBackoff(
                () -> {
                    try { return servicio.llamar(); }
                    catch (Exception e) { throw new RuntimeException(e.getMessage()); }
                },
                5,
                100
            );
            System.out.println("\nResultado final: " + resultado);
        } catch (Exception e) {
            System.out.println("\nError irrecuperable: " + e.getMessage());
        }

        System.out.println();
        System.out.println("---");
        System.out.println();

        // Caso 2: se agotan los reintentos
        ServicioExterno servicioRoto = new ServicioExterno(100); // siempre falla
        System.out.println("Caso 2: servicio siempre falla (maxRetries=2)");

        try {
            retryWithBackoff(
                () -> {
                    try { return servicioRoto.llamar(); }
                    catch (Exception e) { throw new RuntimeException(e.getMessage()); }
                },
                2,
                100
            );
        } catch (Exception e) {
            System.out.println("\nError propagado correctamente: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== En Project Reactor ===");
        System.out.println("Mono.fromCallable(() -> servicio.llamar())");
        System.out.println("    .retryWhen(Retry.backoff(5, Duration.ofMillis(100))");
        System.out.println("                   .maxBackoff(Duration.ofSeconds(5)))");
        System.out.println("    .subscribe(...);");
    }
}
