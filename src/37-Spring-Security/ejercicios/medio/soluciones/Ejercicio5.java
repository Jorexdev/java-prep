import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ejercicio5 {

    record Request(String method, String path, Map<String, String> headers, String userId) {}

    static class Response {
        int status;
        String body;
        Response(int status, String body) { this.status = status; this.body = body; }
    }

    interface Filter {
        boolean doFilter(Request req, Response res, FilterChain chain);
    }

    static class FilterChain {
        private final List<Filter> filters;
        private int index = 0;

        FilterChain(List<Filter> filters) { this.filters = new ArrayList<>(filters); }

        boolean next(Request req, Response res) {
            if (index >= filters.size()) return true;
            return filters.get(index++).doFilter(req, res, this);
        }
    }

    static class CorsFilter implements Filter {
        @Override
        public boolean doFilter(Request req, Response res, FilterChain chain) {
            System.out.println("[CorsFilter] " + req.method() + " " + req.path());
            res.body = (res.body == null ? "" : res.body);

            if ("OPTIONS".equals(req.method())) {
                res.status = 204;
                res.body = "CORS pre-flight OK";
                System.out.println("[CorsFilter] OPTIONS — respondiendo 204, cortando cadena");
                return false;
            }

            return chain.next(req, res);
        }
    }

    static class JwtAuthFilter implements Filter {
        @Override
        public boolean doFilter(Request req, Response res, FilterChain chain) {
            String auth = req.headers().get("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                res.status = 401;
                res.body = "Unauthorized: JWT requerido";
                System.out.println("[JwtAuthFilter] Sin token — cortando cadena (401)");
                return false;
            }
            System.out.println("[JwtAuthFilter] Token presente — OK");
            return chain.next(req, res);
        }
    }

    static class AuthorizationFilter implements Filter {
        private final Map<String, List<String>> userRoles;

        AuthorizationFilter(Map<String, List<String>> userRoles) {
            this.userRoles = userRoles;
        }

        @Override
        public boolean doFilter(Request req, Response res, FilterChain chain) {
            List<String> roles = userRoles.getOrDefault(req.userId(), List.of());

            if (req.path().startsWith("/admin") && !roles.contains("ADMIN")) {
                res.status = 403;
                res.body = "Forbidden: se requiere rol ADMIN";
                System.out.println("[AuthorizationFilter] Sin permisos para " + req.path() + " — cortando (403)");
                return false;
            }
            System.out.println("[AuthorizationFilter] Autorizado para " + req.path());
            return chain.next(req, res);
        }
    }

    static class LoggingFilter implements Filter {
        @Override
        public boolean doFilter(Request req, Response res, FilterChain chain) {
            boolean result = chain.next(req, res);
            if (res.status == 0) res.status = 200;
            System.out.println("[LoggingFilter] " + req.method() + " " + req.path()
                    + " → " + res.status);
            return result;
        }
    }

    static class SecurityFilterChain {
        private final List<Filter> filters;

        SecurityFilterChain(List<Filter> filters) { this.filters = filters; }

        Response execute(Request req) {
            Response res = new Response(0, "");
            new FilterChain(filters).next(req, res);
            return res;
        }
    }

    public static void main(String[] args) {
        Map<String, List<String>> roles = Map.of(
                "user-basic", List.of("USER"),
                "user-admin", List.of("USER", "ADMIN"));

        SecurityFilterChain chain = new SecurityFilterChain(List.of(
                new CorsFilter(),
                new JwtAuthFilter(),
                new AuthorizationFilter(roles),
                new LoggingFilter()));

        System.out.println("=== 1. Sin token (corta en JwtAuthFilter) ===");
        Response r1 = chain.execute(new Request("GET", "/admin/panel", Map.of(), "user-basic"));
        System.out.println("Resultado: " + r1.status + " — " + r1.body + "\n");

        System.out.println("=== 2. Token presente, sin permisos (corta en AuthorizationFilter) ===");
        Response r2 = chain.execute(new Request("GET", "/admin/panel",
                Map.of("Authorization", "Bearer token123"), "user-basic"));
        System.out.println("Resultado: " + r2.status + " — " + r2.body + "\n");

        System.out.println("=== 3. Request válido con permisos (pasa toda la cadena) ===");
        Response r3 = chain.execute(new Request("GET", "/admin/panel",
                Map.of("Authorization", "Bearer token456"), "user-admin"));
        System.out.println("Resultado: " + r3.status + " — " + r3.body + "\n");

        System.out.println("=== 4. OPTIONS pre-flight (pasa CORS, corta antes de JWT) ===");
        Response r4 = chain.execute(new Request("OPTIONS", "/api/datos", Map.of(), ""));
        System.out.println("Resultado: " + r4.status + " — " + r4.body);
    }
}
