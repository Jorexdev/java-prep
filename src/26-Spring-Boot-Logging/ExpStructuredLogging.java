import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// Simula structured JSON logging frente a plain-text logging.
// Spring Boot 3.4+ soporta structured logging nativamente con logging.structured.format=json.
// Antes de eso se configuraba Logback con LogstashEncoder.
public class ExpStructuredLogging {

    // ── Niveles de log ────────────────────────────────────────────────────────

    enum Level { DEBUG, INFO, WARN, ERROR }

    // ── Logger de texto plano ─────────────────────────────────────────────────

    static class PlainLogger {
        private final String name;

        PlainLogger(String name) { this.name = name; }

        void log(Level level, String message, Map<String, String> fields) {
            StringBuilder sb = new StringBuilder();
            sb.append(Instant.now()).append(" ");
            sb.append(level).append("  [").append(name).append("] ");
            sb.append(message);
            if (!fields.isEmpty()) {
                fields.forEach((k, v) -> sb.append(" ").append(k).append("=").append(v));
            }
            System.out.println(sb);
        }
    }

    // ── Logger JSON estructurado ──────────────────────────────────────────────

    static class JsonLogger {
        private final String name;

        JsonLogger(String name) { this.name = name; }

        // Serialización JSON manual — en producción se usa Jackson o Logstash
        private String toJson(Level level, String message, Map<String, String> extraFields) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("timestamp", Instant.now().toString());
            entry.put("level",     level.name());
            entry.put("logger",    name);
            entry.put("message",   message);
            entry.putAll(extraFields);  // campos adicionales al mismo nivel raíz

            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String> e : entry.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":");
                sb.append("\"").append(e.getValue()).append("\"");
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        void log(Level level, String message, Map<String, String> fields) {
            System.out.println(toJson(level, message, fields));
        }
    }

    // ── Demo: mismo evento en ambos formatos ──────────────────────────────────

    public static void main(String[] args) {
        Map<String, String> requestContext = new LinkedHashMap<>();
        requestContext.put("requestId", "req-abc123");
        requestContext.put("userId",    "u-42");
        requestContext.put("path",      "/api/orders");

        Map<String, String> errorContext = new LinkedHashMap<>();
        errorContext.put("requestId",   "req-xyz789");
        errorContext.put("userId",      "u-99");
        errorContext.put("errorCode",   "PAYMENT_TIMEOUT");
        errorContext.put("duration_ms", "5023");

        System.out.println("=== Plain text logging ===");
        PlainLogger plain = new PlainLogger("OrderService");
        plain.log(Level.INFO,  "Pedido recibido",     requestContext);
        plain.log(Level.ERROR, "Pago fallido",         errorContext);

        System.out.println("\n=== Structured JSON logging ===");
        JsonLogger json = new JsonLogger("OrderService");
        json.log(Level.INFO,  "Pedido recibido",     requestContext);
        json.log(Level.ERROR, "Pago fallido",         errorContext);

        System.out.println("\n=== Por qué structured logging ===");
        System.out.println("Plain: grep 'PAYMENT_TIMEOUT' app.log          → solo coincidencia de texto");
        System.out.println("JSON:  filter errorCode='PAYMENT_TIMEOUT'       → filtro exacto por campo");
        System.out.println("JSON:  avg(duration_ms) WHERE level='ERROR'     → métricas sobre logs");
        System.out.println("JSON:  correlate requestId across microservices → trazabilidad distribuida");

        System.out.println("\n=== Configuración en Spring Boot ===");
        System.out.println("logging.structured.format.console=json   # Spring Boot 3.4+");
        System.out.println("logging.structured.format.file=ecs        # Elastic Common Schema");
        System.out.println("# O vía Logback: LogstashEncoder en logback-spring.xml");
    }
}
