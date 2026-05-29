import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Ejercicio5 {

    // --- Modelos ---

    record HttpRequest(String method, String path, String ip,
                       Map<String, String> headers, String userId) {}

    static class HttpResponse {
        int status = 0;
        String body = "";
        @Override public String toString() { return status + " — " + body; }
    }

    // --- Interfaz de filtro con afterCompletion ---

    interface SecurityFilter {
        int order();
        // Devuelve false para cortar la cadena
        boolean doFilter(HttpRequest req, HttpResponse res, FilterContext ctx);
        // Siempre se llama al terminar, independientemente de si se cortó la cadena
        default void afterCompletion(HttpRequest req, HttpResponse res) {}
    }

    // Contexto mutable compartido entre filtros (bolsa de atributos)
    static class FilterContext {
        private final Map<String, Object> attrs = new HashMap<>();
        void set(String key, Object val) { attrs.put(key, val); }
        @SuppressWarnings("unchecked")
        <T> T get(String key) { return (T) attrs.get(key); }
    }

    // --- Filtro 1: CORS (orden 1) ---

    static class CorsFilter implements SecurityFilter {
        @Override public int order() { return 1; }

        @Override
        public boolean doFilter(HttpRequest req, HttpResponse res, FilterContext ctx) {
            System.out.println("  [CorsFilter] procesando " + req.method() + " " + req.path());
            if ("OPTIONS".equals(req.method())) {
                res.status = 204;
                res.body   = "CORS pre-flight OK";
                System.out.println("  [CorsFilter] OPTIONS — cortando cadena (204)");
                return false;
            }
            return true;
        }
    }

    // --- Filtro 2: CSRF (orden 2) ---

    static class CsrfFilter implements SecurityFilter {
        private final Map<String, String> sessionTokens; // sessionId → csrfToken

        CsrfFilter(Map<String, String> sessionTokens) { this.sessionTokens = sessionTokens; }

        @Override public int order() { return 2; }

        @Override
        public boolean doFilter(HttpRequest req, HttpResponse res, FilterContext ctx) {
            Set<String> mutating = Set.of("POST", "PUT", "DELETE", "PATCH");
            if (!mutating.contains(req.method())) return true;

            String sessionId = req.headers().get("X-Session-ID");
            String csrfToken = req.headers().get("X-CSRF-TOKEN");
            String expected  = sessionTokens.get(sessionId);

            if (expected == null || !expected.equals(csrfToken)) {
                res.status = 403;
                res.body   = "CSRF token inválido o ausente";
                System.out.println("  [CsrfFilter] CSRF inválido — cortando cadena (403)");
                return false;
            }
            System.out.println("  [CsrfFilter] CSRF válido");
            return true;
        }
    }

    // --- Filtro 3: Rate Limit Token Bucket (orden 3) ---

    static class RateLimitFilter implements SecurityFilter {
        private final int capacity;
        private final Map<String, int[]> buckets = new HashMap<>(); // ip → [tokens]

        RateLimitFilter(int capacity) { this.capacity = capacity; }

        @Override public int order() { return 3; }

        @Override
        public boolean doFilter(HttpRequest req, HttpResponse res, FilterContext ctx) {
            int[] bucket = buckets.computeIfAbsent(req.ip(), k -> new int[]{capacity});
            if (bucket[0] <= 0) {
                res.status = 429;
                res.body   = "Too Many Requests — rate limit excedido para IP: " + req.ip();
                System.out.println("  [RateLimitFilter] Rate limit superado para " + req.ip() + " — cortando (429)");
                return false;
            }
            bucket[0]--;
            System.out.println("  [RateLimitFilter] OK — tokens restantes para " + req.ip() + ": " + bucket[0]);
            return true;
        }
    }

    // --- Filtro 4: JWT Auth (orden 4) ---

    static class JwtAuthFilter implements SecurityFilter {
        private final Set<String> validTokens;

        JwtAuthFilter(Set<String> validTokens) { this.validTokens = validTokens; }

        @Override public int order() { return 4; }

        @Override
        public boolean doFilter(HttpRequest req, HttpResponse res, FilterContext ctx) {
            String auth = req.headers().getOrDefault("Authorization", "");
            if (!auth.startsWith("Bearer ")) {
                res.status = 401;
                res.body   = "Unauthorized — JWT requerido";
                System.out.println("  [JwtAuthFilter] Sin token — cortando cadena (401)");
                return false;
            }
            String token = auth.substring(7);
            if (!validTokens.contains(token)) {
                res.status = 401;
                res.body   = "Unauthorized — JWT inválido";
                System.out.println("  [JwtAuthFilter] Token inválido — cortando cadena (401)");
                return false;
            }
            ctx.set("authenticatedUser", req.userId());
            System.out.println("  [JwtAuthFilter] JWT válido — usuario: " + req.userId());
            return true;
        }
    }

    // --- Filtro 5: Audit (orden 5) — siempre registra via afterCompletion ---

    static class AuditFilter implements SecurityFilter {
        private final List<String> log = new ArrayList<>();

        @Override public int order() { return 5; }

        @Override
        public boolean doFilter(HttpRequest req, HttpResponse res, FilterContext ctx) {
            System.out.println("  [AuditFilter] request alcanzó la capa de auditoría");
            return true; // nunca corta
        }

        @Override
        public void afterCompletion(HttpRequest req, HttpResponse res) {
            String entry = "[AUDIT] " + req.method() + " " + req.path()
                + " from=" + req.ip()
                + " status=" + res.status;
            log.add(entry);
            System.out.println("  " + entry);
        }

        List<String> getLog() { return Collections.unmodifiableList(log); }
    }

    // --- SecurityFilterChain ---

    static class SecurityFilterChain {
        private final List<SecurityFilter> filters;
        private final AuditFilter auditFilter;

        SecurityFilterChain(List<SecurityFilter> filters, AuditFilter auditFilter) {
            // Ordenar por @Order
            this.filters = filters.stream()
                .sorted(Comparator.comparingInt(SecurityFilter::order))
                .toList();
            this.auditFilter = auditFilter;
        }

        HttpResponse execute(HttpRequest req) {
            HttpResponse res = new HttpResponse();
            FilterContext ctx = new FilterContext();

            for (SecurityFilter filter : filters) {
                boolean proceed = filter.doFilter(req, res, ctx);
                if (!proceed) break;
            }

            // afterCompletion siempre se ejecuta
            auditFilter.afterCompletion(req, res);
            if (res.status == 0) res.status = 200;
            return res;
        }
    }

    // --- Main ---

    public static void main(String[] args) {
        Map<String, String> csrfTokens = Map.of("sess-abc", "csrf-token-xyz");
        Set<String> validJwts          = Set.of("jwt-valid-alice", "jwt-valid-bob");
        AuditFilter auditFilter        = new AuditFilter();

        SecurityFilterChain chain = new SecurityFilterChain(List.of(
            new CorsFilter(),
            new CsrfFilter(csrfTokens),
            new RateLimitFilter(3),
            new JwtAuthFilter(validJwts),
            auditFilter
        ), auditFilter);

        Map<String, String> validHeaders = Map.of(
            "Authorization",  "Bearer jwt-valid-alice",
            "X-Session-ID",   "sess-abc",
            "X-CSRF-TOKEN",   "csrf-token-xyz"
        );

        System.out.println("=== 1. OPTIONS pre-flight — corta en CorsFilter (204) ===");
        System.out.println(chain.execute(
            new HttpRequest("OPTIONS", "/api/datos", "10.0.0.1", Map.of(), "anon")));

        System.out.println("\n=== 2. POST sin JWT — corta en JwtAuthFilter (401) ===");
        System.out.println(chain.execute(
            new HttpRequest("POST", "/api/pedidos", "10.0.0.2",
                Map.of("X-Session-ID", "sess-abc", "X-CSRF-TOKEN", "csrf-token-xyz"), "anon")));

        System.out.println("\n=== 3. POST con CSRF inválido — corta en CsrfFilter (403) ===");
        System.out.println(chain.execute(
            new HttpRequest("POST", "/api/pedidos", "10.0.0.3",
                Map.of("Authorization", "Bearer jwt-valid-alice",
                       "X-Session-ID",  "sess-abc",
                       "X-CSRF-TOKEN",  "token-equivocado"), "alice")));

        System.out.println("\n=== 4. Request válida — pasa toda la cadena (200) ===");
        System.out.println(chain.execute(
            new HttpRequest("POST", "/api/pedidos", "10.0.0.4", validHeaders, "alice")));

        System.out.println("\n=== 5. Rate limit: misma IP agota tokens (429) ===");
        // IP 10.0.0.5 ya sin tokens (capacity=3, ya usó 3)
        for (int i = 0; i < 3; i++) {
            chain.execute(new HttpRequest("GET", "/api/productos", "10.0.0.5", validHeaders, "bob"));
        }
        System.out.println("  [Cuarta request — rate limit agotado]");
        System.out.println(chain.execute(
            new HttpRequest("GET", "/api/productos", "10.0.0.5", validHeaders, "bob")));

        System.out.println("\n=== Log de auditoría completo ===");
        auditFilter.getLog().forEach(System.out::println);
    }
}
