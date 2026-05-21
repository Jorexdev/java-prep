import java.util.concurrent.atomic.AtomicLong;

// Ejercicio 3 (Medio) — Log sampling
// SamplingLogger(rate) loguea ~rate*100% de los mensajes
public class Ejercicio3 {

    enum Level { TRACE, DEBUG, INFO, WARN, ERROR }

    static class SamplingLogger {
        private final String name;
        private final double rate;
        private Level minLevel;

        // Contadores para estadísticas
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong loggedCount = new AtomicLong(0);

        SamplingLogger(String name, double rate, Level minLevel) {
            this.name = name;
            this.rate = rate;
            this.minLevel = minLevel;
        }

        /**
         * Solo loguea el mensaje si:
         * 1. El nivel es >= minLevel
         * 2. Math.random() < rate
         */
        private void log(Level level, String message) {
            if (level.ordinal() < minLevel.ordinal()) return;

            totalCalls.incrementAndGet();

            if (Math.random() < rate) {
                loggedCount.incrementAndGet();
                System.out.printf("[%-5s] [sample] %s - %s%n", level.name(), name, message);
            }
        }

        public void trace(String msg) { log(Level.TRACE, msg); }
        public void debug(String msg) { log(Level.DEBUG, msg); }
        public void info(String msg)  { log(Level.INFO, msg); }
        public void warn(String msg)  { log(Level.WARN, msg); }
        public void error(String msg) { log(Level.ERROR, msg); }

        public long getTotalCalls()   { return totalCalls.get(); }
        public long getLoggedCount()  { return loggedCount.get(); }
        public double getActualRate() {
            long total = totalCalls.get();
            return total == 0 ? 0.0 : (double) loggedCount.get() / total;
        }

        public void printStats() {
            System.out.printf("Estadísticas: total=%d, logueados=%d, rate=%.1f%% (config=%.0f%%)%n",
                totalCalls.get(), loggedCount.get(),
                getActualRate() * 100, rate * 100);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Log sampling ===");
        System.out.println();

        // Sampling al 10%
        SamplingLogger logger10 = new SamplingLogger("com.app.MetricsService", 0.10, Level.INFO);

        System.out.println("Llamando info() 1000 veces con rate=10%...");
        System.out.println("(se muestran los primeros mensajes logueados)");
        System.out.println();

        int shownMessages = 0;
        for (int i = 0; i < 1000; i++) {
            long before = logger10.getLoggedCount();
            logger10.info("Request #" + (i + 1) + " procesado");
            if (logger10.getLoggedCount() > before && shownMessages < 5) {
                shownMessages++; // Los primeros 5 ya se imprimieron en log()
            }
        }

        System.out.println();
        logger10.printStats();
        System.out.printf("Rango esperado: 80-120 (10%% de 1000)%n");
        System.out.printf("Resultado dentro del rango: %b%n",
            logger10.getLoggedCount() >= 50 && logger10.getLoggedCount() <= 150);

        System.out.println();
        System.out.println("=== Comparando distintas tasas de muestreo ===");

        int[] calls = {100, 1000, 10000};
        double[] rates = {0.01, 0.05, 0.10, 0.25, 0.50, 1.0};

        System.out.printf("%-10s %-8s %-12s %-12s %-10s%n",
            "RATE", "CALLS", "LOGUEADOS", "ESPERADO", "REAL %");
        System.out.println("-".repeat(55));

        for (double rate : rates) {
            SamplingLogger sl = new SamplingLogger("test", rate, Level.INFO);
            for (int i = 0; i < 1000; i++) sl.info("msg");
            System.out.printf("%-10.0f%% %-8d %-12d %-12d %-10.1f%%%n",
                rate * 100,
                sl.getTotalCalls(),
                sl.getLoggedCount(),
                (long)(sl.getTotalCalls() * rate),
                sl.getActualRate() * 100);
        }

        System.out.println();
        System.out.println("Uso típico: rate=0.01 en producción para métricas de alta frecuencia");
        System.out.println("preserva ~1% del volumen de logs → reduce storage y costes.");
    }
}
