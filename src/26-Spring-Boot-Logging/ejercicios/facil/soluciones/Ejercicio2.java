import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Ejercicio 2 (Fácil) — Logger hierarchy
// Si un logger no tiene nivel propio, hereda del padre
public class Ejercicio2 {

    enum Level {
        TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);
        final int value;
        Level(int value) { this.value = value; }
    }

    static class HierarchicalLogger {
        final String name;
        private Level ownLevel; // null si no tiene nivel propio
        private final HierarchicalLogger parent;

        HierarchicalLogger(String name, Level ownLevel, HierarchicalLogger parent) {
            this.name = name;
            this.ownLevel = ownLevel;
            this.parent = parent;
        }

        /**
         * Devuelve el nivel efectivo: el propio si existe, o el del padre recursivamente.
         */
        public Level effectiveLevel() {
            if (ownLevel != null) return ownLevel;
            if (parent != null) return parent.effectiveLevel();
            return Level.INFO; // fallback final
        }

        private void log(Level level, String message) {
            if (level.value >= effectiveLevel().value) {
                System.out.printf("[%-5s] %-35s - %s%n",
                    level.name(), name, message);
            }
        }

        public void trace(String msg) { log(Level.TRACE, msg); }
        public void debug(String msg) { log(Level.DEBUG, msg); }
        public void info(String msg)  { log(Level.INFO, msg); }
        public void warn(String msg)  { log(Level.WARN, msg); }
        public void error(String msg) { log(Level.ERROR, msg); }
    }

    static class LoggerFactory {
        private final Map<String, HierarchicalLogger> registry = new HashMap<>();

        public HierarchicalLogger getOrCreate(String name, Level ownLevel) {
            HierarchicalLogger parent = findParent(name);
            HierarchicalLogger logger = new HierarchicalLogger(name, ownLevel, parent);
            registry.put(name, logger);
            return logger;
        }

        private HierarchicalLogger findParent(String name) {
            // Buscar el logger más específico que sea prefijo de este nombre
            String candidate = name;
            while (candidate.contains(".")) {
                candidate = candidate.substring(0, candidate.lastIndexOf('.'));
                if (registry.containsKey(candidate)) return registry.get(candidate);
            }
            return registry.get("root");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Logger hierarchy ===");
        System.out.println();

        LoggerFactory factory = new LoggerFactory();

        // root con nivel INFO
        HierarchicalLogger root = factory.getOrCreate("root", Level.INFO);

        // com.app con nivel DEBUG (más permisivo que root)
        HierarchicalLogger comApp = factory.getOrCreate("com.app", Level.DEBUG);

        // com.app.service SIN nivel propio → hereda de com.app (DEBUG)
        HierarchicalLogger comAppService = factory.getOrCreate("com.app.service", null);

        // com.other SIN nivel propio → hereda de root (INFO)
        HierarchicalLogger comOther = factory.getOrCreate("com.other", null);

        System.out.println("Árbol de loggers:");
        System.out.println("  root                  nivel propio=INFO  efectivo=INFO");
        System.out.println("  com.app               nivel propio=DEBUG efectivo=DEBUG");
        System.out.println("  com.app.service       nivel propio=null  efectivo=" + comAppService.effectiveLevel() + " (hereda com.app)");
        System.out.println("  com.other             nivel propio=null  efectivo=" + comOther.effectiveLevel() + " (hereda root)");
        System.out.println();

        System.out.println("--- Logging con root (INFO) ---");
        root.debug("NO aparece (DEBUG < INFO)");
        root.info("Aplicación arrancando");
        root.warn("Memoria al 80%");

        System.out.println();
        System.out.println("--- Logging con com.app (DEBUG) ---");
        comApp.debug("Cache inicializado");
        comApp.info("Módulo app listo");

        System.out.println();
        System.out.println("--- Logging con com.app.service (hereda DEBUG de com.app) ---");
        comAppService.debug("Método processRequest() llamado");  // aparece porque hereda DEBUG
        comAppService.info("Request procesado");

        System.out.println();
        System.out.println("--- Logging con com.other (hereda INFO de root) ---");
        comOther.debug("NO aparece (hereda INFO de root)");
        comOther.info("Módulo other iniciado");

        System.out.println();
        System.out.println("--- Resumen de niveles efectivos ---");
        System.out.printf("%-30s nivel=%s%n", "root", root.effectiveLevel());
        System.out.printf("%-30s nivel=%s%n", "com.app", comApp.effectiveLevel());
        System.out.printf("%-30s nivel=%s (heredado)%n", "com.app.service", comAppService.effectiveLevel());
        System.out.printf("%-30s nivel=%s (heredado)%n", "com.other", comOther.effectiveLevel());
    }
}
