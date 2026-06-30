import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

// WebClient — cliente HTTP reactivo y no bloqueante de Spring WebFlux.
//
// RestTemplate vs WebClient:
//   RestTemplate: 1 thread por request bloqueado en I/O (Servlet model).
//   WebClient:    event loop + callbacks → un hilo sirve miles de requests concurrentes.
//   Regla práctica: usa WebClient incluso en apps no-reactivas si haces muchas
//   llamadas externas; usa .block() con cuidado solo en código no-reactivo.
//
// WebClient.create() vs WebClient.builder():
//   create()  → instancia mínima con defaults, para uso rápido
//   builder() → personalización: baseUrl, defaultHeader, filter (interceptors),
//               codec config, ExchangeStrategies (buffer sizes)
//
// retrieve() vs exchangeToMono():
//   retrieve()        → flujo feliz; lanza error automático para 4xx/5xx
//   exchangeToMono()  → acceso completo a ClientResponse (status + headers + body)
//                       → útil para lógica condicional por código de respuesta
//
// .block():
//   Permite usar WebClient en código síncrono (tests, main, código legado).
//   NUNCA en un hilo del event loop de Reactor — causa deadlock.
//   Justificado en: tests de integración, código de arranque, bridges a código blocking.
//
// Operadores reactivos clave:
//   onErrorResume(e -> Mono.just(fallback)) → fallback al fallar
//   retry(3)                                → reintenta hasta 3 veces
//   timeout(Duration.ofSeconds(5))          → error si supera el tiempo
//   bodyToMono(String.class)                → body completo como Mono<String>
//   bodyToFlux(String.class)                → streaming: cada chunk como Flux item

// ── Simulación de Mono/Flux con CompletableFuture ────────────────────────────

// Mono<T> → 0 o 1 elemento (equivale a CompletableFuture<Optional<T>>)
// Flux<T> → 0 o N elementos (equivale a streaming/iterator async)
// Esta clase simula la semántica de Mono usando CompletableFuture internamente.
class Mono<T> {
    private final CompletableFuture<T> future;

    private Mono(CompletableFuture<T> future) { this.future = future; }

    static <T> Mono<T> just(T value) {
        return new Mono<>(CompletableFuture.completedFuture(value));
    }

    static <T> Mono<T> error(Throwable ex) {
        CompletableFuture<T> f = new CompletableFuture<>();
        f.completeExceptionally(ex);
        return new Mono<>(f);
    }

    static <T> Mono<T> fromCallable(Callable<T> callable) {
        return new Mono<>(CompletableFuture.supplyAsync(() -> {
            try { return callable.call(); }
            catch (Exception e) { throw new CompletionException(e); }
        }));
    }

    // .map() → transforma el valor cuando llega
    <R> Mono<R> map(Function<T, R> mapper) {
        return new Mono<>(future.thenApply(mapper));
    }

    // .flatMap() → transforma a otro Mono (composición)
    <R> Mono<R> flatMap(Function<T, Mono<R>> mapper) {
        return new Mono<>(future.thenCompose(v -> mapper.apply(v).future));
    }

    // .onErrorResume() → fallback cuando hay error
    Mono<T> onErrorResume(Function<Throwable, Mono<T>> fallback) {
        return new Mono<>(future.exceptionally(ex -> {
            try { return fallback.apply(ex).block(); }
            catch (Exception e) { throw new CompletionException(e); }
        }));
    }

    // .retry(n) → reintenta n veces ante error (simplificado: no reintenta async)
    Mono<T> retry(int times) {
        // En Reactor real: cada retry re-suscribe al publisher original
        // Aquí marcamos en el log que se aplicaría retry
        System.out.println("  [Mono] retry(" + times + ") configurado");
        return this;
    }

    // .timeout(ms) → completa con error si tarda más
    Mono<T> timeout(long timeoutMs) {
        return new Mono<>(future.orTimeout(timeoutMs, TimeUnit.MILLISECONDS));
    }

    // .block() — BLOQUEANTE: espera el resultado en el hilo actual
    // Uso justificado: código síncrono, tests, main. NUNCA en event loop.
    T block() {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // .subscribe() — NO bloqueante: registra callbacks y sigue ejecutando
    void subscribe(Consumer<T> onNext, Consumer<Throwable> onError) {
        future.whenComplete((value, ex) -> {
            if (ex != null) onError.accept(ex instanceof CompletionException ? ex.getCause() : ex);
            else onNext.accept(value);
        });
    }
}

// ── Respuesta HTTP simulada ───────────────────────────────────────────────────

record WebResponse(int status, String body) {
    boolean isOk()  { return status >= 200 && status < 300; }
    boolean is4xx() { return status >= 400 && status < 500; }
    boolean is5xx() { return status >= 500; }
}

// ── WebClient simulado ────────────────────────────────────────────────────────

// Simula la API de org.springframework.web.reactive.function.client.WebClient.
// En producción:
//   WebClient.builder()
//     .baseUrl("http://api.example.com")
//     .defaultHeader("X-App-Name", "java-prep")
//     .filter(ExchangeFilterFunction.ofRequestProcessor(...))  // equivale a interceptor
//     .build()
class WebClient {
    private final String baseUrl;
    private final Map<String, String> defaultHeaders;
    private int failCount = 0; // para simular errores transitorios

    private WebClient(String baseUrl, Map<String, String> defaultHeaders) {
        this.baseUrl        = baseUrl;
        this.defaultHeaders = defaultHeaders;
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    // create() → mínimo, sin configuración adicional
    static WebClient create(String baseUrl) {
        System.out.println("  [WebClient.create] baseUrl=" + baseUrl);
        return new WebClient(baseUrl, Map.of());
    }

    // builder() → configuración completa
    static Builder builder() { return new Builder(); }

    static class Builder {
        private String baseUrl = "";
        private final Map<String, String> headers = new LinkedHashMap<>();

        Builder baseUrl(String url) { this.baseUrl = url; return this; }
        Builder defaultHeader(String name, String value) { headers.put(name, value); return this; }

        WebClient build() {
            System.out.println("  [WebClient.builder] baseUrl=" + baseUrl + " headers=" + headers);
            return new WebClient(baseUrl, Map.copyOf(headers));
        }
    }

    // ── Red simulada ─────────────────────────────────────────────────────────

    private static final Map<String, WebResponse> MOCK = Map.of(
        "GET /api/productos",      new WebResponse(200, "[{\"id\":1},{\"id\":2}]"),
        "GET /api/productos/1",    new WebResponse(200, "{\"id\":1,\"nombre\":\"Teclado\"}"),
        "GET /api/productos/99",   new WebResponse(404, "Not Found"),
        "POST /api/pedidos",       new WebResponse(201, "{\"pedidoId\":\"P-100\"}"),
        "GET /api/inestable",      new WebResponse(503, "Service Unavailable")
    );

    private WebResponse networkCall(String method, String path) {
        String key = method + " " + path;
        WebResponse resp = MOCK.getOrDefault(key, new WebResponse(404, "Not Found"));
        System.out.printf("  [HTTP] %s %s%s → %d%n",
            method, baseUrl, path, resp.status());
        return resp;
    }

    // ── retrieve() — flujo feliz, errores automáticos ────────────────────────

    RequestSpec get(String path) { return new RequestSpec("GET", path); }
    RequestSpec post(String path) { return new RequestSpec("POST", path); }

    class RequestSpec {
        private final String method;
        private final String path;
        private String body;

        RequestSpec(String method, String path) {
            this.method = method; this.path = path;
        }

        RequestSpec bodyValue(String body) { this.body = body; return this; }

        // retrieve() + bodyToMono() → lanza error para 4xx/5xx automáticamente
        Mono<String> retrieve() {
            return Mono.fromCallable(() -> {
                WebResponse resp = networkCall(method, path);
                if (resp.is4xx() || resp.is5xx()) {
                    throw new RuntimeException("WebClientResponseException: " + resp.status());
                }
                return resp.body();
            });
        }

        // exchangeToMono() → acceso completo a la respuesta (status + body)
        // Útil cuando necesitas lógica distinta según el código de respuesta.
        Mono<WebResponse> exchangeToMono() {
            return Mono.fromCallable(() -> networkCall(method, path));
        }
    }

    // Simula un endpoint inestable que falla las primeras N veces
    Mono<String> getUnstable(String path) {
        return Mono.fromCallable(() -> {
            failCount++;
            WebResponse resp = networkCall("GET", path);
            if (resp.is5xx()) {
                System.out.println("  [Inestable] Intento #" + failCount + " → fallo");
                throw new RuntimeException("503 Service Unavailable");
            }
            return resp.body();
        });
    }
}

// ── Main ──────────────────────────────────────────────────────────────────────

public class ExpWebClient {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("═".repeat(60));
        System.out.println("  ExpWebClient — Cliente HTTP reactivo");
        System.out.println("═".repeat(60));

        // ── Caso 1: WebClient.create() + retrieve() + block() ───────────────
        System.out.println("\n── Caso 1: retrieve().bodyToMono().block() ───────────────────");
        WebClient simple = WebClient.create("http://api.example.com");
        String productos = simple.get("/api/productos").retrieve().block();
        System.out.println("  Body: " + productos);

        // ── Caso 2: WebClient.builder() con defaultHeader ────────────────────
        System.out.println("\n── Caso 2: builder() con defaultHeader ──────────────────────");
        WebClient configured = WebClient.builder()
            .baseUrl("http://api.example.com")
            .defaultHeader("X-App-Name", "java-prep")
            .defaultHeader("Accept", "application/json")
            .build();

        // .map() encadena transformación sin bloquear
        String nombre = configured.get("/api/productos/1")
            .retrieve()
            .map(body -> "Procesado: " + body)
            .block();
        System.out.println("  " + nombre);

        // ── Caso 3: exchangeToMono() para lógica por status ──────────────────
        System.out.println("\n── Caso 3: exchangeToMono() → acceso al status ───────────────");
        WebResponse resp = configured.get("/api/productos/99")
            .exchangeToMono()
            .block();
        System.out.println("  Status: " + resp.status() + " | 4xx=" + resp.is4xx());

        // ── Caso 4: onErrorResume → fallback ─────────────────────────────────
        System.out.println("\n── Caso 4: onErrorResume → fallback en 4xx/5xx ───────────────");
        String result = configured.get("/api/productos/99")
            .retrieve()
            .onErrorResume(ex -> {
                System.out.println("  [onErrorResume] " + ex.getMessage() + " → usando cache");
                return Mono.just("{\"id\":99,\"nombre\":\"(desde cache)\"}");
            })
            .block();
        System.out.println("  Resultado: " + result);

        // ── Caso 5: POST con body ─────────────────────────────────────────────
        System.out.println("\n── Caso 5: POST + bodyValue ─────────────────────────────────");
        String pedido = configured.post("/api/pedidos")
            .bodyValue("{\"productoId\":1,\"cantidad\":2}")
            .retrieve()
            .block();
        System.out.println("  Creado: " + pedido);

        // ── Caso 6: timeout ───────────────────────────────────────────────────
        System.out.println("\n── Caso 6: timeout() ────────────────────────────────────────");
        try {
            // Simula un endpoint que no existe → 404 antes del timeout
            configured.get("/api/productos/99")
                .retrieve()
                .timeout(5000)
                .block();
        } catch (RuntimeException e) {
            System.out.println("  Error capturado: " + e.getMessage());
        }

        // ── Caso 7: subscribe() — no bloqueante ───────────────────────────────
        System.out.println("\n── Caso 7: subscribe() — ejecución no bloqueante ────────────");
        CountDownLatch latch = new CountDownLatch(1);
        simple.get("/api/productos").retrieve()
            .subscribe(
                body  -> { System.out.println("  [subscribe onNext] " + body); latch.countDown(); },
                error -> { System.out.println("  [subscribe onError] " + error.getMessage()); latch.countDown(); }
            );
        System.out.println("  [main] subscribe() retornó inmediatamente (hilo libre)");
        latch.await(3, TimeUnit.SECONDS);

        // ── Resumen ──────────────────────────────────────────────────────────
        System.out.println("\n── Resumen ───────────────────────────────────────────────────");
        System.out.println("  RestTemplate: 1 thread bloqueado por request (Servlet model)");
        System.out.println("  WebClient:    event loop → un hilo sirve N requests concurrentes");
        System.out.println("  retrieve()    → lanza error automático para 4xx/5xx");
        System.out.println("  exchangeToMono() → acceso a status/headers antes de deserializar");
        System.out.println("  .block()      → síncrono, justificado en main/tests/bridges");
        System.out.println("  .subscribe()  → async, no bloquea el hilo llamante");
        System.out.println("  onErrorResume → fallback (cache, valor por defecto, otro servicio)");
        System.out.println("  retry(n)      → reintento automático ante errores transitorios");
        System.out.println("  timeout(ms)   → falla rápido si el servicio no responde");
        System.out.println("═".repeat(60));
    }
}
