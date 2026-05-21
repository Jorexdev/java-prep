import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Ejercicio 4 (Fácil) — Log format
// Formato: "2024-01-15 10:23:45.123 [INFO ] [main  ] c.a.Service - mensaje"
public class Ejercicio4 {

    static final DateTimeFormatter TIMESTAMP_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Abrevia el nombre completo de un logger:
     * "com.app.service.UserService" → "c.a.s.UserService"
     * La última parte se mantiene completa; las anteriores se reducen a inicial.
     */
    static String abbreviateLoggerName(String name) {
        String[] parts = name.split("\\.");
        if (parts.length <= 1) return name;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(parts[i].charAt(0)).append(".");
            }
        }
        sb.append(parts[parts.length - 1]);
        return sb.toString();
    }

    enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    static class FormattedLogger {
        private final String name;
        private final String abbreviatedName;
        private Level minLevel;

        FormattedLogger(String name, Level minLevel) {
            this.name = name;
            this.abbreviatedName = abbreviateLoggerName(name);
            this.minLevel = minLevel;
        }

        /**
         * Formato: "2024-01-15 10:23:45.123 [INFO ] [main  ] c.a.Service - mensaje"
         * - Level:  5 chars con padding a la derecha
         * - Thread: 6 chars con padding a la derecha, recortado si más largo
         */
        private void log(Level level, String message) {
            if (level.ordinal() < minLevel.ordinal()) return;

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);

            // Nivel: 5 chars con padding derecho
            String levelStr = String.format("%-5s", level.name());

            // Thread: 6 chars con padding derecho, recortado si es más largo
            String rawThread = Thread.currentThread().getName();
            String threadStr = rawThread.length() > 6
                ? rawThread.substring(0, 6)
                : String.format("%-6s", rawThread);

            System.out.printf("%s [%s] [%s] %s - %s%n",
                timestamp, levelStr, threadStr, abbreviatedName, message);
        }

        public void trace(String msg) { log(Level.TRACE, msg); }
        public void debug(String msg) { log(Level.DEBUG, msg); }
        public void info(String msg)  { log(Level.INFO, msg); }
        public void warn(String msg)  { log(Level.WARN, msg); }
        public void error(String msg) { log(Level.ERROR, msg); }
    }

    public static void main(String[] args) {
        System.out.println("=== Log format ===");
        System.out.println();

        System.out.println("Verificación de abreviación de nombres:");
        String[] names = {
            "com.app.service.UserService",
            "com.example.controller.OrderController",
            "org.springframework.web.DispatcherServlet",
            "Service",
            "c.a.UserService"
        };
        for (String n : names) {
            System.out.printf("  %-50s → %s%n", n, abbreviateLoggerName(n));
        }

        System.out.println();
        System.out.println("Demo de log formateado:");
        System.out.println("-".repeat(75));

        FormattedLogger logger1 = new FormattedLogger("com.app.service.UserService", Level.TRACE);
        FormattedLogger logger2 = new FormattedLogger("com.app.controller.OrderController", Level.DEBUG);

        logger1.trace("Entrando a método findById(42)");
        logger1.debug("Ejecutando query: SELECT * FROM users WHERE id=42");
        logger1.info("Usuario encontrado: {id=42, name='jorge'}");
        logger1.warn("Sesión expira en 5 minutos");
        logger1.error("Fallo al guardar cambios: constraint violation");

        System.out.println();
        logger2.debug("Parámetros del request: {page=1, size=20}");
        logger2.info("GET /api/orders procesado en 34ms");

        System.out.println();
        System.out.println("--- Demo multi-thread (nombres de thread más largos) ---");
        Runnable task = () -> {
            FormattedLogger tLogger = new FormattedLogger("com.app.AsyncProcessor", Level.INFO);
            tLogger.info("Tarea iniciada en este thread");
        };

        Thread t1 = new Thread(task, "async-executor-1");
        Thread t2 = new Thread(task, "http-nio-8080-exec-10");
        t1.start();
        t2.start();
        try { t1.join(); t2.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
