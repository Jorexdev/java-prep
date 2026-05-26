import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Simula HandlerInterceptor de Spring MVC.
// La cadena preHandle → handler → postHandle → afterCompletion se aplica en orden.
// Si preHandle retorna false, la cadena se interrumpe y afterCompletion no se llama
// para los interceptores que no llegaron a ejecutar postHandle.

// ── Petición / respuesta simuladas ───────────────────────────────────────────

class InterceptorRequest {
    final String method;
    final String path;
    final String ip;
    final Map<String, String> headers;

    InterceptorRequest(String method, String path, String ip, Map<String, String> headers) {
        this.method  = method;
        this.path    = path;
        this.ip      = ip;
        this.headers = headers != null ? headers : Map.of();
    }

    @Override
    public String toString() { return method + " " + path + " [ip=" + ip + "]"; }
}

class InterceptorResponse {
    int status = 200;
    String body = "";

    @Override
    public String toString() { return "HTTP " + status + (body.isEmpty() ? "" : " " + body); }
}

// ── Interfaz HandlerInterceptor ───────────────────────────────────────────────

// Equivale a org.springframework.web.servlet.HandlerInterceptor
interface HandlerInterceptor {
    // Retorna true → continuar cadena; false → abortar (ya escribió la respuesta)
    boolean preHandle(InterceptorRequest req, InterceptorResponse res, String handlerName);

    // Se llama DESPUÉS del handler, ANTES de renderizar la vista
    void postHandle(InterceptorRequest req, InterceptorResponse res, String handlerName);

    // Se llama SIEMPRE al final (incluso con excepción), si preHandle retornó true
    void afterCompletion(InterceptorRequest req, InterceptorResponse res, String handlerName, Exception ex);
}

// ── AuthInterceptor ───────────────────────────────────────────────────────────

// @Component  (Spring lo detecta y añade a la cadena vía WebMvcConfigurer#addInterceptors)
class AuthInterceptor implements HandlerInterceptor {

    private static final String VALID_TOKEN = "Bearer valid-token-123";

    @Override
    public boolean preHandle(InterceptorRequest req, InterceptorResponse res, String handlerName) {
        String auth = req.headers.get("Authorization");
        System.out.println("  [Auth  ] preHandle — Authorization: " + auth);
        if (!VALID_TOKEN.equals(auth)) {
            res.status = 401;
            res.body   = "{ \"error\": \"Unauthorized\" }";
            System.out.println("  [Auth  ] → 401 Rechazado. Cadena interrumpida.");
            return false;   // ← interrumpe la cadena
        }
        System.out.println("  [Auth  ] → Token válido, continúa.");
        return true;
    }

    @Override
    public void postHandle(InterceptorRequest req, InterceptorResponse res, String handlerName) {
        System.out.println("  [Auth  ] postHandle — OK");
    }

    @Override
    public void afterCompletion(InterceptorRequest req, InterceptorResponse res, String handlerName, Exception ex) {
        System.out.println("  [Auth  ] afterCompletion — limpieza de contexto de seguridad");
    }
}

// ── LoggingInterceptor ────────────────────────────────────────────────────────

class LoggingInterceptor implements HandlerInterceptor {

    // Almacena el instante de inicio por hilo (en Spring real sería ThreadLocal)
    private final Map<String, Long> startTimes = new HashMap<>();

    @Override
    public boolean preHandle(InterceptorRequest req, InterceptorResponse res, String handlerName) {
        long start = System.nanoTime();
        startTimes.put(req.path, start);
        System.out.println("  [Logger] preHandle — inicio " + req + " → handler=" + handlerName);
        return true;
    }

    @Override
    public void postHandle(InterceptorRequest req, InterceptorResponse res, String handlerName) {
        System.out.println("  [Logger] postHandle — handler completado, status=" + res.status);
    }

    @Override
    public void afterCompletion(InterceptorRequest req, InterceptorResponse res, String handlerName, Exception ex) {
        long elapsed = (System.nanoTime() - startTimes.getOrDefault(req.path, System.nanoTime())) / 1_000_000;
        System.out.println("  [Logger] afterCompletion — " + req.method + " " + req.path
            + " status=" + res.status + " elapsed=" + elapsed + "ms"
            + (ex != null ? " exception=" + ex.getMessage() : ""));
    }
}

// ── RateLimitInterceptor ──────────────────────────────────────────────────────

class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 3;
    private final Map<String, Integer> contadorPorIp = new HashMap<>();

    @Override
    public boolean preHandle(InterceptorRequest req, InterceptorResponse res, String handlerName) {
        int count = contadorPorIp.merge(req.ip, 1, Integer::sum);
        System.out.println("  [Rate  ] preHandle — ip=" + req.ip + " peticiones=" + count + "/" + MAX_REQUESTS);
        if (count > MAX_REQUESTS) {
            res.status = 429;
            res.body   = "{ \"error\": \"Too Many Requests\" }";
            System.out.println("  [Rate  ] → 429 Límite superado para ip=" + req.ip);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(InterceptorRequest req, InterceptorResponse res, String handlerName) {
        System.out.println("  [Rate  ] postHandle — OK");
    }

    @Override
    public void afterCompletion(InterceptorRequest req, InterceptorResponse res, String handlerName, Exception ex) {
        System.out.println("  [Rate  ] afterCompletion — registro auditado");
    }
}

// ── Dispatcher con cadena de interceptores ────────────────────────────────────

// Equivale a DispatcherServlet aplicando la cadena de HandlerInterceptor registrada
class InterceptorChain {

    private final List<HandlerInterceptor> interceptors;

    InterceptorChain(List<HandlerInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    void dispatch(InterceptorRequest req, String handlerName) {
        InterceptorResponse res = new InterceptorResponse();
        Exception handlerEx = null;

        // Lista de los interceptores cuyo preHandle devolvió true (para invocar afterCompletion)
        List<HandlerInterceptor> ejecutados = new ArrayList<>();

        System.out.println("  ── preHandle (orden: 1→N) ──");
        for (HandlerInterceptor interceptor : interceptors) {
            if (!interceptor.preHandle(req, res, handlerName)) {
                // Un interceptor abortó — invocar afterCompletion en orden inverso
                System.out.println("  ── afterCompletion (orden inverso, cadena abortada) ──");
                for (int i = ejecutados.size() - 1; i >= 0; i--) {
                    ejecutados.get(i).afterCompletion(req, res, handlerName, null);
                }
                System.out.println("  → Respuesta: " + res);
                return;
            }
            ejecutados.add(interceptor);
        }

        // Handler real ejecuta aquí
        System.out.println("  ── handler ── → ejecutando " + handlerName);
        res.body = "{ \"data\": \"ok\" }";

        System.out.println("  ── postHandle (orden inverso: N→1) ──");
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).postHandle(req, res, handlerName);
        }

        System.out.println("  ── afterCompletion (orden inverso: N→1) ──");
        for (int i = ejecutados.size() - 1; i >= 0; i--) {
            ejecutados.get(i).afterCompletion(req, res, handlerName, handlerEx);
        }

        System.out.println("  → Respuesta: " + res);
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpInterceptor {
    public static void main(String[] args) {

        InterceptorChain chain = new InterceptorChain(List.of(
            new AuthInterceptor(),
            new LoggingInterceptor(),
            new RateLimitInterceptor()
        ));

        System.out.println("=== Simulación HandlerInterceptor ===");
        System.out.println("Orden registrado: Auth → Logging → RateLimit\n");

        // ─── Caso 1: sin token → AuthInterceptor rechaza ─────────────────────
        System.out.println(">>> Caso 1: GET /api/productos — sin Authorization header");
        chain.dispatch(
            new InterceptorRequest("GET", "/api/productos", "192.168.1.10", Map.of()),
            "ProductoController#listar"
        );

        System.out.println();

        // ─── Caso 2: token válido → pasa los tres interceptores ──────────────
        System.out.println(">>> Caso 2: GET /api/productos — token válido");
        chain.dispatch(
            new InterceptorRequest("GET", "/api/productos", "10.0.0.1",
                Map.of("Authorization", "Bearer valid-token-123")),
            "ProductoController#listar"
        );

        System.out.println();

        // ─── Caso 3: rate limit — misma IP supera el límite ──────────────────
        System.out.println(">>> Caso 3: misma IP satura el rate limiter (4 peticiones, límite=3)");
        RateLimitInterceptor rateInterceptor = new RateLimitInterceptor();
        InterceptorChain chainConRate = new InterceptorChain(List.of(
            new AuthInterceptor(),
            new LoggingInterceptor(),
            rateInterceptor
        ));

        Map<String, String> validHeaders = Map.of("Authorization", "Bearer valid-token-123");
        for (int i = 1; i <= 4; i++) {
            System.out.println("--- Petición " + i + " ---");
            chainConRate.dispatch(
                new InterceptorRequest("GET", "/api/datos", "172.16.0.5", validHeaders),
                "DatosController#obtener"
            );
            System.out.println();
        }
    }
}
