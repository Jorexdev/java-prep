import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Ejercicio2 {

    static class MaxAttemptsExceededException extends RuntimeException {
        MaxAttemptsExceededException(String msg) { super(msg); }
    }

    static class RetryPolicy {
        private final int maxAttempts;
        private final long initialDelayMs;
        private final double multiplier;

        RetryPolicy(int maxAttempts, long initialDelayMs, double multiplier) {
            this.maxAttempts = maxAttempts;
            this.initialDelayMs = initialDelayMs;
            this.multiplier = multiplier;
        }

        <T> T execute(Supplier<T> action) {
            long delay = initialDelayMs;
            long accumulated = 0;
            Exception last = null;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    System.out.printf("Intento %d (delay acumulado: %dms)%n", attempt, accumulated);
                    T result = action.get();
                    System.out.println("Éxito en intento " + attempt);
                    return result;
                } catch (Exception e) {
                    last = e;
                    System.out.println("  Fallo: " + e.getMessage());
                    if (attempt < maxAttempts) {
                        accumulated += delay;
                        delay = (long) (delay * multiplier);
                    }
                }
            }
            throw new MaxAttemptsExceededException(
                "Agotados " + maxAttempts + " intentos. Último error: " + last.getMessage());
        }
    }

    interface RemoteService {
        String call();
    }

    static class UnstableService implements RemoteService {
        private int callCount = 0;

        @Override
        public String call() {
            callCount++;
            if (callCount <= 2) throw new RuntimeException("Error transitorio (intento " + callCount + ")");
            return "Respuesta exitosa";
        }
    }

    public static void main(String[] args) {
        RetryPolicy policy = new RetryPolicy(4, 100, 2.0);
        RemoteService service = new UnstableService();

        try {
            String result = policy.execute(service::call);
            System.out.println("Resultado: " + result);
        } catch (MaxAttemptsExceededException e) {
            System.out.println("Error final: " + e.getMessage());
        }

        System.out.println("\n--- Servicio que siempre falla ---");
        RetryPolicy strictPolicy = new RetryPolicy(3, 50, 1.5);
        try {
            strictPolicy.execute(() -> { throw new RuntimeException("Sin disponibilidad"); });
        } catch (MaxAttemptsExceededException e) {
            System.out.println("Capturado: " + e.getMessage());
        }
    }
}
