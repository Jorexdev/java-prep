import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

// Structured logging con campos JSON automaticos via MDC

public class Ejercicio6 {

    // ====== MDC (Mapped Diagnostic Context) simulado ======
    // En produccion se usaria org.slf4j.MDC o equivalente

    static final ThreadLocal<Map<String, String>> MDC_CONTEXT =
            ThreadLocal.withInitial(LinkedHashMap::new);

    static class MDC {
        static void put(String key, String value) {
            MDC_CONTEXT.get().put(key, value);
        }

        static String get(String key) {
            return MDC_CONTEXT.get().get(key);
        }

        static void remove(String key) {
            MDC_CONTEXT.get().remove(key);
        }

        static void clear() {
            MDC_CONTEXT.get().clear();
        }

        static Map<String, String> getCopyOfContextMap() {
            return new LinkedHashMap<>(MDC_CONTEXT.get());
        }
    }

    // ====== JSON Logger estructurado ======

    enum Level { TRACE, DEBUG, INFO, WARN, ERROR }

    static class StructuredJsonLogger {
        private final String loggerName;
        private Level minLevel = Level.DEBUG;

        StructuredJsonLogger(String loggerName) {
            this.loggerName = abbreviate(loggerName);
        }

        void setLevel(Level level) { this.minLevel = level; }

        private boolean isEnabled(Level level) {
            return level.ordinal() >= minLevel.ordinal();
        }

        void log(Level level, String message, Map<String, Object> extraFields) {
            if (!isEnabled(level)) return;
            emit(level, message, extraFields);
        }

        void info(String message)  { log(Level.INFO,  message, null); }
        void debug(String message) { log(Level.DEBUG, message, null); }
        void warn(String message)  { log(Level.WARN,  message, null); }
        void error(String message) { log(Level.ERROR, message, null); }

        void info(String message, Map<String, Object> fields)  { log(Level.INFO,  message, fields); }
        void debug(String message, Map<String, Object> fields) { log(Level.DEBUG, message, fields); }
        void warn(String message, Map<String, Object> fields)  { log(Level.WARN,  message, fields); }
        void error(String message, Map<String, Object> fields) { log(Level.ERROR, message, fields); }

        private void emit(Level level, String message, Map<String, Object> extra) {
            StringBuilder sb = new StringBuilder("{");

            // Campos fijos
            appendStr(sb, "ts",     Instant.now().toString());
            sb.append(", ");
            appendStr(sb, "level",  level.name());
            sb.append(", ");
            appendStr(sb, "logger", loggerName);
            sb.append(", ");
            appendStr(sb, "msg",    message);
            sb.append(", ");
            appendStr(sb, "thread", Thread.currentThread().getName());

            // Campos del MDC (automaticos, sin codigo explicito)
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            for (Map.Entry<String, String> e : mdc.entrySet()) {
                sb.append(", ");
                appendStr(sb, e.getKey(), e.getValue());
            }

            // Campos extra pasados directamente
            if (extra != null) {
                for (Map.Entry<String, Object> e : extra.entrySet()) {
                    sb.append(", ");
                    if (e.getValue() instanceof Number) {
                        sb.append("\"").append(e.getKey()).append("\": ").append(e.getValue());
                    } else {
                        appendStr(sb, e.getKey(), String.valueOf(e.getValue()));
                    }
                }
            }

            sb.append("}");
            System.out.println(sb);
        }

        private void appendStr(StringBuilder sb, String key, String value) {
            sb.append("\"").append(key).append("\": \"").append(escape(value)).append("\"");
        }

        private String escape(String s) {
            return s == null ? "" : s.replace("\"", "\\\"");
        }

        private String abbreviate(String name) {
            // c.e.MyService -> para nombres de clase
            String[] parts = name.split("\\.");
            if (parts.length <= 1) return name;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                sb.append(parts[i].charAt(0)).append(".");
            }
            sb.append(parts[parts.length - 1]);
            return sb.toString();
        }
    }

    // ====== Servicios de ejemplo que usan el logger ======

    static class OrderService {
        private final StructuredJsonLogger log =
                new StructuredJsonLogger("com.example.OrderService");

        void processOrder(String orderId, String userId, double amount) {
            // Los campos del MDC se incluyen automaticamente en cada log
            log.info("Procesando pedido", Map.of(
                    "orderId", orderId,
                    "amount", amount,
                    "currency", "EUR"
            ));

            if (amount > 1000) {
                log.warn("Pedido de alto valor requiere aprobacion", Map.of(
                        "orderId", orderId,
                        "amount", amount,
                        "threshold", 1000
                ));
            }

            log.info("Pedido procesado correctamente", Map.of("orderId", orderId));
        }

        void failedOrder(String orderId, String reason) {
            log.error("Pedido fallido", Map.of(
                    "orderId", orderId,
                    "reason", reason,
                    "retryable", false
            ));
        }
    }

    static class AuthService {
        private final StructuredJsonLogger log =
                new StructuredJsonLogger("com.example.AuthService");

        void login(String username, boolean success) {
            if (success) {
                log.info("Login exitoso", Map.of("username", username));
            } else {
                log.warn("Login fallido", Map.of("username", username, "attempts", 3));
            }
        }
    }

    // ====== Request filter que gestiona el MDC ======

    static class RequestContext implements AutoCloseable {
        private final String requestId;
        private final String userId;

        RequestContext(String requestId, String userId) {
            this.requestId = requestId;
            this.userId = userId;
            // Poblar MDC al inicio del request
            MDC.put("requestId", requestId);
            MDC.put("userId", userId);
            MDC.put("service", "orders-api");
        }

        @Override
        public void close() {
            // Limpiar MDC al final del request (critico para evitar contaminacion entre threads)
            MDC.clear();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Structured Logging con MDC automatico ===");
        System.out.println();

        OrderService orderService = new OrderService();
        AuthService authService   = new AuthService();

        // --- Demo 1: request con MDC completo ---
        System.out.println("[ Demo 1: request normal con MDC ]");
        try (RequestContext ctx = new RequestContext("req-abc123", "user-42")) {
            authService.login("alice", true);
            orderService.processOrder("ORD-001", "user-42", 150.0);
        }
        System.out.println();

        // --- Demo 2: request con pedido de alto valor ---
        System.out.println("[ Demo 2: pedido de alto valor ]");
        try (RequestContext ctx = new RequestContext("req-def456", "user-99")) {
            MDC.put("role", "premium"); // campo adicional para este request
            orderService.processOrder("ORD-002", "user-99", 2500.0);
        }
        System.out.println();

        // --- Demo 3: error con MDC ---
        System.out.println("[ Demo 3: pedido fallido ]");
        try (RequestContext ctx = new RequestContext("req-ghi789", "user-07")) {
            orderService.failedOrder("ORD-003", "stock insuficiente");
        }
        System.out.println();

        // --- Demo 4: threads concurrentes, MDC aislado ---
        System.out.println("[ Demo 4: 3 threads concurrentes (MDC aislado por thread) ]");
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 1; i <= 3; i++) {
            final int n = i;
            Thread.ofVirtual().start(() -> {
                try (RequestContext ctx = new RequestContext(
                        "req-thread-" + n, "user-" + (n * 10))) {
                    MDC.put("threadNum", String.valueOf(n));
                    orderService.processOrder("ORD-T0" + n, "user-" + (n * 10), n * 100.0);
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        System.out.println();

        // --- Demo 5: log sin MDC activo ---
        System.out.println("[ Demo 5: log sin MDC (fuera de request) ]");
        authService.login("intruder", false);
        System.out.println();

        System.out.println("=== Conclusion ===");
        System.out.println("MDC se puebla al inicio del request (RequestContext) y se limpia al salir.");
        System.out.println("Todos los logs dentro del request incluyen los campos MDC automaticamente.");
        System.out.println("En produccion: SLF4J MDC + Logback JsonEncoder hace exactamente esto.");
        System.out.println("Los campos JSON permiten filtrar por userId, requestId en Elasticsearch/Kibana.");
    }
}
