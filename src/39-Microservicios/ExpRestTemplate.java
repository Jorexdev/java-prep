import java.util.*;
import java.util.function.Function;

// RestTemplate — cliente HTTP síncrono de Spring (legacy).
//
// Por qué está deprecated:
//   Spring 5 introdujo WebClient (reactivo y no bloqueante).
//   RestTemplate permanece por compatibilidad pero no recibe nuevas features.
//   Para código nuevo: usar WebClient (incluso si no necesitas reactive) o RestClient
//   (wrapper síncrono sobre WebClient, disponible desde Spring 6.1).
//
// Métodos clave:
//   getForObject(url, Class)            → GET, deserializa body al tipo dado
//   postForEntity(url, body, Class)     → POST, devuelve ResponseEntity completo
//   exchange(url, method, entity, Class)→ máximo control: cualquier método + headers
//
// Configuración de timeout:
//   SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//   factory.setConnectTimeout(3000);  // ms hasta establecer conexión
//   factory.setReadTimeout(5000);     // ms esperando respuesta
//   new RestTemplate(factory);
//
// Interceptors (ClientHttpRequestInterceptor):
//   → logging de requests/responses
//   → inyección de Authorization header (propagación de tokens)
//
// ErrorHandler (ResponseErrorHandler):
//   → por defecto lanza HttpClientErrorException para 4xx y HttpServerErrorException para 5xx
//   → implementación propia para mapear errores a excepciones de negocio

// ── Modelos ───────────────────────────────────────────────────────────────────

record HttpRequest(String method, String url, Map<String, String> headers, String body) {}

record HttpResponse(int status, String body, Map<String, String> headers) {
    boolean isSuccessful() { return status >= 200 && status < 300; }
    boolean is4xx()        { return status >= 400 && status < 500; }
    boolean is5xx()        { return status >= 500; }
}

// ── Interceptor simulado ──────────────────────────────────────────────────────

// Equivale a ClientHttpRequestInterceptor.intercept(request, body, execution)
// Se encadena: request → interceptor1 → interceptor2 → ... → servidor
interface HttpInterceptor {
    HttpResponse intercept(HttpRequest req, Function<HttpRequest, HttpResponse> chain);
}

class LoggingInterceptor implements HttpInterceptor {
    @Override
    public HttpResponse intercept(HttpRequest req, Function<HttpRequest, HttpResponse> chain) {
        System.out.println("  [LOG] → " + req.method() + " " + req.url());
        if (req.body() != null) System.out.println("  [LOG]    body: " + req.body());
        HttpResponse resp = chain.apply(req);
        System.out.println("  [LOG] ← " + resp.status() + " " + resp.body());
        return resp;
    }
}

class AuthInterceptor implements HttpInterceptor {
    private final String token;

    AuthInterceptor(String token) { this.token = token; }

    @Override
    public HttpResponse intercept(HttpRequest req, Function<HttpRequest, HttpResponse> chain) {
        // Propaga el token JWT a todos los requests salientes
        Map<String, String> headers = new LinkedHashMap<>(req.headers());
        headers.put("Authorization", "Bearer " + token);
        HttpRequest enriched = new HttpRequest(req.method(), req.url(), headers, req.body());
        System.out.println("  [Auth] Añadido Authorization header");
        return chain.apply(enriched);
    }
}

// ── ErrorHandler simulado ─────────────────────────────────────────────────────

// DefaultResponseErrorHandler de Spring lanza:
//   HttpClientErrorException → 4xx
//   HttpServerErrorException → 5xx
// Con un handler propio se mapea a excepciones de negocio.
class RestErrorHandler {
    void handleError(HttpResponse response) {
        if (response.is4xx()) {
            if (response.status() == 404) {
                throw new NoSuchElementException("Recurso no encontrado (404): " + response.body());
            }
            throw new IllegalArgumentException("Error del cliente (" + response.status() + "): " + response.body());
        }
        if (response.is5xx()) {
            throw new RuntimeException("Error del servidor (" + response.status() + "): " + response.body());
        }
    }
}

// ── RestTemplate simulado ─────────────────────────────────────────────────────

// Simula el comportamiento de org.springframework.web.client.RestTemplate.
// En producción: @Bean RestTemplate restTemplate(RestTemplateBuilder builder)
class RestTemplate {
    private final List<HttpInterceptor> interceptors = new ArrayList<>();
    private final RestErrorHandler errorHandler = new RestErrorHandler();
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    // SimpleClientHttpRequestFactory conecta directamente (sin connection pool).
    // Para producción con pool: HttpComponentsClientHttpRequestFactory (Apache HC).
    RestTemplate(int connectTimeoutMs, int readTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs    = readTimeoutMs;
    }

    void addInterceptor(HttpInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    // ── Simulación de red ────────────────────────────────────────────────────

    // Simula el servidor remoto con respuestas predefinidas por URL
    private static final Map<String, HttpResponse> MOCK_SERVER = Map.of(
        "GET /api/usuarios/1",   new HttpResponse(200, "{\"id\":1,\"nombre\":\"Ana\"}",   Map.of()),
        "GET /api/usuarios/99",  new HttpResponse(404, "Usuario no encontrado",           Map.of()),
        "POST /api/usuarios",    new HttpResponse(201, "{\"id\":3,\"nombre\":\"Carlos\"}", Map.of()),
        "PUT /api/usuarios/1",   new HttpResponse(200, "{\"id\":1,\"nombre\":\"Ana M.\"}", Map.of()),
        "GET /api/error",        new HttpResponse(500, "Internal Server Error",            Map.of())
    );

    private HttpResponse executeNetwork(HttpRequest req) {
        // Simula el límite de readTimeout para el servidor de error
        if (readTimeoutMs < 1000 && req.url().contains("timeout")) {
            throw new RuntimeException("ReadTimeout tras " + readTimeoutMs + "ms");
        }
        String key = req.method() + " " + req.url().replace("http://api.example.com", "");
        return MOCK_SERVER.getOrDefault(key, new HttpResponse(404, "Not Found", Map.of()));
    }

    private HttpResponse execute(HttpRequest req) {
        // Construir la cadena de interceptors → red
        Function<HttpRequest, HttpResponse> chain = this::executeNetwork;
        // Aplicar interceptors en orden inverso (último añadido, primero ejecutado hacia red)
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            final HttpInterceptor interceptor = interceptors.get(i);
            final Function<HttpRequest, HttpResponse> next = chain;
            chain = r -> interceptor.intercept(r, next);
        }
        HttpResponse response = chain.apply(req);
        if (!response.isSuccessful()) {
            errorHandler.handleError(response);
        }
        return response;
    }

    // ── API pública simulada ─────────────────────────────────────────────────

    // getForObject → GET, devuelve el body deserializado (null si body vacío)
    String getForObject(String url) {
        HttpRequest req = new HttpRequest("GET", url, Map.of(), null);
        return execute(req).body();
    }

    // postForEntity → POST, devuelve ResponseEntity con status + body + headers
    HttpResponse postForEntity(String url, String body) {
        HttpRequest req = new HttpRequest("POST", url, Map.of(), body);
        return execute(req);
    }

    // exchange → máximo control (método, headers personalizados, body)
    HttpResponse exchange(String url, String method, Map<String, String> headers, String body) {
        HttpRequest req = new HttpRequest(method, url, headers, body);
        return execute(req);
    }
}

// ── Main ──────────────────────────────────────────────────────────────────────

public class ExpRestTemplate {

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  ExpRestTemplate — Cliente HTTP síncrono (legacy)");
        System.out.println("═".repeat(60));

        // Configuración: timeouts + interceptors
        // En Spring real: RestTemplateBuilder.connectTimeout(3s).readTimeout(5s)
        RestTemplate client = new RestTemplate(3000, 5000);
        client.addInterceptor(new AuthInterceptor("mi-jwt-token-123"));
        client.addInterceptor(new LoggingInterceptor());

        // ── Caso 1: getForObject → 200 ───────────────────────────────────────
        System.out.println("\n── Caso 1: getForObject → 200 OK ────────────────────────────");
        String usuario = client.getForObject("http://api.example.com/api/usuarios/1");
        System.out.println("  Respuesta: " + usuario);

        // ── Caso 2: postForEntity → 201 Created ──────────────────────────────
        System.out.println("\n── Caso 2: postForEntity → 201 Created ──────────────────────");
        HttpResponse created = client.postForEntity(
            "http://api.example.com/api/usuarios",
            "{\"nombre\":\"Carlos\"}");
        System.out.println("  Status: " + created.status() + " | Body: " + created.body());

        // ── Caso 3: exchange con headers personalizados ───────────────────────
        System.out.println("\n── Caso 3: exchange PUT con X-Request-ID ────────────────────");
        HttpResponse updated = client.exchange(
            "http://api.example.com/api/usuarios/1", "PUT",
            Map.of("X-Request-ID", "req-abc123"),
            "{\"nombre\":\"Ana M.\"}");
        System.out.println("  Status: " + updated.status() + " | Body: " + updated.body());

        // ── Caso 4: ErrorHandler → 404 lanza excepción ───────────────────────
        System.out.println("\n── Caso 4: ErrorHandler → 404 mapeado a excepción ──────────");
        try {
            client.getForObject("http://api.example.com/api/usuarios/99");
        } catch (NoSuchElementException e) {
            System.out.println("  Excepción capturada: " + e.getMessage());
        }

        // ── Caso 5: ErrorHandler → 500 ───────────────────────────────────────
        System.out.println("\n── Caso 5: ErrorHandler → 500 Server Error ──────────────────");
        try {
            client.getForObject("http://api.example.com/api/error");
        } catch (RuntimeException e) {
            System.out.println("  Excepción capturada: " + e.getMessage());
        }

        // ── Resumen ──────────────────────────────────────────────────────────
        System.out.println("\n── Resumen ───────────────────────────────────────────────────");
        System.out.println("  getForObject  → GET simple, body directo, sin acceso a headers");
        System.out.println("  postForEntity → POST, ResponseEntity con status + headers");
        System.out.println("  exchange      → control total (método, headers, body, tipo)");
        System.out.println("  Interceptor   → cross-cutting concerns (logging, auth, tracing)");
        System.out.println("  ErrorHandler  → convierte HTTP errors en excepciones de negocio");
        System.out.println("  Timeout       → connectTimeout (TCP) y readTimeout (espera body)");
        System.out.println("  Deprecado:    → usar WebClient o RestClient (Spring 6.1+)");
        System.out.println("  1 thread/req: → bajo carga bloqueante; WebClient usa event loop");
        System.out.println("═".repeat(60));
    }
}
