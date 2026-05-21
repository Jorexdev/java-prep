import java.time.Instant;
import java.util.*;

public class Ejercicio2 {

    static class GatewayRequest {
        final String clientId;
        final String apiKey;
        final String path;
        final String method;

        GatewayRequest(String clientId, String apiKey, String path, String method) {
            this.clientId = clientId;
            this.apiKey = apiKey;
            this.path = path;
            this.method = method;
        }
    }

    static class GatewayResponse {
        final int status;
        final String body;

        GatewayResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        @Override
        public String toString() {
            return status + " " + body;
        }
    }

    static class TokenBucket {
        private final int capacity;
        private int tokens;
        private long windowStartMs;
        private final long windowMs;

        TokenBucket(int capacity, long windowMs) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.windowMs = windowMs;
            this.windowStartMs = System.currentTimeMillis();
        }

        synchronized boolean consume() {
            long now = System.currentTimeMillis();
            if (now - windowStartMs >= windowMs) {
                tokens = capacity;
                windowStartMs = now;
            }
            if (tokens > 0) { tokens--; return true; }
            return false;
        }
    }

    static class Gateway {
        private final Map<String, String> routes = new LinkedHashMap<>();
        private final Set<String> validApiKeys = new HashSet<>();
        private final Map<String, TokenBucket> rateLimiters = new HashMap<>();
        private final int rateLimit;
        private final long windowMs;

        Gateway(int rateLimit, long windowMs) {
            this.rateLimit = rateLimit;
            this.windowMs = windowMs;
        }

        void addRoute(String prefix, String target) {
            routes.put(prefix, target);
        }

        void registerApiKey(String apiKey) {
            validApiKeys.add(apiKey);
        }

        GatewayResponse handle(GatewayRequest req) {
            String timestamp = Instant.now().toString();

            if (req.apiKey == null || !validApiKeys.contains(req.apiKey)) {
                log(timestamp, req, 401, "Unauthorized");
                return new GatewayResponse(401, "Unauthorized: API key inválida");
            }

            TokenBucket bucket = rateLimiters.computeIfAbsent(
                req.clientId, k -> new TokenBucket(rateLimit, windowMs));
            if (!bucket.consume()) {
                log(timestamp, req, 429, "Too Many Requests");
                return new GatewayResponse(429, "Rate limit excedido para: " + req.clientId);
            }

            String target = null;
            for (Map.Entry<String, String> entry : routes.entrySet()) {
                if (req.path.startsWith(entry.getKey())) {
                    target = entry.getValue();
                    break;
                }
            }
            if (target == null) {
                log(timestamp, req, 404, "Not Found");
                return new GatewayResponse(404, "Ruta no encontrada: " + req.path);
            }

            log(timestamp, req, 200, "→ " + target);
            return new GatewayResponse(200, "Redirigido a " + target + req.path);
        }

        private void log(String ts, GatewayRequest req, int status, String result) {
            System.out.printf("[%s] %s %s client=%s → %d %s%n",
                ts.substring(11, 23), req.method, req.path, req.clientId, status, result);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Gateway gateway = new Gateway(5, 60_000);
        gateway.addRoute("/inventario", "inventario-service:8081");
        gateway.addRoute("/pedidos", "pedidos-service:8082");
        gateway.addRoute("/usuarios", "usuarios-service:8083");
        gateway.registerApiKey("secret-key-123");
        gateway.registerApiKey("secret-key-456");

        System.out.println("=== Request válido ===");
        System.out.println(gateway.handle(new GatewayRequest("cliente-A", "secret-key-123", "/inventario/items", "GET")));

        System.out.println("\n=== Sin API key ===");
        System.out.println(gateway.handle(new GatewayRequest("cliente-B", null, "/pedidos/1", "GET")));

        System.out.println("\n=== API key inválida ===");
        System.out.println(gateway.handle(new GatewayRequest("cliente-B", "wrong-key", "/pedidos/1", "GET")));

        System.out.println("\n=== Rate limiting (6 requests, límite 5) ===");
        for (int i = 1; i <= 6; i++) {
            GatewayResponse resp = gateway.handle(
                new GatewayRequest("cliente-C", "secret-key-456", "/inventario/items", "GET"));
            System.out.println("Request " + i + ": " + resp);
        }

        System.out.println("\n=== Ruta no encontrada ===");
        System.out.println(gateway.handle(new GatewayRequest("cliente-A", "secret-key-123", "/desconocido/path", "GET")));
    }
}
