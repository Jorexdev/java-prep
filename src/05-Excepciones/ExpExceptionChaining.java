import java.sql.SQLException;

// ===== 3-layer call stack: Controller → Service → Repository =====

// ─── Capa Repository ──────────────────────────────────────────────────────────
class OrderRepository {
    public String findOrder(int orderId) throws SQLException {
        if (orderId <= 0) {
            throw new SQLException("No row found for order_id=" + orderId, "02000", 0);
        }
        return "Order#" + orderId;
    }
}

// ─── Capa Service ─────────────────────────────────────────────────────────────
class OrderServiceException extends RuntimeException {
    public OrderServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

class OrderService {
    private final OrderRepository repository = new OrderRepository();

    // BIEN: encadena la causa → diagnóstico completo
    public String getOrder(int orderId) {
        try {
            return repository.findOrder(orderId);
        } catch (SQLException e) {
            throw new OrderServiceException("Fallo al recuperar orden id=" + orderId, e);
        }
    }

    // MAL: traga la causa → diagnóstico perdido
    public String getOrderNoCause(int orderId) {
        try {
            return repository.findOrder(orderId);
        } catch (SQLException e) {
            // Solo el mensaje — la causa y el stack trace de SQL desaparecen
            throw new OrderServiceException("Fallo al recuperar orden id=" + orderId, null);
        }
    }
}

// ─── Capa Controller ─────────────────────────────────────────────────────────
class OrderController {
    private final OrderService service = new OrderService();

    public String handleRequest(int orderId) {
        return service.getOrder(orderId);
    }
}

public class ExpExceptionChaining {

    // ─── 1. Encadenamiento normal con cause ───────────────────────────────────
    static void showChaining() {
        System.out.println("\n── 1. Encadenamiento con cause (controller → service → repo) ──");
        OrderController controller = new OrderController();
        try {
            controller.handleRequest(-5);
        } catch (OrderServiceException e) {
            System.out.println("getMessage():  " + e.getMessage());
            System.out.println("getCause():    " + e.getCause().getClass().getSimpleName()
                    + " → " + e.getCause().getMessage());

            // Navegar la cadena completa
            Throwable current = e;
            int depth = 0;
            System.out.println("Cadena completa:");
            while (current != null) {
                System.out.println("  [" + depth++ + "] "
                        + current.getClass().getSimpleName() + ": " + current.getMessage());
                current = current.getCause();
            }
        }
    }

    // ─── 2. initCause() como alternativa a constructor con cause ─────────────
    static void initCauseAlternative() {
        System.out.println("\n── 2. initCause() como alternativa ──");
        try {
            try {
                throw new SQLException("Timeout de conexión");
            } catch (SQLException original) {
                // initCause() se usa cuando no hay constructor con Throwable
                RuntimeException wrapped = new RuntimeException("Fallo en repositorio");
                wrapped.initCause(original);
                throw wrapped;
            }
        } catch (RuntimeException e) {
            System.out.println("Excepción: " + e.getMessage());
            System.out.println("Causa via initCause: " + e.getCause().getMessage());
        }
    }

    // ─── 3. printStackTrace(): con vs sin causa ───────────────────────────────
    static void printStackTraceComparison() {
        System.out.println("\n── 3. printStackTrace(): CON causa vs SIN causa ──");

        // CON causa — muestra "Caused by:" con el stack trace de origen
        System.out.println("[CON causa]");
        OrderService service = new OrderService();
        try {
            service.getOrder(-1);
        } catch (OrderServiceException e) {
            // Imprimir solo las primeras 4 líneas del stack trace para legibilidad
            String[] lines = stackTraceToString(e).split("\n");
            for (int i = 0; i < Math.min(lines.length, 8); i++) {
                System.out.println("  " + lines[i]);
            }
        }

        // SIN causa — información de diagnóstico incompleta
        System.out.println("[SIN causa]");
        try {
            service.getOrderNoCause(-1);
        } catch (OrderServiceException e) {
            String[] lines = stackTraceToString(e).split("\n");
            for (int i = 0; i < Math.min(lines.length, 4); i++) {
                System.out.println("  " + lines[i]);
            }
            System.out.println("  (sin 'Caused by:' → SQL original perdido)");
        }
    }

    // ─── 4. Preservar info diagnóstica vs atrapar y suprimir ─────────────────
    static void chainingVsSuppressing() {
        System.out.println("\n── 4. Con encadenamiento vs sin encadenamiento ──");

        // Sin encadenamiento: solo el mensaje final llega al log
        System.out.println("Sin encadenamiento:");
        try {
            try {
                throw new SQLException("Table 'users' doesn't exist");
            } catch (SQLException e) {
                throw new RuntimeException("Error de base de datos"); // causa perdida
            }
        } catch (RuntimeException e) {
            System.out.println("  Log: " + e.getMessage() + " | causa: " + e.getCause());
        }

        // Con encadenamiento: causa original disponible para debugging
        System.out.println("Con encadenamiento:");
        try {
            try {
                throw new SQLException("Table 'users' doesn't exist");
            } catch (SQLException e) {
                throw new RuntimeException("Error de base de datos", e); // causa preservada
            }
        } catch (RuntimeException e) {
            System.out.println("  Log: " + e.getMessage()
                    + " | causa: " + e.getCause().getMessage());
        }
    }

    // Utilidad: convierte stack trace a String
    private static String stackTraceToString(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }

    public static void main(String[] args) {
        showChaining();
        initCauseAlternative();
        printStackTraceComparison();
        chainingVsSuppressing();
    }
}
