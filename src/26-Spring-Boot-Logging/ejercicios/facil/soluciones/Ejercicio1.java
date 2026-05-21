// Ejercicio 1 (Fácil) — Log levels
// Logger con 5 niveles, solo loguea si nivel >= nivel configurado
public class Ejercicio1 {

    enum Level {
        TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);

        final int value;
        Level(int value) { this.value = value; }
    }

    static class Logger {
        private final String name;
        private Level configured;

        Logger(String name, Level configured) {
            this.name = name;
            this.configured = configured;
        }

        public void setLevel(Level level) { this.configured = level; }

        private void log(Level level, String message) {
            if (level.value >= configured.value) {
                System.out.printf("[%-5s] %s - %s%n", level.name(), name, message);
            }
        }

        public void trace(String msg) { log(Level.TRACE, msg); }
        public void debug(String msg) { log(Level.DEBUG, msg); }
        public void info(String msg)  { log(Level.INFO, msg); }
        public void warn(String msg)  { log(Level.WARN, msg); }
        public void error(String msg) { log(Level.ERROR, msg); }

        public boolean isTraceEnabled() { return Level.TRACE.value >= configured.value; }
        public boolean isDebugEnabled() { return Level.DEBUG.value >= configured.value; }
        public boolean isInfoEnabled()  { return Level.INFO.value >= configured.value; }
    }

    public static void main(String[] args) {
        System.out.println("=== Log levels ===");
        System.out.println();

        Logger logger = new Logger("com.example.Service", Level.INFO);

        System.out.println("Nivel configurado: INFO");
        System.out.println("Esperado: solo INFO, WARN, ERROR aparecen");
        System.out.println();

        logger.trace("Este mensaje NO debe aparecer (TRACE < INFO)");
        logger.debug("Este mensaje NO debe aparecer (DEBUG < INFO)");
        logger.info("Aplicación iniciada correctamente");
        logger.warn("Conexión lenta detectada: 2500ms");
        logger.error("No se pudo conectar a la base de datos");

        System.out.println();
        System.out.println("--- Cambio a nivel DEBUG ---");
        logger.setLevel(Level.DEBUG);
        logger.trace("Este mensaje NO debe aparecer (TRACE < DEBUG)");
        logger.debug("Cache miss para key='user:42'");
        logger.info("Request procesado en 45ms");

        System.out.println();
        System.out.println("--- Cambio a nivel TRACE (todo visible) ---");
        logger.setLevel(Level.TRACE);
        logger.trace("Entrando al método processRequest");
        logger.debug("Parámetros: {id=1, name='jorge'}");
        logger.info("Procesando...");

        System.out.println();
        System.out.println("--- Cambio a nivel ERROR (solo errores) ---");
        logger.setLevel(Level.ERROR);
        logger.info("Este NO aparece");
        logger.warn("Este NO aparece");
        logger.error("Error crítico: OutOfMemoryError");
    }
}
