import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// Ejercicio 1 (Difícil) — Async logger
// LinkedBlockingQueue + daemon thread escritor; el hilo principal solo encola
public class Ejercicio1 {

    enum Level { TRACE, DEBUG, INFO, WARN, ERROR }

    record LogEvent(Level level, String logger, String message, long timestamp) {}

    static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    static class AsyncLogger {
        private final String name;
        private final LinkedBlockingQueue<LogEvent> queue;
        private Level minLevel;

        AsyncLogger(String name, LinkedBlockingQueue<LogEvent> queue, Level minLevel) {
            this.name = name;
            this.queue = queue;
            this.minLevel = minLevel;
        }

        /**
         * Encola el evento sin esperar a que sea escrito (no-blocking para el caller).
         */
        private void log(Level level, String message) {
            if (level.ordinal() < minLevel.ordinal()) return;

            LogEvent event = new LogEvent(level, name, message, System.currentTimeMillis());
            boolean offered = queue.offer(event); // no bloquea; descarta si cola llena
            if (!offered) {
                System.err.println("[AsyncLogger] Cola llena, evento descartado: " + message);
            }
        }

        public void trace(String msg) { log(Level.TRACE, msg); }
        public void debug(String msg) { log(Level.DEBUG, msg); }
        public void info(String msg)  { log(Level.INFO, msg); }
        public void warn(String msg)  { log(Level.WARN, msg); }
        public void error(String msg) { log(Level.ERROR, msg); }
    }

    /**
     * Thread escritor dedicado que consume la cola y escribe en stdout.
     * Es un daemon thread → no impide que la JVM termine.
     */
    static class WriterThread extends Thread {
        private final LinkedBlockingQueue<LogEvent> queue;
        private final AtomicLong written = new AtomicLong(0);
        private volatile boolean running = true;

        WriterThread(LinkedBlockingQueue<LogEvent> queue) {
            super("async-log-writer");
            this.queue = queue;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (running || !queue.isEmpty()) {
                try {
                    LogEvent event = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        write(event);
                        written.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        private void write(LogEvent e) {
            String time = LocalTime.now().format(TIME_FMT);
            System.out.printf("[ASYNC][%-5s] %s %s - %s%n",
                e.level().name(), time, e.logger(), e.message());
        }

        /** Detiene el writer tras vaciar la cola */
        public void shutdown() throws InterruptedException {
            running = false;
            join(5000); // esperar hasta 5s a que termine
        }

        public long getWrittenCount() { return written.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Async logger ===");
        System.out.println();

        LinkedBlockingQueue<LogEvent> queue = new LinkedBlockingQueue<>(1000);
        WriterThread writer = new WriterThread(queue);
        writer.start();

        AsyncLogger logger = new AsyncLogger("com.app.AsyncProcessor", queue, Level.DEBUG);

        System.out.println("--- Encolando 50 eventos desde el hilo principal ---");
        long enqueueStart = System.currentTimeMillis();

        for (int i = 1; i <= 50; i++) {
            Level level = switch (i % 5) {
                case 0 -> Level.WARN;
                case 1 -> Level.ERROR;
                default -> Level.INFO;
            };
            String msg = switch (level) {
                case INFO  -> "Procesando item #" + i;
                case WARN  -> "Latencia alta en item #" + i + ": 2500ms";
                case ERROR -> "Error procesando item #" + i;
                default    -> "msg #" + i;
            };

            if (level == Level.INFO)  logger.info(msg);
            else if (level == Level.WARN) logger.warn(msg);
            else logger.error(msg);
        }

        long enqueueEnd = System.currentTimeMillis();
        System.out.println();
        System.out.printf("Encolados 50 eventos en %dms (encolado es no-blocking)%n",
            enqueueEnd - enqueueStart);
        System.out.printf("Cola en este momento: %d eventos pendientes de escribir%n",
            queue.size());

        System.out.println();
        System.out.println("--- Esperando a que el writer termine de procesar... ---");

        // Dar tiempo al writer thread para procesar todos los eventos
        writer.shutdown();

        System.out.println();
        System.out.printf("Writer procesó %d eventos en total%n", writer.getWrittenCount());
        System.out.println("Cola vacía: " + queue.isEmpty());
        System.out.println();
        System.out.println("Ventaja del async logger:");
        System.out.println("  - El hilo principal no espera a que el disco/stdout responda");
        System.out.println("  - Reduce la latencia percibida por el usuario");
        System.out.println("  - Un solo thread de escritura evita contención en I/O");
    }
}
