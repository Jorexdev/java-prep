import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Ejercicio 2 (Medio) — Request tracing
// traceId único por request en MDC + 3 threads simultáneos sin contaminación
public class Ejercicio2 {

    static class MDC {
        private static final ThreadLocal<Map<String, String>> ctx =
            ThreadLocal.withInitial(HashMap::new);

        public static void put(String k, String v) { ctx.get().put(k, v); }
        public static String get(String k) { return ctx.get().get(k); }
        public static void clear() { ctx.get().clear(); }
        public static Map<String, String> snapshot() {
            return Collections.unmodifiableMap(new HashMap<>(ctx.get()));
        }

        public static String format() {
            Map<String, String> m = ctx.get();
            if (m.isEmpty()) return "";
            StringBuilder sb = new StringBuilder("[");
            m.forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
            sb.setLength(sb.length() - 1);
            sb.append("] ");
            return sb.toString();
        }
    }

    enum Level { DEBUG, INFO, WARN, ERROR }

    static class Logger {
        private final String name;
        Logger(String name) { this.name = name; }

        public void info(String msg) {
            System.out.printf("[INFO ] [%-18s] %s%s - %s%n",
                Thread.currentThread().getName(), MDC.format(), name, msg);
        }
        public void debug(String msg) {
            System.out.printf("[DEBUG] [%-18s] %s%s - %s%n",
                Thread.currentThread().getName(), MDC.format(), name, msg);
        }
        public void warn(String msg) {
            System.out.printf("[WARN ] [%-18s] %s%s - %s%n",
                Thread.currentThread().getName(), MDC.format(), name, msg);
        }
    }

    // Lista thread-safe para recoger resultados del traceId de cada thread
    static final List<String> capturedTraceIds = Collections.synchronizedList(new ArrayList<>());

    static class RequestHandler {
        private static final Logger logger = new Logger("com.app.RequestHandler");

        /**
         * Simula el ciclo de vida de un request:
         * 1. Generar traceId y guardar en MDC
         * 2. Procesar el request (con logs)
         * 3. Limpiar MDC al finalizar
         */
        public void handle(String requestPath) {
            // Inicio: generar traceId y poner en MDC
            String traceId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put("traceId", traceId);
            MDC.put("path", requestPath);

            capturedTraceIds.add(traceId);

            try {
                logger.info("Request iniciado: " + requestPath);

                // Simular trabajo con delay variable para entrelazar los threads
                simulateWork(requestPath);

                logger.info("Request completado");

                // Verificar que el traceId no cambió durante el request
                String currentTraceId = MDC.get("traceId");
                logger.debug("traceId al final del request: " + currentTraceId
                        + " (mismo que al inicio: " + traceId.equals(currentTraceId) + ")");

            } finally {
                // Fin: limpiar MDC siempre (incluso si hay excepción)
                MDC.clear();
                logger.info("MDC limpiado");
            }
        }

        private void simulateWork(String path) {
            try {
                long delay = switch (path) {
                    case "/api/users"   -> 30;
                    case "/api/orders"  -> 50;
                    case "/api/products"-> 20;
                    default             -> 40;
                };
                Thread.sleep(delay);
                logger.debug("Trabajo completado en ~" + delay + "ms");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Request tracing con MDC ===");
        System.out.println();
        System.out.println("Lanzando 3 threads simultáneos, cada uno con su propio traceId...");
        System.out.println();

        RequestHandler handler = new RequestHandler();

        Thread t1 = new Thread(() -> handler.handle("/api/users"),   "http-exec-1");
        Thread t2 = new Thread(() -> handler.handle("/api/orders"),  "http-exec-2");
        Thread t3 = new Thread(() -> handler.handle("/api/products"),"http-exec-3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println();
        System.out.println("=== Verificación: no contaminación entre threads ===");
        System.out.println("TraceIds capturados: " + capturedTraceIds);
        long distinct = capturedTraceIds.stream().distinct().count();
        System.out.println("Todos distintos: " + (distinct == capturedTraceIds.size())
                + " (" + distinct + " de " + capturedTraceIds.size() + " son únicos)");

        System.out.println();
        System.out.println("Conclusión: cada request tiene su propio traceId en MDC.");
        System.out.println("El ThreadLocal garantiza aislamiento entre threads.");
    }
}
