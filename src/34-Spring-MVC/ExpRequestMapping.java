import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

// Simula las variantes de @RequestMapping: path variables, request params, headers,
// consumes/produces. El RequestRouter resuelve qué handler coincide, priorizando
// la ruta más específica cuando dos mappings solapan.

// ── Modelo de petición HTTP simulada ─────────────────────────────────────────

class HttpRequest {
    final String method;
    final String path;
    final Map<String, String> params;
    final Map<String, String> headers;
    final String contentType;   // Content-Type del body enviado
    final String accept;        // Accept del cliente

    HttpRequest(String method, String path, Map<String, String> params,
                Map<String, String> headers, String contentType, String accept) {
        this.method      = method;
        this.path        = path;
        this.params      = params != null ? params : Map.of();
        this.headers     = headers != null ? headers : Map.of();
        this.contentType = contentType != null ? contentType : "*/*";
        this.accept      = accept != null ? accept : "*/*";
    }

    // Constructor simplificado para peticiones sin extras
    HttpRequest(String method, String path) {
        this(method, path, null, null, null, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(method + " " + path);
        if (!params.isEmpty())   sb.append(" params=").append(params);
        if (!headers.isEmpty())  sb.append(" headers=").append(headers);
        if (!"*/*".equals(accept)) sb.append(" Accept=").append(accept);
        return sb.toString();
    }
}

// ── Definición de un mapping ──────────────────────────────────────────────────

class MappingDefinition {
    final String method;
    final String pathPattern;      // e.g. "/api/users/{id}"
    final String requiredParam;    // @RequestParam requerido (null = cualquiera)
    final String requiredHeader;   // @RequestHeader requerido (null = cualquiera)
    final String consumes;         // Content-Type que acepta (null = cualquiera)
    final String produces;         // Media type que produce (null = cualquiera)
    final String handlerName;
    final int specificity;         // mayor = más específico; gana en conflicto

    MappingDefinition(String method, String pathPattern, String requiredParam,
                      String requiredHeader, String consumes, String produces,
                      String handlerName, int specificity) {
        this.method         = method;
        this.pathPattern    = pathPattern;
        this.requiredParam  = requiredParam;
        this.requiredHeader = requiredHeader;
        this.consumes       = consumes;
        this.produces       = produces;
        this.handlerName    = handlerName;
        this.specificity    = specificity;
    }
}

// ── Router ────────────────────────────────────────────────────────────────────

// Equivale al DispatcherServlet resolviendo @RequestMapping en runtime
class RequestRouter {

    private final List<MappingDefinition> mappings = new ArrayList<>();

    // @RequestMapping(method=..., path=...) registrado al arrancar el contexto
    void register(MappingDefinition mapping) {
        mappings.add(mapping);
    }

    // Retorna el handler que coincide; en caso de conflicto gana el más específico
    String route(HttpRequest req) {
        List<MappingDefinition> candidates = new ArrayList<>();

        for (MappingDefinition m : mappings) {
            if (!m.method.equals(req.method)) continue;
            if (!pathMatches(m.pathPattern, req.path)) continue;
            if (m.requiredParam  != null && !req.params.containsKey(m.requiredParam))  continue;
            if (m.requiredHeader != null && !req.headers.containsKey(m.requiredHeader)) continue;
            if (m.consumes != null && !req.contentType.contains(m.consumes)) continue;
            if (m.produces != null && !req.accept.contains(m.produces) && !"*/*".equals(req.accept)) continue;
            candidates.add(m);
        }

        if (candidates.isEmpty()) return null;

        // Desempate por especificidad — la ruta más concreta gana
        candidates.sort((a, b) -> b.specificity - a.specificity);
        return candidates.get(0).handlerName;
    }

    // Comprueba si un path concreto encaja con un patrón que puede tener {variable}
    private boolean pathMatches(String pattern, String path) {
        String[] patternParts = pattern.split("/");
        String[] pathParts    = path.split("/");
        if (patternParts.length != pathParts.length) return false;
        for (int i = 0; i < patternParts.length; i++) {
            if (patternParts[i].startsWith("{") && patternParts[i].endsWith("}")) continue; // @PathVariable
            if (!patternParts[i].equals(pathParts[i])) return false;
        }
        return true;
    }

    void printMappings() {
        System.out.println("Mappings registrados:");
        for (MappingDefinition m : mappings) {
            System.out.printf("  %-6s %-35s handler=%-35s spec=%d%n",
                m.method, m.pathPattern, m.handlerName, m.specificity);
        }
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpRequestMapping {
    public static void main(String[] args) {

        RequestRouter router = new RequestRouter();

        // ─── Registro de mappings ─────────────────────────────────────────────

        // @GetMapping("/api/users")
        router.register(new MappingDefinition(
            "GET", "/api/users", null, null, null, null,
            "UserController#listar()", 10));

        // @GetMapping("/api/users/{id}")     ← @PathVariable
        router.register(new MappingDefinition(
            "GET", "/api/users/{id}", null, null, null, null,
            "UserController#obtener(@PathVariable id)", 20));

        // @GetMapping(value="/api/users", params="activo=true")   ← @RequestParam requerido
        // Más específico que el GET /api/users genérico → mayor especificidad
        router.register(new MappingDefinition(
            "GET", "/api/users", "activo", null, null, null,
            "UserController#listarActivos(@RequestParam activo)", 30));

        // @GetMapping(value="/api/users/{id}", headers="X-Admin")  ← @RequestHeader requerido
        router.register(new MappingDefinition(
            "GET", "/api/users/{id}", null, "X-Admin", null, null,
            "UserController#obtenerAdmin(@RequestHeader X-Admin, @PathVariable id)", 40));

        // @PostMapping(value="/api/users", consumes="application/json", produces="application/json")
        router.register(new MappingDefinition(
            "POST", "/api/users", null, null, "application/json", "application/json",
            "UserController#crear(@RequestBody json)", 50));

        System.out.println("=== Simulación @RequestMapping variants ===\n");
        router.printMappings();
        System.out.println();

        // ─── Enrutamiento de peticiones ───────────────────────────────────────

        List<HttpRequest> peticiones = List.of(

            // 1. GET /api/users → handler genérico de lista
            new HttpRequest("GET", "/api/users"),

            // 2. GET /api/users?activo=true → handler específico con @RequestParam
            new HttpRequest("GET", "/api/users", Map.of("activo", "true"), null, null, null),

            // 3. GET /api/users/42 → @PathVariable sin header admin
            new HttpRequest("GET", "/api/users/42"),

            // 4. GET /api/users/42 + header X-Admin → handler admin (más específico que #3)
            new HttpRequest("GET", "/api/users/42", null, Map.of("X-Admin", "secret"), null, null),

            // 5. POST /api/users con JSON → handler crear
            new HttpRequest("POST", "/api/users", null, null, "application/json", "application/json"),

            // 6. DELETE /api/users/42 → ningún mapping registrado → 404
            new HttpRequest("DELETE", "/api/users/42")
        );

        System.out.println("Resultado de enrutamiento:");
        System.out.println();

        for (HttpRequest req : peticiones) {
            String handler = router.route(req);
            if (handler != null) {
                System.out.printf("  %-60s → %s%n", req.toString(), handler);
            } else {
                System.out.printf("  %-60s → 404 No handler found%n", req.toString());
            }
        }

        System.out.println();
        System.out.println("[ CONFLICTO resuelto por especificidad ]");
        System.out.println("  GET /api/users            coincide con: handler genérico (spec=10)");
        System.out.println("  GET /api/users?activo=true coincide con: handler genérico (spec=10) Y handler activos (spec=30)");
        System.out.println("  → Gana spec=30: UserController#listarActivos (ruta + param = más restrictivo)");
        System.out.println();
        System.out.println("  GET /api/users/42         coincide con: @PathVariable (spec=20)");
        System.out.println("  GET /api/users/42 + X-Admin → coincide con @PathVariable (spec=20) Y admin (spec=40)");
        System.out.println("  → Gana spec=40: handler admin (path + header = más restrictivo)");
    }
}
