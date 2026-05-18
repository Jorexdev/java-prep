import java.util.ArrayList;
import java.util.List;

/**
 * Simulación de la cadena de filtros de Spring Security con Java puro.
 *
 * Demuestra:
 * - Interfaz Filtro con doFilter(Request, Response, FilterChain)
 * - FiltroLogging — registra cada request
 * - FiltroAutenticacion — rechaza requests sin token
 * - FiltroAutorizacion — rechaza requests sin el rol requerido
 * - FilterChain ejecuta los filtros en orden y puede cortocircuitar
 * - Casos: sin token (401), con token sin rol (403), con token y rol (200)
 *
 * Las anotaciones Spring aparecen como comentarios.
 *
 * Ejecutar: java -cp target/classes ExpSecurityChain
 */
public class ExpSecurityChain {

    // ── Modelos de Request / Response ────────────────────────────────────────

    static class Request {
        final String metodo;
        final String ruta;
        final String token;         // null si no hay Authorization header
        final String rol;           // extraído del token en FiltroAutenticacion

        private String rolAutenticado;

        Request(String metodo, String ruta, String token) {
            this.metodo = metodo;
            this.ruta   = ruta;
            this.token  = token;
            this.rol    = null;
        }

        void setRolAutenticado(String rol) {
            this.rolAutenticado = rol;
        }

        String getRolAutenticado() {
            return rolAutenticado;
        }

        @Override
        public String toString() {
            return metodo + " " + ruta + (token != null ? " [token: " + token + "]" : " [sin token]");
        }
    }

    static class Response {
        int status = 200;
        String body = "";

        void enviar(int status, String body) {
            this.status = status;
            this.body   = body;
        }

        @Override
        public String toString() {
            return "HTTP " + status + " — " + body;
        }
    }

    // ── Interfaz Filtro — simula javax.servlet.Filter ────────────────────────

    interface Filtro {
        /**
         * Procesa el request. Llama a chain.next() para pasar al siguiente filtro.
         * Si no llama a chain.next(), el request se corta aquí.
         */
        void doFilter(Request request, Response response, FilterChain chain);

        default String nombre() {
            return getClass().getSimpleName();
        }
    }

    // ── FilterChain ───────────────────────────────────────────────────────────

    static class FilterChain {
        private final List<Filtro> filtros;
        private int indice = 0;
        private final Runnable endpoint;

        FilterChain(List<Filtro> filtros, Runnable endpoint) {
            this.filtros  = new ArrayList<>(filtros);
            this.endpoint = endpoint;
        }

        public void next(Request request, Response response) {
            if (indice < filtros.size()) {
                Filtro filtro = filtros.get(indice++);
                filtro.doFilter(request, response, this);
            } else {
                // Todos los filtros pasados — llegar al endpoint
                endpoint.run();
            }
        }
    }

    // ── Implementaciones de filtros ──────────────────────────────────────────

    /**
     * Registra cada request que pasa por la cadena.
     * Simula el filtro de logging de Spring Security.
     */
    // @Component
    static class FiltroLogging implements Filtro {

        @Override
        public void doFilter(Request request, Response response, FilterChain chain) {
            System.out.println("    [Logging] Request: " + request);
            chain.next(request, response);   // siempre pasa al siguiente
            System.out.println("    [Logging] Response: " + response);
        }
    }

    /**
     * Verifica que el request lleve un token válido.
     * Si no hay token → 401 Unauthorized y corta la cadena.
     * Si hay token → extrae el rol y pasa al siguiente filtro.
     *
     * Simula UsernamePasswordAuthenticationFilter / JwtAuthFilter.
     */
    // @Component
    static class FiltroAutenticacion implements Filtro {

        // Tokens válidos simulados: token → rol
        private static final java.util.Map<String, String> TOKENS_VALIDOS = new java.util.Map.of(
            "token-user-123",  "USER",
            "token-admin-456", "ADMIN"
        );

        @Override
        public void doFilter(Request request, Response response, FilterChain chain) {
            if (request.token == null || !TOKENS_VALIDOS.containsKey(request.token)) {
                System.out.println("    [Autenticacion] Sin token válido → 401");
                response.enviar(401, "Unauthorized — se requiere autenticación");
                // No llamamos a chain.next() → cadena cortada aquí
                return;
            }

            String rol = TOKENS_VALIDOS.get(request.token);
            request.setRolAutenticado(rol);
            System.out.println("    [Autenticacion] Token válido, rol: " + rol);
            chain.next(request, response);   // continuar cadena
        }
    }

    /**
     * Verifica que el usuario autenticado tenga el rol requerido para la ruta.
     * Si no tiene el rol → 403 Forbidden.
     *
     * Simula el filtro de autorización de Spring Security.
     */
    // @Component
    static class FiltroAutorizacion implements Filtro {

        @Override
        public void doFilter(Request request, Response response, FilterChain chain) {
            String ruta = request.ruta;
            String rol  = request.getRolAutenticado();

            // Regla: /admin/** requiere ADMIN
            if (ruta.startsWith("/admin") && !"ADMIN".equals(rol)) {
                System.out.println("    [Autorizacion] Ruta de admin con rol " + rol + " → 403");
                response.enviar(403, "Forbidden — se requiere rol ADMIN");
                return;
            }

            System.out.println("    [Autorizacion] Acceso autorizado para rol: " + rol);
            chain.next(request, response);
        }
    }

    // ── Endpoint simulado ────────────────────────────────────────────────────

    static void procesarEndpoint(Request request, Response response) {
        System.out.println("    [Controller] Procesando " + request.metodo + " " + request.ruta);
        response.enviar(200, "OK — recurso entregado a " + request.getRolAutenticado());
    }

    // ── Método auxiliar para ejecutar un caso ────────────────────────────────

    static void ejecutarRequest(String descripcion, Request request) {
        System.out.println("\n── " + descripcion);
        Response response = new Response();

        List<Filtro> filtros = List.of(
            new FiltroLogging(),
            new FiltroAutenticacion(),
            new FiltroAutorizacion()
        );

        FilterChain chain = new FilterChain(filtros,
            () -> procesarEndpoint(request, response));

        chain.next(request, response);
        System.out.println("  Resultado final: " + response);
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  ExpSecurityChain — Cadena de filtros Spring Security");
        System.out.println("═══════════════════════════════════════════════════");

        // Caso 1: request sin token → 401 en FiltroAutenticacion
        ejecutarRequest(
            "Caso 1: GET /api/datos sin token",
            new Request("GET", "/api/datos", null)
        );

        // Caso 2: token de USER intentando acceder a /admin → 403 en FiltroAutorizacion
        ejecutarRequest(
            "Caso 2: GET /admin/usuarios con token USER",
            new Request("GET", "/admin/usuarios", "token-user-123")
        );

        // Caso 3: token de ADMIN accediendo a /admin → 200 llega al controller
        ejecutarRequest(
            "Caso 3: GET /admin/usuarios con token ADMIN",
            new Request("GET", "/admin/usuarios", "token-admin-456")
        );

        // Caso 4: token de USER accediendo a ruta pública → 200
        ejecutarRequest(
            "Caso 4: GET /api/productos con token USER",
            new Request("GET", "/api/productos", "token-user-123")
        );

        // Caso 5: token inválido → 401
        ejecutarRequest(
            "Caso 5: GET /api/datos con token falso",
            new Request("GET", "/api/datos", "token-falso-xyz")
        );

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("  Resumen del flujo:");
        System.out.println("  Sin token      → FiltroAutenticacion devuelve 401");
        System.out.println("  Sin rol ADMIN  → FiltroAutorizacion devuelve 403 en /admin/**");
        System.out.println("  Token válido + rol correcto → llega al Controller");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
