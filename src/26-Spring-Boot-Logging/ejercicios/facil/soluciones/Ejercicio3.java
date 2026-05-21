import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Ejercicio 3 (Fácil) — MDC básico
// ThreadLocal<Map<String,String>> + Logger que incluye MDC en cada línea
public class Ejercicio3 {

    static class MDC {
        private static final ThreadLocal<Map<String, String>> context =
            ThreadLocal.withInitial(HashMap::new);

        public static void put(String key, String value) {
            context.get().put(key, value);
        }

        public static String get(String key) {
            return context.get().get(key);
        }

        public static void remove(String key) {
            context.get().remove(key);
        }

        public static void clear() {
            context.get().clear();
        }

        /** Devuelve una copia inmutable del mapa actual */
        public static Map<String, String> getCopyOfContextMap() {
            return Collections.unmodifiableMap(new HashMap<>(context.get()));
        }

        /** Formatea el MDC como [key1=val1, key2=val2] o vacío si no hay entradas */
        public static String format() {
            Map<String, String> map = context.get();
            if (map.isEmpty()) return "";
            StringBuilder sb = new StringBuilder("[");
            map.forEach((k, v) -> sb.append(k).append("=").append(v).append(", "));
            sb.setLength(sb.length() - 2); // quitar última ", "
            sb.append("] ");
            return sb.toString();
        }
    }

    enum Level { TRACE, DEBUG, INFO, WARN, ERROR }

    static class Logger {
        private final String name;
        private Level minLevel;

        Logger(String name, Level minLevel) {
            this.name = name;
            this.minLevel = minLevel;
        }

        private void log(Level level, String message) {
            if (level.ordinal() < minLevel.ordinal()) return;

            // El MDC se incluye automáticamente en cada línea
            String mdcStr = MDC.format();
            System.out.printf("[%-5s] %s%s - %s%n",
                level.name(), mdcStr, name, message);
        }

        public void trace(String msg) { log(Level.TRACE, msg); }
        public void debug(String msg) { log(Level.DEBUG, msg); }
        public void info(String msg)  { log(Level.INFO, msg); }
        public void warn(String msg)  { log(Level.WARN, msg); }
        public void error(String msg) { log(Level.ERROR, msg); }
    }

    public static void main(String[] args) {
        System.out.println("=== MDC básico ===");
        System.out.println();

        Logger logger = new Logger("com.app.UserService", Level.DEBUG);

        // Sin MDC
        System.out.println("--- Sin MDC ---");
        logger.info("Logger sin contexto MDC");

        System.out.println();
        System.out.println("--- Con MDC: userId y requestId ---");

        // Simular inicio de request
        MDC.put("userId", "42");
        MDC.put("requestId", "abc-123-xyz");

        logger.info("Procesando request del usuario");
        logger.debug("Buscando usuario en base de datos");
        logger.info("Usuario encontrado: jorge");
        logger.warn("Intento fallido de autorización");
        logger.error("No se pudo procesar el pago");

        System.out.println();
        System.out.println("MDC actual: " + MDC.getCopyOfContextMap());

        // Simular fin de request → limpiar MDC
        MDC.clear();
        System.out.println("MDC tras clear(): " + MDC.getCopyOfContextMap());

        System.out.println();
        System.out.println("--- Después de clear (MDC vacío) ---");
        logger.info("Nuevo request sin MDC");

        System.out.println();
        System.out.println("--- MDC con múltiples threads ---");

        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            MDC.put("thread", threadName);
            MDC.put("traceId", "trace-" + threadName.hashCode());
            logger.info("Mensaje desde " + threadName);
            MDC.clear();
        };

        Thread t1 = new Thread(task, "worker-1");
        Thread t2 = new Thread(task, "worker-2");
        t1.start();
        t2.start();
        try { t1.join(); t2.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
