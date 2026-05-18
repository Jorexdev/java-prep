import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Simula @ControllerAdvice con manejo centralizado de excepciones.
// Muestra cómo una excepción de negocio se convierte en respuesta HTTP con código y cuerpo.

// ── Excepciones de negocio ────────────────────────────────────────────────────

// extends RuntimeException → excepción no chequeada, Spring la propaga sin try/catch
class RecursoNoEncontradoException extends RuntimeException {
    private final String recurso;
    private final Object id;

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " con id=" + id + " no encontrado");
        this.recurso = recurso;
        this.id = id;
    }

    public String getRecurso() { return recurso; }
    public Object getId()      { return id; }
}

class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String operacion) {
        super("Acceso denegado para la operación: " + operacion);
    }
}

// ── Respuesta de error estándar ───────────────────────────────────────────────

// Equivale al DTO que @ExceptionHandler devuelve como JSON
class ErrorResponse {
    private final int status;
    private final String error;
    private final String mensaje;

    public ErrorResponse(int status, String error, String mensaje) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return "{ \"status\": " + status + ", \"error\": \"" + error
            + "\", \"mensaje\": \"" + mensaje + "\" }";
    }
}

// ── Servicio que lanza excepciones ───────────────────────────────────────────

// @Service
class PedidoService {
    // Simula una "base de datos" de pedidos
    private final Map<Long, String> pedidos = new HashMap<>(Map.of(
        1L, "Pedido laptop",
        2L, "Pedido teclado"
    ));

    // @Transactional (en producción)
    public String obtenerPedido(Long id) {
        if (!pedidos.containsKey(id)) {
            throw new RecursoNoEncontradoException("Pedido", id);   // ← se propaga al handler
        }
        return pedidos.get(id);
    }

    public void cancelarPedido(Long id, String usuario) {
        if (!pedidos.containsKey(id)) {
            throw new RecursoNoEncontradoException("Pedido", id);
        }
        if (usuario.equals("invitado")) {
            throw new AccesoDenegadoException("cancelar pedido");   // ← 403
        }
        pedidos.remove(id);
        System.out.println("  [Service] Pedido " + id + " cancelado por " + usuario);
    }
}

// ── GlobalExceptionHandler ────────────────────────────────────────────────────

// @RestControllerAdvice                          ← se aplica a TODOS los controllers
// Centraliza el manejo de errores — ningún controller necesita try/catch
class GlobalExceptionHandler {

    // @ExceptionHandler(RecursoNoEncontradoException.class)
    // @ResponseStatus(HttpStatus.NOT_FOUND)  → 404
    public ErrorResponse manejarNotFound(RecursoNoEncontradoException ex) {
        System.out.println("  [Advisor] RecursoNoEncontradoException capturada → 404");
        return new ErrorResponse(404, "Not Found", ex.getMessage());
    }

    // @ExceptionHandler(AccesoDenegadoException.class)
    // @ResponseStatus(HttpStatus.FORBIDDEN)  → 403
    public ErrorResponse manejarAccesoDenegado(AccesoDenegadoException ex) {
        System.out.println("  [Advisor] AccesoDenegadoException capturada → 403");
        return new ErrorResponse(403, "Forbidden", ex.getMessage());
    }

    // @ExceptionHandler(Exception.class)      ← catch-all para excepciones inesperadas
    // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) → 500
    public ErrorResponse manejarGeneral(Exception ex) {
        System.out.println("  [Advisor] Excepción inesperada capturada → 500");
        return new ErrorResponse(500, "Internal Server Error", "Error interno del servidor");
    }

    // Enrutador: simula cómo DispatcherServlet redirige la excepción al handler correcto
    public ErrorResponse handle(Exception ex) {
        if (ex instanceof RecursoNoEncontradoException e) return manejarNotFound(e);
        if (ex instanceof AccesoDenegadoException e)     return manejarAccesoDenegado(e);
        return manejarGeneral(ex);
    }
}

// ── Simulación del DispatcherServlet invocando controller + advisor ───────────

// Simula la cadena: Controller method → excepción → ControllerAdvice → respuesta HTTP
class Dispatcher {
    private final GlobalExceptionHandler advisor = new GlobalExceptionHandler();

    public void ejecutar(String descripcion, Runnable controllerMethod) {
        System.out.println(">>> " + descripcion);
        try {
            controllerMethod.run();
            System.out.println("  → 200 OK\n");
        } catch (Exception ex) {
            ErrorResponse resp = advisor.handle(ex);
            System.out.println("  → Respuesta: " + resp + "\n");
        }
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpExceptionHandler {
    public static void main(String[] args) {

        PedidoService service = new PedidoService();
        Dispatcher dispatcher = new Dispatcher();

        System.out.println("=== Simulación @ControllerAdvice — Manejo centralizado de excepciones ===\n");

        // Caso 1: recurso existente → 200 OK
        dispatcher.ejecutar("GET /api/pedidos/1", () -> {
            String pedido = service.obtenerPedido(1L);
            System.out.println("  [Controller] Pedido encontrado: " + pedido);
        });

        // Caso 2: recurso inexistente → 404 Not Found (via @ExceptionHandler)
        dispatcher.ejecutar("GET /api/pedidos/99", () -> {
            service.obtenerPedido(99L);   // lanza RecursoNoEncontradoException
        });

        // Caso 3: usuario autorizado cancela pedido → 200 OK
        dispatcher.ejecutar("DELETE /api/pedidos/2 (usuario=admin)", () -> {
            service.cancelarPedido(2L, "admin");
        });

        // Caso 4: usuario invitado intenta cancelar → 403 Forbidden
        dispatcher.ejecutar("DELETE /api/pedidos/1 (usuario=invitado)", () -> {
            service.cancelarPedido(1L, "invitado");   // lanza AccesoDenegadoException
        });

        // Caso 5: excepción inesperada → 500 Internal Server Error
        dispatcher.ejecutar("GET /api/pedidos/crash", () -> {
            throw new RuntimeException("Error de base de datos inesperado");
        });
    }
}
