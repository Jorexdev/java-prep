import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Ejercicio 1 (Medio) — Structured JSON logging
// Cada log entry es un objeto JSON con campos fijos + MDC adicionales
public class Ejercicio1 {

    static final DateTimeFormatter ISO_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            .withZone(ZoneOffset.UTC);

    // MDC con ThreadLocal
    static class MDC {
        private static final ThreadLocal<Map<String, String>> ctx =
            ThreadLocal.withInitial(HashMap::new);

        public static void put(String k, String v) { ctx.get().put(k, v); }
        public static void clear() { ctx.get().clear(); }
        public static Map<String, String> snapshot() {
            return Collections.unmodifiableMap(new HashMap<>(ctx.get()));
        }
    }

    enum Level { TRACE, DEBUG, INFO, WARN, ERROR }

    static class JsonLogger {
        private final String loggerName;
        private Level minLevel;

        JsonLogger(String name, Level minLevel) {
            this.loggerName = name;
            this.minLevel = minLevel;
        }

        /**
         * Construye un JSON manualmente (sin librerías externas).
         * Escapa comillas dobles en los valores.
         */
        private String buildJson(Level level, String message) {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("ts", ISO_FMT.format(Instant.now()));
            fields.put("level", level.name());
            fields.put("logger", abbreviate(loggerName));
            fields.put("msg", message);

            // Añadir campos del MDC
            MDC.snapshot().forEach(fields::put);

            StringBuilder sb = new StringBuilder("{");
            fields.forEach((k, v) -> {
                sb.append('"').append(escape(k)).append("\":\"")
                  .append(escape(v)).append("\",");
            });
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setLength(sb.length() - 1);
            }
            sb.append('}');
            return sb.toString();
        }

        private String escape(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        private String abbreviate(String name) {
            String[] parts = name.split("\\.");
            if (parts.length <= 1) return name;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0)).append(".");
            }
            sb.append(parts[parts.length - 1]);
            return sb.toString();
        }

        private void log(Level level, String message) {
            if (level.ordinal() < minLevel.ordinal()) return;
            System.out.println(buildJson(level, message));
        }

        public void trace(String msg) { log(Level.TRACE, msg); }
        public void debug(String msg) { log(Level.DEBUG, msg); }
        public void info(String msg)  { log(Level.INFO, msg); }
        public void warn(String msg)  { log(Level.WARN, msg); }
        public void error(String msg) { log(Level.ERROR, msg); }
    }

    public static void main(String[] args) {
        System.out.println("=== Structured JSON logging ===");
        System.out.println();

        JsonLogger logger = new JsonLogger("com.app.service.UserService", Level.DEBUG);

        // Log entry sin MDC
        System.out.println("--- Log sin MDC ---");
        logger.info("Aplicación iniciada");

        System.out.println();
        System.out.println("--- Log con MDC populado (userId + requestId) ---");

        // Simular request 1
        MDC.put("userId", "42");
        MDC.put("requestId", "req-abc-123");
        logger.info("Procesando request del usuario");
        logger.debug("Buscando usuario en DB");
        logger.warn("Sesión próxima a expirar");
        MDC.clear();

        System.out.println();
        System.out.println("--- Log con diferente MDC ---");

        // Simular request 2
        MDC.put("userId", "99");
        MDC.put("requestId", "req-xyz-789");
        MDC.put("service", "payment-service");
        logger.info("Procesando pago");
        logger.error("Tarjeta rechazada: insufficient_funds");
        MDC.clear();

        System.out.println();
        System.out.println("--- Log sin MDC (después de clear) ---");
        logger.info("Request completado");

        System.out.println();
        System.out.println("--- Nota: cada línea es JSON válido, parseable por Elasticsearch/Splunk ---");
    }
}
