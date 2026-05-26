import java.util.*;
import java.util.function.*;

/**
 * Simulación de API Gateway pattern con Java puro.
 *
 * Componentes:
 *  - ServiceRegistry: catálogo de servicios disponibles
 *  - RouteFilter: cadena de filtros (auth, rate limiter, logger, circuit breaker)
 *  - Gateway: enruta requests y ejecuta la cadena de filtros
 *  - Agregación: un request del gateway llama a varios servicios y fusiona la respuesta
 */
public class ExpAPIGateway {

    // ─────────────────────────────────────────────
    // REQUEST / RESPONSE
    // ─────────────────────────────────────────────

    static class GatewayRequest {
        private final String path;
        private final String method;
        private final String clientId;
        private final Map<String, String> headers;

        GatewayRequest(String path, String method, String clientId) {
            this.path = path;
            this.method = method;
            this.clientId = clientId;
            this.headers = new LinkedHashMap<>();
        }

        String path() { return path; }
        String method() { return method; }
        String clientId() { return clientId; }
        Map<String, String> headers() { return headers; }
        void addHeader(String k, String v) { headers.put(k, v); }

        @Override
        public String toString() {
            return String.format("%s %s (client='%s')", method, path, clientId);
        }
    }

    record GatewayResponse(int status, String body) {
        boolean ok() { return status >= 200 && status < 300; }

        @Override
        public String toString() {
            return String.format("HTTP %d — %s", status, body);
        }
    }

    // ─────────────────────────────────────────────
    // SERVICE REGISTRY
    // ─────────────────────────────────────────────

    static class ServiceRegistry {
        private final Map<String, String> services = new LinkedHashMap<>();
        private final Set<String> downServices = new HashSet<>();

        void register(String name, String url) {
            services.put(name, url);
        }

        void markDown(String name) {
            downServices.add(name);
            System.out.printf("  [ServiceRegistry] '%s' marcado como DOWN%n", name);
        }

        boolean isUp(String name) { return !downServices.contains(name); }
        String urlOf(String name) { return services.getOrDefault(name, "unknown"); }
        Set<String> allServices() { return services.keySet(); }
    }

    // ─────────────────────────────────────────────
    // BACKEND SERVICE SIMULADO
    // ─────────────────────────────────────────────

    static class BackendService {
        private final String name;
        private final ServiceRegistry registry;

        BackendService(String name, ServiceRegistry registry) {
            this.name = name;
            this.registry = registry;
        }

        GatewayResponse call(String endpoint) {
            if (!registry.isUp(name)) {
                throw new RuntimeException("Service '" + name + "' no disponible (DOWN)");
            }
            // Simula respuesta del backend
            return new GatewayResponse(200,
                    String.format("{\"service\":\"%s\",\"endpoint\":\"%s\",\"data\":\"ok\"}", name, endpoint));
        }
    }

    // ─────────────────────────────────────────────
    // FILTROS DE GATEWAY
    // ─────────────────────────────────────────────

    // Un filtro devuelve null para continuar la cadena, o un GatewayResponse para cortocircuitar
    interface RouteFilter {
        GatewayResponse filter(GatewayRequest request, Supplier<GatewayResponse> next);
        String nombre();
    }

    // Filtro 1: Autenticación — rechaza si no hay token
    static class AuthFilter implements RouteFilter {
        private final Set<String> validTokens = Set.of("token-A", "token-B", "token-admin");

        @Override
        public GatewayResponse filter(GatewayRequest request, Supplier<GatewayResponse> next) {
            String token = request.headers().get("Authorization");
            System.out.printf("    [AuthFilter] client='%s' token='%s'%n",
                    request.clientId(), token != null ? token : "(no token)");
            if (token == null || !validTokens.contains(token)) {
                System.out.println("    [AuthFilter] RECHAZADO: token inválido");
                return new GatewayResponse(401, "{\"error\":\"Unauthorized\"}");
            }
            System.out.println("    [AuthFilter] OK");
            return next.get();
        }

        @Override public String nombre() { return "AuthFilter"; }
    }

    // Filtro 2: Rate limiter — máximo N requests por cliente
    static class RateLimiterFilter implements RouteFilter {
        private final int maxPerClient;
        private final Map<String, Integer> counters = new HashMap<>();

        RateLimiterFilter(int maxPerClient) {
            this.maxPerClient = maxPerClient;
        }

        @Override
        public GatewayResponse filter(GatewayRequest request, Supplier<GatewayResponse> next) {
            int count = counters.merge(request.clientId(), 1, Integer::sum);
            System.out.printf("    [RateLimiter] client='%s' requests=%d/%d%n",
                    request.clientId(), count, maxPerClient);
            if (count > maxPerClient) {
                System.out.println("    [RateLimiter] RECHAZADO: límite superado");
                return new GatewayResponse(429, "{\"error\":\"Too Many Requests\"}");
            }
            System.out.println("    [RateLimiter] OK");
            return next.get();
        }

        @Override public String nombre() { return "RateLimiter(max=" + maxPerClient + ")"; }
    }

    // Filtro 3: Logger
    static class RequestLoggerFilter implements RouteFilter {
        private int requestCount = 0;

        @Override
        public GatewayResponse filter(GatewayRequest request, Supplier<GatewayResponse> next) {
            int id = ++requestCount;
            long start = System.nanoTime();
            System.out.printf("    [Logger] → REQ#%d %s%n", id, request);
            GatewayResponse response = next.get();
            long elapsed = (System.nanoTime() - start) / 1_000; // microsegundos
            System.out.printf("    [Logger] ← REQ#%d %s (%dµs)%n", id, response, elapsed);
            return response;
        }

        @Override public String nombre() { return "RequestLogger"; }
    }

    // Filtro 4: Circuit Breaker — abre el circuito tras N fallos consecutivos
    static class CircuitBreakerFilter implements RouteFilter {
        private final String serviceName;
        private final int failureThreshold;
        private int consecutiveFailures = 0;
        private boolean open = false;

        CircuitBreakerFilter(String serviceName, int failureThreshold) {
            this.serviceName = serviceName;
            this.failureThreshold = failureThreshold;
        }

        @Override
        public GatewayResponse filter(GatewayRequest request, Supplier<GatewayResponse> next) {
            if (open) {
                System.out.printf("    [CircuitBreaker '%s'] OPEN → fallback directo%n", serviceName);
                return new GatewayResponse(503, "{\"error\":\"Service Unavailable (circuit open)\"}");
            }
            System.out.printf("    [CircuitBreaker '%s'] CLOSED — pasando request%n", serviceName);
            try {
                GatewayResponse response = next.get();
                if (!response.ok()) {
                    recordFailure();
                } else {
                    consecutiveFailures = 0; // reset en éxito
                }
                return response;
            } catch (Exception e) {
                recordFailure();
                return new GatewayResponse(502, "{\"error\":\"Bad Gateway\"}");
            }
        }

        private void recordFailure() {
            consecutiveFailures++;
            System.out.printf("    [CircuitBreaker '%s'] Fallo %d/%d%n",
                    serviceName, consecutiveFailures, failureThreshold);
            if (consecutiveFailures >= failureThreshold) {
                open = true;
                System.out.printf("    [CircuitBreaker '%s'] *** CIRCUITO ABIERTO ***%n", serviceName);
            }
        }

        @Override public String nombre() { return "CircuitBreaker(" + serviceName + ")"; }
    }

    // ─────────────────────────────────────────────
    // GATEWAY — enruta y aplica filtros en cadena
    // ─────────────────────────────────────────────

    static class Gateway {
        // pathPrefix → serviceName
        private final Map<String, String> routes = new LinkedHashMap<>();
        private final List<RouteFilter> filters = new ArrayList<>();
        private final ServiceRegistry registry;
        private final Map<String, BackendService> services = new LinkedHashMap<>();

        Gateway(ServiceRegistry registry) {
            this.registry = registry;
        }

        void addRoute(String pathPrefix, String serviceName) {
            routes.put(pathPrefix, serviceName);
        }

        void addFilter(RouteFilter filter) {
            filters.add(filter);
        }

        void registerBackend(BackendService service) {
            services.put(service.name, service);
        }

        GatewayResponse handle(GatewayRequest request) {
            System.out.printf("%n  [Gateway] %s%n", request);
            System.out.println("  " + "─".repeat(50));

            // Resolver ruta
            String serviceName = resolveRoute(request.path());
            if (serviceName == null) {
                System.out.println("    [Gateway] Ruta no encontrada");
                return new GatewayResponse(404, "{\"error\":\"Not Found\"}");
            }
            System.out.printf("    [Gateway] Ruta → servicio '%s'%n", serviceName);

            // Ejecutar cadena de filtros (los filtros se encadenan como lambdas recursivas)
            BackendService backend = services.get(serviceName);
            Supplier<GatewayResponse> handler = () -> {
                try {
                    return backend.call(request.path());
                } catch (Exception e) {
                    return new GatewayResponse(502, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            };

            // Envolver la cadena de filtros de atrás hacia adelante
            for (int i = filters.size() - 1; i >= 0; i--) {
                RouteFilter filter = filters.get(i);
                Supplier<GatewayResponse> next = handler;
                handler = () -> filter.filter(request, next);
            }

            GatewayResponse response = handler.get();
            System.out.printf("  [Gateway] Respuesta final: %s%n", response);
            return response;
        }

        // Agregación: llama a múltiples servicios y fusiona las respuestas
        GatewayResponse aggregate(GatewayRequest request, List<String> serviceNames) {
            System.out.printf("%n  [Gateway AGGREGATE] %s → %s%n", request, serviceNames);
            System.out.println("  " + "─".repeat(50));
            List<String> parts = new ArrayList<>();
            for (String svcName : serviceNames) {
                BackendService svc = services.get(svcName);
                if (svc == null) continue;
                try {
                    GatewayResponse r = svc.call(request.path());
                    parts.add(r.body());
                    System.out.printf("    [Aggregate] %s → %s%n", svcName, r.body());
                } catch (Exception e) {
                    parts.add(String.format("{\"service\":\"%s\",\"error\":\"%s\"}", svcName, e.getMessage()));
                    System.out.printf("    [Aggregate] %s → ERROR: %s%n", svcName, e.getMessage());
                }
            }
            String merged = "{\"results\":[" + String.join(",", parts) + "]}";
            return new GatewayResponse(200, merged);
        }

        private String resolveRoute(String path) {
            // Buscar el prefijo más largo que coincida (más específico primero)
            return routes.entrySet().stream()
                    .filter(e -> path.startsWith(e.getKey()))
                    .max(Comparator.comparingInt(e -> e.getKey().length()))
                    .map(Map.Entry::getValue)
                    .orElse(null);
        }
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  API GATEWAY — Java puro");
        System.out.println("═".repeat(65));

        // Setup
        ServiceRegistry registry = new ServiceRegistry();
        registry.register("orders",    "http://orders-svc:8080");
        registry.register("inventory", "http://inventory-svc:8081");
        registry.register("users",     "http://users-svc:8082");

        BackendService ordersSvc    = new BackendService("orders", registry);
        BackendService inventorySvc = new BackendService("inventory", registry);
        BackendService usersSvc     = new BackendService("users", registry);

        Gateway gateway = new Gateway(registry);
        gateway.addRoute("/api/orders",    "orders");
        gateway.addRoute("/api/inventory", "inventory");
        gateway.addRoute("/api/users",     "users");

        gateway.registerBackend(ordersSvc);
        gateway.registerBackend(inventorySvc);
        gateway.registerBackend(usersSvc);

        gateway.addFilter(new RequestLoggerFilter());
        gateway.addFilter(new AuthFilter());
        gateway.addFilter(new RateLimiterFilter(3));
        gateway.addFilter(new CircuitBreakerFilter("orders", 2));

        // ── Demo 1: request autenticado normal ────────────────────────
        System.out.println("\n══ DEMO 1: Request autenticado ══");
        GatewayRequest req1 = new GatewayRequest("/api/orders/123", "GET", "client-1");
        req1.addHeader("Authorization", "token-A");
        gateway.handle(req1);

        // ── Demo 2: request sin token ──────────────────────────────────
        System.out.println("\n══ DEMO 2: Request sin autenticación ══");
        GatewayRequest req2 = new GatewayRequest("/api/users/profile", "GET", "client-anon");
        gateway.handle(req2);

        // ── Demo 3: rate limit superado ───────────────────────────────
        System.out.println("\n══ DEMO 3: Rate limit (máx 3 por cliente) ══");
        for (int i = 1; i <= 4; i++) {
            GatewayRequest req = new GatewayRequest("/api/inventory", "GET", "heavy-client");
            req.addHeader("Authorization", "token-B");
            System.out.printf("\n  Intento %d:%n", i);
            gateway.handle(req);
        }

        // ── Demo 4: servicio caído → circuit breaker ──────────────────
        System.out.println("\n══ DEMO 4: Circuit breaker — servicio caído ══");
        registry.markDown("orders");
        for (int i = 1; i <= 3; i++) {
            GatewayRequest req = new GatewayRequest("/api/orders/456", "GET", "client-2");
            req.addHeader("Authorization", "token-admin");
            System.out.printf("\n  Request #%d (orders DOWN):%n", i);
            gateway.handle(req);
        }

        // ── Demo 5: agregación ─────────────────────────────────────────
        System.out.println("\n══ DEMO 5: Agregación (fan-out a 2 servicios) ══");
        registry = new ServiceRegistry(); // fresh registry, todos UP
        registry.register("inventory", "http://inventory-svc:8081");
        registry.register("users",     "http://users-svc:8082");
        BackendService inv2  = new BackendService("inventory", registry);
        BackendService usr2  = new BackendService("users",     registry);
        Gateway gw2 = new Gateway(registry);
        gw2.registerBackend(inv2);
        gw2.registerBackend(usr2);

        GatewayRequest aggReq = new GatewayRequest("/api/dashboard", "GET", "client-admin");
        GatewayResponse aggregated = gw2.aggregate(aggReq, List.of("inventory", "users"));
        System.out.printf("  Respuesta agregada: %s%n", aggregated.body());

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN API GATEWAY");
        System.out.println("═".repeat(65));
        System.out.println("  El gateway es el único punto de entrada para los clientes.");
        System.out.println("  Responsabilidades cross-cutting: auth, rate limit, logging, CB.");
        System.out.println("  Los microservicios backend no necesitan implementar estas capas.");
        System.out.println("  Agregación: reduce el número de llamadas del cliente (BFF pattern).");
        System.out.println("═".repeat(65));
    }
}
