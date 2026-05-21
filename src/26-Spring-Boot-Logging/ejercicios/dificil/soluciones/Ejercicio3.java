import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio3 {

    enum Level { TRACE, DEBUG, INFO, WARN, ERROR }

    record LogEntry(Level level, long timestampMs, String message) {}

    static class ErrorBudgetLogger {
        private final Deque<LogEntry> window = new ArrayDeque<>();
        private final long windowMs;
        private final double alertThreshold; // 0.05 = 5%
        private int totalLogged = 0;
        private int alertsFired = 0;

        ErrorBudgetLogger(long windowMs, double alertThreshold) {
            this.windowMs = windowMs;
            this.alertThreshold = alertThreshold;
        }

        synchronized void log(Level level, String message) {
            long now = System.currentTimeMillis();
            evictExpired(now);

            window.add(new LogEntry(level, now, message));
            totalLogged++;

            double errorPercent = computeErrorPercent();
            String prefix = switch (level) {
                case ERROR -> "[ERROR]";
                case WARN  -> "[WARN ]";
                case INFO  -> "[INFO ]";
                default    -> "[DEBUG]";
            };
            System.out.printf("  %s %s  (window: %d entries, %.1f%% ERROR)%n",
                prefix, message, window.size(), errorPercent * 100);

            if (errorPercent > alertThreshold) {
                alertsFired++;
                System.out.printf("  [ALERT] Error budget exceeded: %.1f%% (threshold: %.0f%%)%n",
                    errorPercent * 100, alertThreshold * 100);
            }
        }

        private void evictExpired(long now) {
            while (!window.isEmpty() && (now - window.peek().timestampMs()) > windowMs) {
                window.poll();
            }
        }

        private double computeErrorPercent() {
            if (window.isEmpty()) return 0;
            long errors = window.stream().filter(e -> e.level() == Level.ERROR).count();
            return (double) errors / window.size();
        }

        void printSummary() {
            System.out.println("\n  Total logged: " + totalLogged
                + ", Alerts fired: " + alertsFired);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Ventana de 2 segundos para demo (vs 60s en producción)
        ErrorBudgetLogger logger = new ErrorBudgetLogger(2000, 0.05);

        System.out.println("=== Error Budget Logger (ventana 2s, umbral 5%) ===\n");

        System.out.println("--- Operación normal (< 5% errors) ---");
        logger.log(Level.INFO,  "Request procesado OK");
        logger.log(Level.INFO,  "Request procesado OK");
        logger.log(Level.INFO,  "Request procesado OK");
        logger.log(Level.WARN,  "Timeout leve en DB");
        logger.log(Level.INFO,  "Request procesado OK");

        System.out.println("\n--- Burst de errores (supera el 5%) ---");
        logger.log(Level.ERROR, "NullPointerException en ServicioPagos");
        logger.log(Level.ERROR, "Connection refused a base de datos");
        logger.log(Level.INFO,  "Retry exitoso");
        logger.log(Level.ERROR, "Timeout en servicio externo");
        logger.log(Level.ERROR, "Circuit breaker abierto");

        System.out.println("\n--- Esperar a que caduque la ventana ---");
        Thread.sleep(2100);
        logger.log(Level.INFO, "Sistema recuperado");
        logger.log(Level.INFO, "Todo OK de nuevo");

        logger.printSummary();
    }
}
