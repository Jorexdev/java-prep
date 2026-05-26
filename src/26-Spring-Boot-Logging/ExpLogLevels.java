import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// Simula el sistema de niveles de log y cambio dinámico en runtime.
// En Spring Boot: GET/POST /actuator/loggers/{name} cambia el nivel sin reiniciar.
public class ExpLogLevels {

    // ── Niveles — el orden numérico determina el filtrado ────────────────────

    enum Level {
        TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4), OFF(5);

        final int priority;
        Level(int priority) { this.priority = priority; }

        boolean isEnabled(Level configured) {
            return this.priority >= configured.priority;
        }
    }

    // ── Registro de loggers con niveles configurables ─────────────────────────

    // Equivale a la jerarquía de loggers de Logback/Log4j2
    static class LoggerRegistry {
        // Niveles configurados por logger name
        private final Map<String, Level> levels = new TreeMap<>();
        // Nivel raíz — fallback cuando no hay configuración específica
        private Level rootLevel = Level.INFO;

        // Simula logging.level.<name>=DEBUG en application.properties
        void setLevel(String name, Level level) {
            levels.put(name, level);
            System.out.println("  [Registry] " + name + " → " + level);
        }

        void setRootLevel(Level level) {
            this.rootLevel = level;
            System.out.println("  [Registry] ROOT → " + level);
        }

        // Herencia: com.example.db hereda de com.example si no tiene nivel propio
        Level effectiveLevel(String name) {
            if (levels.containsKey(name)) return levels.get(name);
            // Busca el prefijo más largo que tenga nivel configurado
            String candidate = name;
            while (candidate.contains(".")) {
                candidate = candidate.substring(0, candidate.lastIndexOf('.'));
                if (levels.containsKey(candidate)) return levels.get(candidate);
            }
            return rootLevel;
        }
    }

    // ── Logger que consulta el registry en cada llamada ───────────────────────

    static class Logger {
        private final String name;
        private final LoggerRegistry registry;

        Logger(String name, LoggerRegistry registry) {
            this.name     = name;
            this.registry = registry;
        }

        private void log(Level level, String message) {
            Level configured = registry.effectiveLevel(name);
            if (level.isEnabled(configured)) {
                System.out.printf("%-5s [%s] %s%n", level, name, message);
            }
        }

        void trace(String msg) { log(Level.TRACE, msg); }
        void debug(String msg) { log(Level.DEBUG, msg); }
        void info(String msg)  { log(Level.INFO,  msg); }
        void warn(String msg)  { log(Level.WARN,  msg); }
        void error(String msg) { log(Level.ERROR, msg); }
    }

    public static void main(String[] args) {
        LoggerRegistry registry = new LoggerRegistry();

        Logger appLog = new Logger("com.example.app",         registry);
        Logger dbLog  = new Logger("com.example.db",          registry);
        Logger secLog = new Logger("com.example.security",    registry);

        System.out.println("=== Configuración inicial: ROOT=INFO ===");
        registry.setRootLevel(Level.INFO);
        appLog.debug("query preparada");       // filtrado — DEBUG < INFO
        appLog.info("Aplicación iniciada");
        appLog.warn("Pool de conexiones al 80%");
        dbLog.debug("SELECT * FROM usuarios"); // filtrado
        dbLog.info("Conexión establecida");

        System.out.println("\n=== Activar DEBUG en com.example.db (como /actuator/loggers) ===");
        // POST /actuator/loggers/com.example.db {"configuredLevel":"DEBUG"}
        registry.setLevel("com.example.db", Level.DEBUG);
        dbLog.debug("SELECT * FROM usuarios WHERE id=42");   // ahora visible
        dbLog.debug("Índice btree utilizado: idx_usuarios_email");
        appLog.debug("query preparada");  // sigue filtrado — app hereda ROOT=INFO

        System.out.println("\n=== Herencia de niveles ===");
        registry.setLevel("com.example", Level.WARN);  // padre de app, db, security
        // db tiene nivel propio DEBUG → no hereda
        // security no tiene nivel propio → hereda WARN de com.example
        Logger newLogger = new Logger("com.example.newservice", registry);
        System.out.println("  com.example.db         efectivo: " + registry.effectiveLevel("com.example.db"));
        System.out.println("  com.example.newservice efectivo: " + registry.effectiveLevel("com.example.newservice"));
        newLogger.info("info visible?");   // filtrado — hereda WARN
        newLogger.warn("warning visible"); // visible

        System.out.println("\n=== Jerarquía de niveles ===");
        System.out.println("TRACE < DEBUG < INFO < WARN < ERROR < OFF");
        System.out.println("Si configuras WARN, solo WARN y ERROR se emiten.");
        System.out.println("DEBUG útil en desarrollo; WARN/ERROR en producción.");

        System.out.println("\n=== Reset a INFO (como desactivar verbose SQL) ===");
        registry.setLevel("com.example.db", Level.INFO);
        dbLog.debug("SELECT * FROM usuarios"); // filtrado de nuevo
        dbLog.info("Modo verbose SQL desactivado");
    }
}
