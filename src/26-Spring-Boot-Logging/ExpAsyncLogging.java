import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;

// Logging asíncrono y propagación de contexto con virtual threads.
// En producción se usa Logback/AsyncAppender; aquí se simula el patrón con JUL.
public class ExpAsyncLogging {

    // ── 1. PROBLEMA DEL LOGGING SÍNCRONO ─────────────────────────────────────
    // Cada llamada a logger.info() escribe al disco de forma síncrona.
    // En un handler que escribe a fichero/red, esto añade latencia a cada request.
    // Con 1000 req/s y 5ms por write → 5 segundos de espera solo por logging.
    static void syncLoggingProblem() throws Exception {
        System.out.println("── 1. Logging síncrono — bloquea el hilo del request ──");

        Logger logger = Logger.getLogger("sync");
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        // Simular un handler que tarda (escribe a disco lento)
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord r) {
                try { Thread.sleep(2); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                // En real: escribe a fichero/socket
            }
            @Override public void flush() {}
            @Override public void close() {}
        });

        long inicio = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            logger.info("Request " + i + " procesado");
        }
        System.out.println("  10 logs síncronos en: " + (System.currentTimeMillis() - inicio) + "ms");
        System.out.println("  Cada log bloquea el hilo ~2ms → latencia añadida al request");
    }

    // ── 2. ASYNC APPENDER — patrón con BlockingQueue ─────────────────────────
    // Logback tiene AsyncAppender que hace exactamente esto:
    //   - El hilo del request pone el LogRecord en una BlockingQueue (< 1μs)
    //   - Un hilo dedicado drena la cola y escribe al destino real
    //
    // Si la cola se llena (queueSize), el comportamiento depende de la config:
    //   - discardingThreshold: descartar logs de DEBUG/INFO cuando la cola está al X%
    //   - neverBlock=true: nunca bloquear el hilo del request (puede perder logs)
    static class AsyncLogger {
        private final BlockingQueue<String> queue = new LinkedBlockingQueue<>(1000);
        private final AtomicLong dropped = new AtomicLong(0);
        private final Thread worker;

        AsyncLogger() {
            // Hilo dedicado para escritura real
            worker = Thread.ofVirtual().start(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String msg = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (msg != null) {
                            // En real: escribe a fichero/red
                            Thread.sleep(2); // simular latencia de escritura
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        // El caller no espera la escritura — regresa inmediatamente
        void log(String msg) {
            if (!queue.offer(msg)) {
                dropped.incrementAndGet(); // cola llena → descarte (configurable en Logback)
            }
        }

        void shutdown() throws InterruptedException {
            Thread.sleep(50); // dar tiempo a vaciar la cola
            worker.interrupt();
            worker.join(200);
        }

        long dropped() { return dropped.get(); }
    }

    static void asyncLoggingDemo() throws Exception {
        System.out.println("\n── 2. AsyncAppender — logging sin bloquear el request ──");

        AsyncLogger asyncLog = new AsyncLogger();

        long inicio = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            asyncLog.log("Request " + i + " procesado");
        }
        long elapsed = System.currentTimeMillis() - inicio;
        System.out.println("  10 logs asíncronos en: " + elapsed + "ms (casi 0 — no espera escritura)");
        System.out.println("  Logs descartados: " + asyncLog.dropped());

        asyncLog.shutdown();
        System.out.println();
        System.out.println("  Logback AsyncAppender config:");
        System.out.println("  <appender name=\"ASYNC\" class=\"ch.qos.logback.classic.AsyncAppender\">");
        System.out.println("    <queueSize>512</queueSize>");
        System.out.println("    <discardingThreshold>20</discardingThreshold>");
        System.out.println("    <neverBlock>false</neverBlock>");
        System.out.println("    <appender-ref ref=\"FILE\" />");
        System.out.println("  </appender>");
    }

    // ── 3. MDC Y VIRTUAL THREADS ──────────────────────────────────────────────
    // MDC usa ThreadLocal internamente. Con virtual threads el ThreadLocal funciona
    // igual — cada VT tiene su propio MDC. PERO hay un riesgo:
    //
    // Si el VT cede el carrier y otro hilo de plataforma retoma el trabajo,
    // el ThreadLocal del VT se preserva (es del VT, no del carrier).
    // Esto es seguro con VTs.
    //
    // El problema es con thread pools de plataforma: si submites una tarea a un
    // ExecutorService y el hilo se reutiliza para otro request, el MDC del
    // request anterior puede filtrarse. Solución: limpiar MDC en finally o
    // usar un Runnable wrapper.
    static void mdcWithVirtualThreads() throws Exception {
        System.out.println("── 3. MDC y Virtual Threads ──");

        // Simular MDC con ThreadLocal
        ThreadLocal<Map<String, String>> mdc = new ThreadLocal<>();

        Runnable request = () -> {
            // Cada VT tiene su propio ThreadLocal → sin contaminación entre requests
            Map<String, String> ctx = new HashMap<>();
            ctx.put("requestId", "req-" + Thread.currentThread().getName().hashCode());
            ctx.put("userId",    "user-42");
            mdc.set(ctx);

            System.out.println("  [" + ctx.get("requestId") + "] procesando en VT: "
                    + Thread.currentThread().isVirtual());
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("  [" + ctx.get("requestId") + "] completado");
            mdc.remove(); // buena práctica: limpiar aunque el VT muera
        };

        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<?>[] cfs = new CompletableFuture[3];
        for (int i = 0; i < 3; i++) {
            cfs[i] = CompletableFuture.runAsync(request, exec);
        }
        CompletableFuture.allOf(cfs).join();
        exec.shutdown();

        System.out.println();
        System.out.println("  VTs: cada uno tiene su ThreadLocal → no hay filtración de MDC entre requests");
        System.out.println("  Pool de plataforma: el ThreadLocal persiste → limpiar en finally o usar wrapper");
    }

    public static void main(String[] args) throws Exception {
        syncLoggingProblem();
        asyncLoggingDemo();
        mdcWithVirtualThreads();
    }
}
