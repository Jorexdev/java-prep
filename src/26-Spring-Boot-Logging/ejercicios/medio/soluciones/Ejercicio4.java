// Ejercicio 4 (Medio) — Correlation ID chain
// CorrelationContext con ThreadLocal + propagación A→B→C
public class Ejercicio4 {

    static class CorrelationContext {
        private static final ThreadLocal<String> correlationId = new ThreadLocal<>();

        public static void set(String id) { correlationId.set(id); }
        public static String get() { return correlationId.get(); }
        public static void clear() { correlationId.remove(); }

        public static String getOrEmpty() {
            String id = correlationId.get();
            return id != null ? id : "(none)";
        }
    }

    enum Level { DEBUG, INFO, WARN, ERROR }

    static class Logger {
        private final String serviceName;

        Logger(String serviceName) { this.serviceName = serviceName; }

        public void info(String msg) {
            System.out.printf("[INFO ] [correlationId=%s] [%s] %s%n",
                CorrelationContext.getOrEmpty(), serviceName, msg);
        }

        public void debug(String msg) {
            System.out.printf("[DEBUG] [correlationId=%s] [%s] %s%n",
                CorrelationContext.getOrEmpty(), serviceName, msg);
        }

        public void warn(String msg) {
            System.out.printf("[WARN ] [correlationId=%s] [%s] %s%n",
                CorrelationContext.getOrEmpty(), serviceName, msg);
        }
    }

    // Nivel C: servicio de acceso a datos
    static class DataRepository {
        private static final Logger logger = new Logger("DataRepository");

        public String findById(String id) {
            // Lee el correlationId del contexto (propagado por A y B)
            logger.debug("SELECT * FROM users WHERE id=" + id
                    + " [correlationId=" + CorrelationContext.getOrEmpty() + "]");
            return "User{id=" + id + ", name='jorge'}";
        }
    }

    // Nivel B: servicio de negocio
    static class UserService {
        private static final Logger logger = new Logger("UserService");
        private final DataRepository repo = new DataRepository();

        public String getUser(String id) {
            logger.info("Buscando usuario con id=" + id);
            String result = repo.findById(id); // llama a C
            logger.info("Usuario encontrado: " + result);
            return result;
        }
    }

    // Nivel A: controlador HTTP (punto de entrada)
    static class UserController {
        private static final Logger logger = new Logger("UserController");
        private final UserService service = new UserService();

        public void handleRequest(String path, String correlationId) {
            // A establece el correlationId al inicio del request
            CorrelationContext.set(correlationId);

            try {
                logger.info("GET " + path + " recibido");
                String result = service.getUser("42"); // llama a B
                logger.info("Response: 200 OK → " + result);
            } finally {
                // Siempre limpiar al final del request
                CorrelationContext.clear();
                logger.debug("Contexto de correlación limpiado");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Correlation ID chain ===");
        System.out.println("Cadena: UserController(A) → UserService(B) → DataRepository(C)");
        System.out.println();

        UserController controller = new UserController();

        // Request 1 con su correlationId
        System.out.println("--- Request 1 ---");
        controller.handleRequest("/api/users/42", "corr-abc-001");

        System.out.println();

        // Request 2 con diferente correlationId
        System.out.println("--- Request 2 ---");
        controller.handleRequest("/api/users/99", "corr-xyz-002");

        System.out.println();

        // Demo con threads simultáneos
        System.out.println("--- 2 requests simultáneos (cada uno con su correlationId) ---");
        Thread t1 = new Thread(() ->
            controller.handleRequest("/api/users/1", "corr-thread1-aaa"), "http-1");
        Thread t2 = new Thread(() ->
            controller.handleRequest("/api/users/2", "corr-thread2-bbb"), "http-2");

        t1.start();
        t2.start();
        try { t1.join(); t2.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        System.out.println();
        System.out.println("Conclusión:");
        System.out.println("  - El mismo correlationId aparece en A, B y C para cada request");
        System.out.println("  - ThreadLocal garantiza que cada thread tiene su propio ID");
        System.out.println("  - Permite trazar una petición completa a través de todos los logs");
    }
}
