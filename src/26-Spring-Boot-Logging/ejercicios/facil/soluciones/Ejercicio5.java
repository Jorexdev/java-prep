// Ejercicio 5 (Fácil) — Performance logging
// timedRun(name, runnable) → "[PERF] nombre: Xms"
public class Ejercicio5 {

    static class PerfLogger {

        /**
         * Ejecuta el Runnable y loguea "[PERF] name: Xms"
         */
        public static void timedRun(String name, Runnable r) {
            long start = System.currentTimeMillis();
            try {
                r.run();
            } finally {
                long elapsed = System.currentTimeMillis() - start;
                System.out.printf("[PERF] %-35s : %dms%n", name, elapsed);
            }
        }

        /**
         * Versión con umbral de alerta: si supera el umbral, imprime SLOW
         */
        public static void timedRun(String name, long warnThresholdMs, Runnable r) {
            long start = System.currentTimeMillis();
            try {
                r.run();
            } finally {
                long elapsed = System.currentTimeMillis() - start;
                String suffix = elapsed > warnThresholdMs ? " [SLOW!]" : "";
                System.out.printf("[PERF] %-35s : %dms%s%n", name, elapsed, suffix);
            }
        }
    }

    // Operación rápida (computación en memoria)
    static int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    // Operación media (simulada con Thread.sleep)
    static String fetchFromCache(String key) {
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return "cached-value-for-" + key;
    }

    // Operación lenta (simulada con Thread.sleep)
    static void saveToDatabase(String data) {
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) {
        System.out.println("=== Performance logging ===");
        System.out.println();

        // Operación 1: rápida (< 1ms)
        PerfLogger.timedRun("fibonacci(30)", () -> {
            int result = fibonacci(30);
            System.out.println("  fibonacci(30) = " + result);
        });

        // Operación 2: media (~50ms)
        PerfLogger.timedRun("fetchFromCache('user:42')", () -> {
            String val = fetchFromCache("user:42");
            System.out.println("  cache result = " + val);
        });

        // Operación 3: lenta (~200ms)
        PerfLogger.timedRun("saveToDatabase(order#1234)", () -> {
            saveToDatabase("{orderId: 1234, total: 99.99}");
            System.out.println("  Guardado OK");
        });

        System.out.println();
        System.out.println("--- Con umbral de alerta (50ms) ---");

        PerfLogger.timedRun("fibonacci(35)", 50, () -> fibonacci(35));
        PerfLogger.timedRun("fetchFromCache('product:99')", 50, () -> fetchFromCache("product:99"));
        PerfLogger.timedRun("saveToDatabase(invoice#5678)", 50, () -> saveToDatabase("data"));

        System.out.println();
        System.out.println("--- Operación que lanza excepción ---");
        try {
            PerfLogger.timedRun("failingOperation", () -> {
                System.out.println("  Ejecutando operación que falla...");
                throw new RuntimeException("Error simulado");
            });
        } catch (RuntimeException e) {
            System.out.println("Excepción capturada: " + e.getMessage());
            System.out.println("El tiempo se logueó igualmente (try-finally)");
        }
    }
}
