import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// Este ejemplo usa java.util.logging (sin dependencias extra).
// En Spring Boot usarías SLF4J + Logback — la API es prácticamente idéntica:
//   Logger log = LoggerFactory.getLogger(MiClase.class);
//   log.info("mensaje {}", variable);
//
// Los niveles equivalen así:
//   JUL SEVERE  → SLF4J ERROR
//   JUL WARNING → SLF4J WARN
//   JUL INFO    → SLF4J INFO
//   JUL FINE    → SLF4J DEBUG
//   JUL FINEST  → SLF4J TRACE
public class ExpLogging {

    private static final Logger log = Logger.getLogger(ExpLogging.class.getName());

    // MDC (Mapped Diagnostic Context) — en Spring Boot: MDC.put("reqId", id)
    // En plain Java simulamos con ThreadLocal: cada hilo tiene su propio contexto
    private static final ThreadLocal<String> requestId = new ThreadLocal<>();

    static void procesarPedido(String id, String usuario) {
        requestId.set(id);
        log.info(String.format("[%s] Procesando pedido de '%s'", requestId.get(), usuario));
        log.fine(String.format("[%s] Stock verificado, iniciando pago", requestId.get()));
        requestId.remove(); // siempre limpiar para evitar memory leaks en thread pools
    }

    static void pagoFallido(String pedidoId) {
        try {
            throw new RuntimeException("Timeout al conectar con pasarela de pago");
        } catch (Exception ex) {
            // En SLF4J: log.error("Pago fallido para {}: {}", pedidoId, ex.getMessage(), ex);
            log.severe(String.format("Pago fallido para %s: %s", pedidoId, ex.getMessage()));
        }
    }

    public static void main(String[] args) {
        // Configura el handler para mostrar FINE (DEBUG) en consola
        log.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        log.addHandler(handler);
        log.setLevel(Level.FINE);

        System.out.println("=== Niveles de log ===");
        log.severe("SEVERE — error crítico, aplicación comprometida");
        log.warning("WARNING — situación anómala, la app sigue funcionando");
        log.info("INFO — evento relevante del flujo normal");
        log.fine("FINE — detalle útil para debugging");

        System.out.println("\n=== Logging con contexto (MDC) ===");
        procesarPedido("REQ-001", "jorex");
        procesarPedido("REQ-002", "ana");

        System.out.println("\n=== Log de excepción ===");
        pagoFallido("REQ-003");

        System.out.println("\n=== Lazy evaluation (evita concatenar si el nivel está desactivado) ===");
        // En SLF4J: log.debug("Resultado: {}", calcularCosa()) — no evalúa el argumento si DEBUG está off
        // En JUL: suppliers para lo mismo
        log.fine(() -> "Lazy: " + String.join(", ", "a", "b", "c"));
    }
}
