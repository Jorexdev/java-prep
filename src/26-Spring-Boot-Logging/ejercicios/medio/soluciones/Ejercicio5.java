import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Ejercicio 5 (Medio) — Hot reload log level
// LogLevelManager permite cambiar niveles en runtime sin reiniciar
public class Ejercicio5 {

    enum Level {
        TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);
        final int value;
        Level(int value) { this.value = value; }
    }

    /**
     * Gestor central de niveles de log.
     * Thread-safe mediante ConcurrentHashMap.
     * Los loggers consultan este manager en cada llamada.
     */
    static class LogLevelManager {
        // ConcurrentHashMap para thread-safety en actualizaciones en caliente
        private final ConcurrentHashMap<String, Level> levels = new ConcurrentHashMap<>();
        private Level defaultLevel = Level.INFO;

        public void setLevel(String loggerName, Level level) {
            Level old = levels.put(loggerName, level);
            System.out.println("[LogLevelManager] Nivel cambiado: " + loggerName
                    + ": " + (old != null ? old : defaultLevel) + " → " + level);
        }

        public void setDefaultLevel(Level level) {
            Level old = this.defaultLevel;
            this.defaultLevel = level;
            System.out.println("[LogLevelManager] Nivel por defecto: " + old + " → " + level);
        }

        /**
         * Resuelve el nivel efectivo de un logger.
         * Busca el nombre exacto, luego los prefijos más largos, finalmente el default.
         */
        public Level getEffectiveLevel(String loggerName) {
            // Exacto
            if (levels.containsKey(loggerName)) return levels.get(loggerName);

            // Prefijos: buscar el más específico que coincida
            String best = null;
            for (String key : levels.keySet()) {
                if (loggerName.startsWith(key + ".") || loggerName.equals(key)) {
                    if (best == null || key.length() > best.length()) best = key;
                }
            }
            if (best != null) return levels.get(best);

            return defaultLevel;
        }

        public void printAll() {
            System.out.println("Niveles configurados:");
            System.out.println("  [default] = " + defaultLevel);
            levels.forEach((k, v) -> System.out.println("  " + k + " = " + v));
        }
    }

    static final LogLevelManager manager = new LogLevelManager();

    static class Logger {
        private final String name;

        Logger(String name) { this.name = name; }

        private void log(Level level, String message) {
            Level effective = manager.getEffectiveLevel(name);
            if (level.value >= effective.value) {
                System.out.printf("[%-5s] [%-35s] %s%n", level.name(), name, message);
            }
        }

        public void trace(String msg) { log(Level.TRACE, msg); }
        public void debug(String msg) { log(Level.DEBUG, msg); }
        public void info(String msg)  { log(Level.INFO, msg); }
        public void warn(String msg)  { log(Level.WARN, msg); }
        public void error(String msg) { log(Level.ERROR, msg); }

        public Level currentLevel() { return manager.getEffectiveLevel(name); }
    }

    public static void main(String[] args) {
        System.out.println("=== Hot reload log level ===");
        System.out.println();

        Logger serviceLogger = new Logger("com.app.service.UserService");
        Logger repoLogger    = new Logger("com.app.repository.UserRepository");
        Logger ctrlLogger    = new Logger("com.app.controller.UserController");

        System.out.println("--- Estado inicial (default=INFO) ---");
        manager.printAll();
        System.out.println();

        System.out.println("Logging inicial (DEBUG no aparece):");
        serviceLogger.debug("Buscando usuario [NO APARECE]");
        serviceLogger.info("Módulo UserService iniciado");
        repoLogger.debug("Cache inicializado [NO APARECE]");
        repoLogger.info("Repositorio listo");

        System.out.println();
        System.out.println("=== Hot reload: com.app.service → DEBUG ===");
        manager.setLevel("com.app.service", Level.DEBUG);
        System.out.println();

        System.out.println("Logging después del cambio:");
        serviceLogger.debug("Buscando usuario id=42 [AHORA SÍ APARECE]");
        serviceLogger.debug("Query: SELECT * FROM users WHERE id=42 [APARECE]");
        serviceLogger.info("Usuario encontrado");
        repoLogger.debug("Aún NO aparece (repo tiene INFO heredado)");
        repoLogger.info("Repositorio: query ejecutada en 12ms");
        ctrlLogger.debug("Controller aún NO aparece (INFO heredado)");

        System.out.println();
        System.out.println("=== Hot reload: com.app.service → INFO (vuelta al nivel anterior) ===");
        manager.setLevel("com.app.service", Level.INFO);
        System.out.println();

        System.out.println("Logging después de volver a INFO:");
        serviceLogger.debug("DEBUG ya no aparece [FILTRADO]");
        serviceLogger.info("UserService: request completado");

        System.out.println();
        System.out.println("=== Hot reload: com.app → TRACE (paquete completo) ===");
        manager.setLevel("com.app", Level.TRACE);
        System.out.println();

        System.out.println("Logging con com.app en TRACE:");
        serviceLogger.trace("TRACE service [APARECE]");
        repoLogger.debug("DEBUG repo [APARECE por herencia com.app]");
        ctrlLogger.info("INFO controller [APARECE]");

        System.out.println();
        System.out.println("--- Estado final ---");
        manager.printAll();
        System.out.println("Nivel efectivo de UserService: " + serviceLogger.currentLevel());
        System.out.println("Nivel efectivo de UserRepository: " + repoLogger.currentLevel());
    }
}
