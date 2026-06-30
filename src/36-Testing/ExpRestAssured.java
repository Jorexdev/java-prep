import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

// REST Assured: DSL fluido para tests de APIs HTTP.
// Sintaxis: given() → when() → then()
//   given() → request spec (headers, body, path/query params, filtros)
//   when()  → método HTTP + path
//   then()  → assertions sobre status code y body (JSON path)
//
// Diferencia con MockMvc: REST Assured tiene sintaxis más expresiva y soporta
// tanto servidor completo como MockMvc en memoria (vía RestAssuredMockMvc).

// ── Modelos de request / response ────────────────────────────────────────────

class ApiRequest {
    final String method;
    final String path;
    final Map<String, String> headers;
    final Map<String, String> pathParams;
    final Map<String, String> queryParams;
    final String body;
    final List<String> filterLog;  // acumulado por logging filters

    ApiRequest(String method, String path,
               Map<String, String> headers,
               Map<String, String> pathParams,
               Map<String, String> queryParams,
               String body,
               List<String> filterLog) {
        this.method      = method;
        this.path        = path;
        this.headers     = headers;
        this.pathParams  = pathParams;
        this.queryParams = queryParams;
        this.body        = body;
        this.filterLog   = filterLog;
    }

    // Sustituye {param} en el path por el valor de pathParams
    String resolvedPath() {
        String p = path;
        for (var entry : pathParams.entrySet()) {
            p = p.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        if (!queryParams.isEmpty()) {
            var sb = new StringBuilder(p).append("?");
            queryParams.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            p = sb.substring(0, sb.length() - 1);
        }
        return p;
    }
}

class ApiResponse {
    final int status;
    final Map<String, Object> jsonBody;  // JSON path simulation: "users[0].name" → value

    ApiResponse(int status, Map<String, Object> jsonBody) {
        this.status   = status;
        this.jsonBody = jsonBody;
    }

    // Navega un path simple estilo GPath: "users[0].name", "id", "users.size()"
    Object extract(String jsonPath) {
        if (jsonPath.endsWith(".size()")) {
            String key = jsonPath.substring(0, jsonPath.length() - ".size()".length());
            Object val = jsonBody.get(key);
            if (val instanceof List<?> list) return list.size();
            return 0;
        }
        // path con índice: "users[0].name"
        if (jsonPath.contains("[")) {
            int bracketStart = jsonPath.indexOf('[');
            int bracketEnd   = jsonPath.indexOf(']');
            String listKey   = jsonPath.substring(0, bracketStart);
            int idx          = Integer.parseInt(jsonPath.substring(bracketStart + 1, bracketEnd));
            String rest      = jsonPath.substring(bracketEnd + 2);  // skip ].
            Object val       = jsonBody.get(listKey);
            if (val instanceof List<?> list && idx < list.size()) {
                Object item = list.get(idx);
                if (item instanceof Map<?, ?> map) return map.get(rest);
            }
            return null;
        }
        return jsonBody.get(jsonPath);
    }
}

// ── Filters — se ejecutan antes/después de la request ────────────────────────

// En REST Assured real:
//   given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
//   o globalmente: RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
interface RaFilter {
    void onRequest(ApiRequest req);
    void onResponse(ApiRequest req, ApiResponse resp);
}

class RequestLoggingFilter implements RaFilter {
    @Override
    public void onRequest(ApiRequest req) {
        req.filterLog.add("→ REQUEST:  " + req.method + " " + req.resolvedPath());
        if (req.body != null) req.filterLog.add("  Body:     " + req.body);
        req.headers.forEach((k, v) -> req.filterLog.add("  Header:   " + k + ": " + v));
    }
    @Override public void onResponse(ApiRequest req, ApiResponse resp) {}
}

class ResponseLoggingFilter implements RaFilter {
    @Override public void onRequest(ApiRequest req) {}
    @Override
    public void onResponse(ApiRequest req, ApiResponse resp) {
        req.filterLog.add("← RESPONSE: " + resp.status);
        resp.jsonBody.forEach((k, v) -> req.filterLog.add("  Body:     " + k + ": " + v));
    }
}

// ── DSL — given() ─────────────────────────────────────────────────────────────

// RequestSpec acumula la configuración de la request antes de dispararla
class RequestSpec {

    private final Map<String, String> headers     = new LinkedHashMap<>();
    private final Map<String, String> pathParams  = new LinkedHashMap<>();
    private final Map<String, String> queryParams = new LinkedHashMap<>();
    private final List<RaFilter>      filters     = new ArrayList<>();
    private String body;

    // Punto de entrada — equivale a RestAssured.given()
    static RequestSpec given() { return new RequestSpec(); }

    RequestSpec header(String name, String value)        { headers.put(name, value); return this; }
    RequestSpec body(String json)                        { this.body = json; return this; }
    RequestSpec pathParam(String name, Object value)     { pathParams.put(name, String.valueOf(value)); return this; }
    RequestSpec queryParam(String name, Object value)    { queryParams.put(name, String.valueOf(value)); return this; }
    RequestSpec filter(RaFilter f)                       { filters.add(f); return this; }

    // Atajo: log().all() activa ambos filtros de logging
    RequestSpec logAll() { return filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter()); }

    // Transición al bloque when()
    WhenSpec when() { return new WhenSpec(this); }

    // Getters para WhenSpec
    Map<String, String> getHeaders()     { return headers; }
    Map<String, String> getPathParams()  { return pathParams; }
    Map<String, String> getQueryParams() { return queryParams; }
    List<RaFilter>      getFilters()     { return filters; }
    String              getBody()        { return body; }
}

// ── DSL — when() ─────────────────────────────────────────────────────────────

class WhenSpec {

    private final RequestSpec spec;
    private final MockServer  server = MockServer.instance();

    WhenSpec(RequestSpec spec) { this.spec = spec; }

    ThenSpec get(String path)    { return dispatch("GET",    path); }
    ThenSpec post(String path)   { return dispatch("POST",   path); }
    ThenSpec put(String path)    { return dispatch("PUT",    path); }
    ThenSpec delete(String path) { return dispatch("DELETE", path); }

    private ThenSpec dispatch(String method, String path) {
        var filterLog = new ArrayList<String>();
        var req = new ApiRequest(method, path,
            spec.getHeaders(), spec.getPathParams(), spec.getQueryParams(),
            spec.getBody(), filterLog);

        // Ejecutar request filters
        for (RaFilter f : spec.getFilters()) f.onRequest(req);

        ApiResponse resp = server.handle(req);

        // Ejecutar response filters
        for (RaFilter f : spec.getFilters()) f.onResponse(req, resp);

        return new ThenSpec(req, resp);
    }
}

// ── DSL — then() ─────────────────────────────────────────────────────────────

class ThenSpec {

    private final ApiRequest  req;
    private final ApiResponse resp;

    ThenSpec(ApiRequest req, ApiResponse resp) {
        this.req  = req;
        this.resp = resp;
    }

    // Imprime los logs de filtros si los hay
    ThenSpec printLogs() {
        req.filterLog.forEach(line -> System.out.println("  " + line));
        return this;
    }

    // then().statusCode(200)
    ThenSpec statusCode(int expected) {
        boolean ok = resp.status == expected;
        System.out.printf("  %s  statusCode(%d) — actual: %d%n",
            ok ? "PASS" : "FAIL", expected, resp.status);
        return this;
    }

    // then().body("users.size()", equalTo(3))
    // then().body("id", notNullValue())
    // then().body("users[0].name", equalTo("Alice"))
    ThenSpec body(String jsonPath, Predicate<Object> matcher, String matcherDesc) {
        Object actual = resp.extract(jsonPath);
        boolean ok = matcher.test(actual);
        System.out.printf("  %s  body(\"%s\", %s) — actual: %s%n",
            ok ? "PASS" : "FAIL", jsonPath, matcherDesc, actual);
        return this;
    }

    // Atajo: extrae un valor del body para usarlo en el test
    Object extract(String jsonPath) { return resp.extract(jsonPath); }

    // Transición idiomática: .get(...).then().statusCode(...)
    // En REST Assured real, then() convierte Response → ValidatableResponse
    ThenSpec then() { return this; }

    // Acceso al response completo para encadenar más assertions
    ApiResponse getResponse() { return resp; }
}

// Predicados tipo Hamcrest para las assertions del body
class Matchers {
    static Predicate<Object> equalTo(Object expected) {
        return actual -> expected.equals(actual);
    }
    static Predicate<Object> notNullValue() {
        return actual -> actual != null;
    }
    static Predicate<Object> greaterThan(int n) {
        return actual -> actual instanceof Number num && num.intValue() > n;
    }
}

// ── Mock Server — simula la API bajo test ────────────────────────────────────

// En tests con servidor completo: RestAssured.baseURI = "http://localhost:8080"
// En tests sin servidor:          RestAssuredMockMvc.standaloneSetup(new UserController())
class MockServer {

    private static final MockServer INSTANCE = new MockServer();
    static MockServer instance() { return INSTANCE; }

    @SuppressWarnings("unchecked")
    ApiResponse handle(ApiRequest req) {
        String path = req.resolvedPath().split("\\?")[0];  // sin query string para el routing

        // GET /api/users/{id}
        if ("GET".equals(req.method) && path.matches("/api/users/\\d+")) {
            String id = path.substring("/api/users/".length());
            return new ApiResponse(200, Map.of(
                "id",    Integer.parseInt(id),
                "name",  "Ana García",
                "email", "ana@example.com"));
        }
        // GET /api/users  (con queryParam page)
        if ("GET".equals(req.method) && "/api/users".equals(path)) {
            return new ApiResponse(200, Map.of(
                "users", List.of(
                    Map.of("id", 1, "name", "Alice"),
                    Map.of("id", 2, "name", "Bob"),
                    Map.of("id", 3, "name", "Carlos")
                ),
                "total", 3));
        }
        // POST /api/users
        if ("POST".equals(req.method) && "/api/users".equals(path)) {
            return new ApiResponse(201, Map.of(
                "id",    42,
                "name",  "Nuevo Usuario",
                "email", "nuevo@example.com"));
        }
        // DELETE /api/users/{id}
        if ("DELETE".equals(req.method) && path.matches("/api/users/\\d+")) {
            return new ApiResponse(204, Map.of());
        }
        return new ApiResponse(404, Map.of("error", "Not Found"));
    }
}

// ── RestAssuredMockMvc — Spring MVC sin servidor levantado ────────────────────

// En producción:
//   RestAssuredMockMvc.standaloneSetup(new UserController());
//   RestAssuredMockMvc.given().when().get("/api/users").then().statusCode(200);
//
// Diferencia con MockMvc puro:
//   mockMvc.perform(get("/api/users")).andExpect(status().isOk())  ← verboso
//   vs
//   given().when().get("/api/users").then().statusCode(200)        ← fluido
//
// RestAssuredMockMvc es idéntico a REST Assured pero en vez de HTTP real
// usa el dispatcher de Spring MVC directamente (sin puerto, sin red).
class RestAssuredMockMvc {

    private final MockServer controller;

    // standaloneSetup: solo el controlador que quieres testear, sin contexto Spring completo
    static RestAssuredMockMvc standaloneSetup(MockServer controller) {
        return new RestAssuredMockMvc(controller);
    }

    private RestAssuredMockMvc(MockServer controller) { this.controller = controller; }

    ThenSpec get(String path) {
        var req = new ApiRequest("GET", path,
            Map.of(), Map.of(), Map.of(), null, new ArrayList<>());
        return new ThenSpec(req, controller.handle(req));
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpRestAssured {
    public static void main(String[] args) {

        System.out.println("=== API Testing con REST Assured (simulado) ===\n");

        // ─── Test 1: GET con path param ───────────────────────────────────────
        System.out.println("[ Test 1 — GET /api/users/{id} con pathParam ]");
        System.out.println();

        RequestSpec.given()
            .header("Accept", "application/json")
            .pathParam("id", 1)                         // sustituye {id} en el path
            .when()
            .get("/api/users/{id}")
            .then()
            .statusCode(200)
            .body("id",    Matchers.notNullValue(),     "notNullValue()")
            .body("name",  Matchers.notNullValue(),     "notNullValue()")
            .body("email", Matchers.equalTo("ana@example.com"), "equalTo(\"ana@example.com\")");

        // ─── Test 2: GET con query param + JSON path sobre lista ──────────────
        System.out.println("\n[ Test 2 — GET /api/users?page=0 con queryParam + JSON path ]");
        System.out.println();

        RequestSpec.given()
            .header("Accept", "application/json")
            .queryParam("page", 0)                      // añade ?page=0 a la URL
            .when()
            .get("/api/users")
            .then()
            .statusCode(200)
            .body("users.size()",  Matchers.equalTo(3),       "equalTo(3)")
            .body("users[0].name", Matchers.equalTo("Alice"),  "equalTo(\"Alice\")")
            .body("total",         Matchers.greaterThan(0),    "greaterThan(0)");

        // ─── Test 3: POST con body + logging filters ──────────────────────────
        System.out.println("\n[ Test 3 — POST /api/users con body + RequestLoggingFilter ]");
        System.out.println();

        RequestSpec.given()
            .header("Content-Type", "application/json")
            .body("{\"name\": \"Nuevo Usuario\", \"email\": \"nuevo@example.com\"}")
            .filter(new RequestLoggingFilter())         // imprime la request antes de enviarla
            .filter(new ResponseLoggingFilter())        // imprime la response al recibirla
            .when()
            .post("/api/users")
            .then()
            .printLogs()                                // muestra lo capturado por los filtros
            .statusCode(201)
            .body("id", Matchers.notNullValue(), "notNullValue()");

        // ─── Test 4: DELETE — assert solo status sin body ────────────────────
        System.out.println("\n[ Test 4 — DELETE /api/users/{id} — sin body en response ]");
        System.out.println();

        RequestSpec.given()
            .pathParam("id", 5)
            .when()
            .delete("/api/users/{id}")
            .then()
            .statusCode(204);

        // ─── Test 5: RestAssuredMockMvc — sin servidor levantado ──────────────
        System.out.println("\n[ Test 5 — RestAssuredMockMvc: Spring MVC sin servidor ]");
        System.out.println("  standaloneSetup() carga solo el controlador, sin contexto Spring.");
        System.out.println("  Equivale a MockMvcBuilders.standaloneSetup(controller).build()");
        System.out.println("  pero con la sintaxis fluida de REST Assured.\n");

        RestAssuredMockMvc mockMvc =
            RestAssuredMockMvc.standaloneSetup(MockServer.instance());

        mockMvc.get("/api/users")
               .statusCode(200)
               .body("total", Matchers.greaterThan(0), "greaterThan(0)");

        // ─── Notas ────────────────────────────────────────────────────────────
        System.out.println("\n[ Resumen de conceptos ]");
        System.out.println("  given()  → acumula headers, body, params, filtros");
        System.out.println("  when()   → dispara la request (GET/POST/PUT/DELETE)");
        System.out.println("  then()   → assertions: statusCode(), body() con JSON path");
        System.out.println("  pathParam vs queryParam:");
        System.out.println("    pathParam(\"id\", 1) → /api/users/1   (segmento de path)");
        System.out.println("    queryParam(\"page\", 0) → /api/users?page=0  (query string)");
        System.out.println("  JSON path: \"users.size()\" cuenta elementos de lista,");
        System.out.println("    \"users[0].name\" accede al campo de un objeto en posición 0.");
        System.out.println("  Filtros: RequestLoggingFilter + ResponseLoggingFilter para");
        System.out.println("    depurar tests flaky sin modificar el código del test.");
        System.out.println("  REST Assured vs MockMvc:");
        System.out.println("    MockMvc:      .andExpect(status().isOk()).andExpect(jsonPath(\"$.id\").exists())");
        System.out.println("    REST Assured: .then().statusCode(200).body(\"id\", notNullValue())");
    }
}
