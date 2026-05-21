import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

// Ejercicio 2 (Difícil) — Log aggregator
// Recibe logs de 3 fuentes, procesa en batches cada 200ms
public class Ejercicio2 {

    enum Level { DEBUG, INFO, WARN, ERROR }

    record LogEntry(String source, Level level, String message) {}

    static class LogAggregator {
        private final BlockingQueue<LogEntry> buffer = new LinkedBlockingQueue<>();
        private final AtomicInteger batchCount = new AtomicInteger(0);
        private final AtomicInteger totalProcessed = new AtomicInteger(0);
        private volatile boolean running = true;

        /** Thread que procesa el buffer en batches cada 200ms */
        private final Thread batchThread = new Thread(() -> {
            while (running || !buffer.isEmpty()) {
                try {
                    Thread.sleep(200); // intervalo de batch
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                flushBatch();
            }
        }, "log-aggregator");

        public void start() {
            batchThread.setDaemon(true);
            batchThread.start();
        }

        public void submit(LogEntry entry) {
            buffer.offer(entry);
        }

        private void flushBatch() {
            if (buffer.isEmpty()) return;

            List<LogEntry> batch = new ArrayList<>();
            buffer.drainTo(batch);

            if (batch.isEmpty()) return;

            int n = batchCount.incrementAndGet();
            totalProcessed.addAndGet(batch.size());

            System.out.printf("%n=== Batch #%d: %d events ===%n", n, batch.size());
            for (LogEntry e : batch) {
                System.out.printf("  [%-5s] [%s] %s%n",
                    e.level().name(), e.source(), e.message());
            }
        }

        public void shutdown() throws InterruptedException {
            running = false;
            batchThread.join(3000);
            flushBatch(); // flush final
        }

        public int getTotalProcessed() { return totalProcessed.get(); }
        public int getBatchCount() { return batchCount.get(); }
    }

    // Simula un logger que envía al aggregator
    static class SourceLogger {
        private final String name;
        private final LogAggregator aggregator;

        SourceLogger(String name, LogAggregator aggregator) {
            this.name = name;
            this.aggregator = aggregator;
        }

        public void info(String msg)  { aggregator.submit(new LogEntry(name, Level.INFO, msg)); }
        public void debug(String msg) { aggregator.submit(new LogEntry(name, Level.DEBUG, msg)); }
        public void warn(String msg)  { aggregator.submit(new LogEntry(name, Level.WARN, msg)); }
        public void error(String msg) { aggregator.submit(new LogEntry(name, Level.ERROR, msg)); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Log aggregator (batch cada 200ms) ===");
        System.out.println();

        LogAggregator aggregator = new LogAggregator();
        aggregator.start();

        SourceLogger serviceA = new SourceLogger("ServiceA", aggregator);
        SourceLogger serviceB = new SourceLogger("ServiceB", aggregator);
        SourceLogger serviceC = new SourceLogger("ServiceC", aggregator);

        System.out.println("Enviando 100 eventos desde 3 fuentes...");

        // Enviar eventos en oleadas con pausas
        // Oleada 1: 30 eventos
        for (int i = 1; i <= 10; i++) {
            serviceA.info("Request A#" + i);
            serviceB.debug("Procesando B#" + i);
            serviceC.info("Task C#" + i);
        }
        System.out.println("Oleada 1 enviada (30 eventos). Esperando primer batch...");
        Thread.sleep(250); // esperar primer batch

        // Oleada 2: 30 eventos
        for (int i = 11; i <= 20; i++) {
            serviceA.warn("Latencia alta A#" + i);
            serviceB.info("Response B#" + i);
            serviceC.error("Error en C#" + i);
        }
        System.out.println("Oleada 2 enviada (30 eventos). Esperando segundo batch...");
        Thread.sleep(250);

        // Oleada 3: 40 eventos (irregular)
        for (int i = 21; i <= 40; i++) {
            if (i % 3 == 0) serviceA.error("Error crítico A#" + i);
            else if (i % 3 == 1) serviceB.info("Completado B#" + i);
            else serviceC.debug("Debug C#" + i);
        }
        System.out.println("Oleada 3 enviada (40 eventos). Esperando tercer batch...");
        Thread.sleep(250);

        aggregator.shutdown();

        System.out.println();
        System.out.println("=== Resumen del aggregator ===");
        System.out.printf("Total eventos procesados: %d%n", aggregator.getTotalProcessed());
        System.out.printf("Número de batches:        %d%n", aggregator.getBatchCount());
        System.out.printf("Promedio por batch:       %.1f%n",
            (double) aggregator.getTotalProcessed() / Math.max(1, aggregator.getBatchCount()));
    }
}
