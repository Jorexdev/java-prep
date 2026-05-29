import java.util.*;
import java.util.function.Function;
import java.util.regex.*;

public class Ejercicio5 {

    // --- Modelos ---

    record Request(String method, String path, Map<String, String> headers, Map<String, String> body) {}

    static class Response {
        int status;
        String body;
        Response(int status, String body) { this.status = status; this.body = body; }
        @Override public String toString() { return status + " — " + body; }
    }

    // --- ProblemDetail (RFC 7807) ---

    static class ProblemDetail {
        final int status;
        final String title;
        final String detail;

        ProblemDetail(int status, String title, String detail) {
            this.status = status;
            this.title = title;
            this.detail = detail;
        }

        Response toResponse() {
            String json = "{\"status\":" + status
                + ",\"title\":\"" + title
                + "\",\"detail\":\"" + detail + "\"}";
            return new Response(status, json);
        }
    }

    // --- Interceptor ---

    interface HandlerInterceptor {
        // @PreHandle — retorna false para cortar la cadena
        boolean preHandle(Request req, Response res);
        void postHandle(Request req, Response res);
    }

    static class AuthInterceptor implements HandlerInterceptor {
        private final Set<String> validTokens;

        AuthInterceptor(Set<String> validTokens) { this.validTokens = validTokens; }

        @Override
        public boolean preHandle(Request req, Response res) {
            String auth = req.headers().getOrDefault("Authorization", "");
            if (!auth.startsWith("Bearer ") || !validTokens.contains(auth.substring(7))) {
                ProblemDetail pd = new ProblemDetail(401, "Unauthorized", "Token de sesión inválido o ausente");
                Response r = pd.toResponse();
                res.status = r.status;
                res.body   = r.body;
                System.out.println("[AuthInterceptor] rechazado — 401");
                return false;
            }
            System.out.println("[AuthInterceptor] token válido");
            return true;
        }

        @Override
        public void postHandle(Request req, Response res) {
            System.out.println("[AuthInterceptor] postHandle — status=" + res.status);
        }
    }

    static class ValidationInterceptor implements HandlerInterceptor {
        // Simula @FieldRequired: lista de campos que deben estar presentes en el body
        private final List<String> requiredFields;

        ValidationInterceptor(List<String> requiredFields) { this.requiredFields = requiredFields; }

        @Override
        public boolean preHandle(Request req, Response res) {
            if ("POST".equals(req.method()) || "PUT".equals(req.method())) {
                for (String field : requiredFields) {
                    String val = req.body().get(field);
                    if (val == null || val.isBlank()) {
                        ProblemDetail pd = new ProblemDetail(400, "Bad Request",
                            "Campo requerido ausente: " + field);
                        Response r = pd.toResponse();
                        res.status = r.status;
                        res.body   = r.body;
                        System.out.println("[ValidationInterceptor] campo faltante: " + field + " — 400");
                        return false;
                    }
                }
            }
            System.out.println("[ValidationInterceptor] validación OK");
            return true;
        }

        @Override
        public void postHandle(Request req, Response res) {}
    }

    // --- Routing ---

    @FunctionalInterface
    interface HandlerMethod {
        Response handle(Request req, Map<String, String> pathVars);
    }

    static class RouteEntry {
        final String method;
        final Pattern pattern;
        final List<String> varNames;
        final HandlerMethod handler;

        RouteEntry(String method, String path, HandlerMethod handler) {
            this.method = method;
            this.handler = handler;
            this.varNames = new ArrayList<>();

            // Convertir /productos/{id} → regex /productos/(?<id>[^/]+)
            String regex = path.replaceAll("\\{(\\w+)}", m -> {
                varNames.add(m.substring(1, m.length() - 1));
                return "([^/]+)";
            });
            this.pattern = Pattern.compile("^" + regex + "$");
        }

        Optional<Map<String, String>> match(String path) {
            Matcher m = pattern.matcher(path);
            if (!m.matches()) return Optional.empty();
            Map<String, String> vars = new HashMap<>();
            for (int i = 0; i < varNames.size(); i++) {
                vars.put(varNames.get(i), m.group(i + 1));
            }
            return Optional.of(vars);
        }
    }

    // --- ErrorHandlerMiddleware ---

    static class ErrorHandlerMiddleware {
        Response wrap(Function<Void, Response> action) {
            try {
                return action.apply(null);
            } catch (Exception ex) {
                System.out.println("[ErrorHandler] excepción no capturada: " + ex.getMessage());
                return new ProblemDetail(500, "Internal Server Error", ex.getMessage()).toResponse();
            }
        }
    }

    // --- DispatcherServlet ---

    static class DispatcherServlet {
        private final List<RouteEntry> routes = new ArrayList<>();
        private final List<HandlerInterceptor> interceptors = new ArrayList<>();
        private final ErrorHandlerMiddleware errorHandler = new ErrorHandlerMiddleware();

        void register(String method, String path, HandlerMethod handler) {
            routes.add(new RouteEntry(method, path, handler));
        }

        void addInterceptor(HandlerInterceptor interceptor) {
            interceptors.add(interceptor);
        }

        Response dispatch(Request req) {
            return errorHandler.wrap(_ -> {
                // Buscar ruta
                RouteEntry matched = null;
                Map<String, String> pathVars = null;
                for (RouteEntry route : routes) {
                    if (!route.method.equals(req.method())) continue;
                    Optional<Map<String, String>> vars = route.match(req.path());
                    if (vars.isPresent()) {
                        matched = route;
                        pathVars = vars.get();
                        break;
                    }
                }

                if (matched == null) {
                    return new ProblemDetail(404, "Not Found",
                        "No existe el endpoint: " + req.method() + " " + req.path()).toResponse();
                }

                Response res = new Response(200, "");

                // Pre-handle chain
                for (HandlerInterceptor interceptor : interceptors) {
                    if (!interceptor.preHandle(req, res)) {
                        return res; // cadena cortada
                    }
                }

                // Handler
                Response handlerRes = matched.handler.handle(req, pathVars);
                res.status = handlerRes.status;
                res.body   = handlerRes.body;

                // Post-handle chain (orden inverso)
                for (int i = interceptors.size() - 1; i >= 0; i--) {
                    interceptors.get(i).postHandle(req, res);
                }

                return res;
            });
        }
    }

    // --- Main ---

    public static void main(String[] args) {
        DispatcherServlet servlet = new DispatcherServlet();

        // Interceptores
        servlet.addInterceptor(new AuthInterceptor(Set.of("token-valid-123")));
        servlet.addInterceptor(new ValidationInterceptor(List.of("nombre", "precio")));

        // Rutas
        servlet.register("GET", "/productos", (req, vars) ->
            new Response(200, "[{\"id\":1,\"nombre\":\"Teclado\"},{\"id\":2,\"nombre\":\"Raton\"}]"));

        servlet.register("GET", "/productos/{id}", (req, vars) ->
            new Response(200, "{\"id\":" + vars.get("id") + ",\"nombre\":\"Producto " + vars.get("id") + "\"}"));

        servlet.register("POST", "/productos", (req, vars) ->
            new Response(201, "{\"mensaje\":\"Producto creado\",\"nombre\":\"" + req.body().get("nombre") + "\"}"));

        // Pruebas
        Map<String, String> authHeaders = Map.of("Authorization", "Bearer token-valid-123");

        System.out.println("=== 1. GET /productos (request válida) ===");
        System.out.println(servlet.dispatch(new Request("GET", "/productos", authHeaders, Map.of())));

        System.out.println("\n=== 2. GET /productos/7 (path variable) ===");
        System.out.println(servlet.dispatch(new Request("GET", "/productos/7", authHeaders, Map.of())));

        System.out.println("\n=== 3. POST /productos sin token (401) ===");
        System.out.println(servlet.dispatch(new Request("POST", "/productos", Map.of(),
            Map.of("nombre", "Monitor", "precio", "299"))));

        System.out.println("\n=== 4. POST /productos sin campo 'precio' (400) ===");
        System.out.println(servlet.dispatch(new Request("POST", "/productos", authHeaders,
            Map.of("nombre", "Monitor"))));

        System.out.println("\n=== 5. POST /productos válido (201) ===");
        System.out.println(servlet.dispatch(new Request("POST", "/productos", authHeaders,
            Map.of("nombre", "Monitor", "precio", "299"))));

        System.out.println("\n=== 6. DELETE /productos/1 — ruta inexistente (404) ===");
        System.out.println(servlet.dispatch(new Request("DELETE", "/productos/1", authHeaders, Map.of())));
    }
}
