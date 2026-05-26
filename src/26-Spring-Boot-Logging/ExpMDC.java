import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Simula MDC (Mapped Diagnostic Context): cada hilo lleva su propio contexto de log.
// En Spring Boot con SLF4J+Logback: MDC.put("requestId", id) — exactamente la misma API.
public class ExpMDC {

    // ── MDC simulado con ThreadLocal ──────────────────────────────────────────

    // En SLF4J: org.slf4j.MDC — misma semántica
    static class MDCContext {
        // ThreadLocal garantiza que cada hilo ve su propio Map
        private static final ThreadLocal<Map<String, String>> context =
            ThreadLocal.withInitial(HashMap::new);

        static void put(String key, String value) {
            context.get().put(key, value);
        }

        static String get(String key) {
            return context.get().get(key);
        }

        static void remove(String key) {
            context.get().remove(key);
        }

        // Siempre limpiar al finalizar — en thread pools el hilo se reutiliza y el MDC persiste
        static void clear() {
            context.get().clear();
        }

        static Map<String, String> getCopyOfContextMap() {
            return new HashMap<>(context.get());
        }
    }

    // ── Logger que incluye MDC en cada línea ──────────────────────────────────

    static class Logger {
        private final String name;

        Logger(String name) { this.name = name; }

        private String formatMdc() {
            Map<String, String> ctx = MDCContext.getCopyOfContextMap();
            if (ctx.isEmpty()) return "";
            StringBuilder sb = new StringBuilder("[");
            ctx.forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
            sb.setCharAt(sb.length() - 1, ']');
            return sb.toString();
        }

        void info(String msg)  { System.out.println("INFO  " + formatMdc() + " " + name + " - " + msg); }
        void debug(String msg) { System.out.println("DEBUG " + formatMdc() + " " + name + " - " + msg); }
        void error(String msg) { System.out.println("ERROR " + formatMdc() + " " + name + " - " + msg); }
    }

    // ── Simulación de un HTTP request handler ─────────────────────────────────

    static final Logger log = new Logger("RequestHandler");

    static void handleRequest(String userId, String path) {
        String requestId = "req-" + UUID.randomUUID().toString().substring(0, 8);
        try {
            // En Spring Boot + SLF4J:
            //   MDC.put("requestId", requestId);
            //   MDC.put("userId",    userId);
            MDCContext.put("requestId", requestId);
            MDCContext.put("userId",    userId);

            log.info("Inicio → " + path);
            log.debug("Validando permisos");
            processPayment(userId);
            log.info("Completado");
        } finally {
            // finally garantiza limpieza aunque haya excepción
            MDCContext.clear();
        }
    }

    static void processPayment(String userId) {
        // Este método no recibe requestId pero el MDC lo propaga automáticamente
        log.info("Procesando pago para usuario " + userId);
        log.debug("Llamando a pasarela de pago...");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Request único ===");
        handleRequest("u-001", "/api/orders");

        System.out.println("\n=== Log fuera de request (MDC vacío) ===");
        log.info("Tarea de limpieza programada");

        System.out.println("\n=== Dos requests concurrentes — MDC separado por hilo ===");
        ExecutorService pool = Executors.newFixedThreadPool(2);

        pool.submit(() -> handleRequest("u-002", "/api/cart"));
        pool.submit(() -> handleRequest("u-003", "/api/profile"));

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\n=== Propagación a hilos hijo ===");
        // MDC es per-thread; para propagarlo a hilos hijo hay que copiarlo manualmente
        MDCContext.put("parentRequestId", "req-parent");
        Map<String, String> snapshot = MDCContext.getCopyOfContextMap();
        MDCContext.clear();

        pool = Executors.newSingleThreadExecutor();
        pool.submit(() -> {
            // Restaurar el snapshot del hilo padre en el hilo hijo
            snapshot.forEach(MDCContext::put);
            log.info("Tarea asíncrona con contexto del padre");
            MDCContext.clear();
        });
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }
}
