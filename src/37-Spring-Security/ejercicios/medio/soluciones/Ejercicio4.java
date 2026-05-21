import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Ejercicio4 {

    record Request(String method, String path, Map<String, String> headers, String sessionId) {}

    record Response(int status, String body) {}

    static class CsrfTokenRepository {
        private final Map<String, String> tokens = new HashMap<>();

        public String generateToken(String sessionId) {
            String token = UUID.randomUUID().toString();
            tokens.put(sessionId, token);
            return token;
        }

        public boolean isValid(String sessionId, String token) {
            return token != null && token.equals(tokens.get(sessionId));
        }

        public String getToken(String sessionId) {
            return tokens.get(sessionId);
        }
    }

    static class CsrfFilter {
        private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
        private final CsrfTokenRepository repo;

        CsrfFilter(CsrfTokenRepository repo) { this.repo = repo; }

        public Response filter(Request req) {
            if (SAFE_METHODS.contains(req.method())) {
                String token = repo.generateToken(req.sessionId());
                System.out.println("[CsrfFilter] " + req.method() + " " + req.path()
                        + " — PASS (token generado: " + token + ")");
                return new Response(200, "OK — CSRF token: " + token);
            }

            String headerToken = req.headers().get("X-CSRF-TOKEN");
            if (!repo.isValid(req.sessionId(), headerToken)) {
                System.out.println("[CsrfFilter] " + req.method() + " " + req.path()
                        + " — BLOQUEADO (CSRF inválido)");
                return new Response(403, "Forbidden: CSRF token inválido o ausente");
            }

            System.out.println("[CsrfFilter] " + req.method() + " " + req.path() + " — PASS");
            return new Response(200, "OK");
        }
    }

    public static void main(String[] args) {
        CsrfTokenRepository repo = new CsrfTokenRepository();
        CsrfFilter filter = new CsrfFilter(repo);
        String session = "sess-abc123";

        System.out.println("--- 1. GET /perfil (siempre pasa) ---");
        Response r1 = filter.filter(new Request("GET", "/perfil", Map.of(), session));
        System.out.println("Status: " + r1.status() + " | " + r1.body());

        System.out.println();
        System.out.println("--- 2. POST /datos sin token ---");
        Response r2 = filter.filter(new Request("POST", "/datos", Map.of(), session));
        System.out.println("Status: " + r2.status() + " | " + r2.body());

        System.out.println();
        System.out.println("--- 3. GET /form para obtener token ---");
        Response r3 = filter.filter(new Request("GET", "/form", Map.of(), session));
        String csrfToken = repo.getToken(session);
        System.out.println("Status: " + r3.status() + " | Token obtenido: " + csrfToken);

        System.out.println();
        System.out.println("--- 4. POST /datos con token válido ---");
        Response r4 = filter.filter(new Request("POST", "/datos",
                Map.of("X-CSRF-TOKEN", csrfToken), session));
        System.out.println("Status: " + r4.status() + " | " + r4.body());
    }
}
