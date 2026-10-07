import java.util.*;

// Simula las estrategias de versionado de APIs REST.
// Ninguna es universalmente correcta — cada una tiene trade-offs.
// En Spring: @RequestMapping(value="/v1/users", produces="application/vnd.company.v1+json")

public class ExpAPIVersioning {

    // ── Modelo ────────────────────────────────────────────────────────────────

    // v1: nombre como String único
    record UserV1(long id, String nombre, String email) {}

    // v2: nombre separado en firstName/lastName + añade role
    record UserV2(long id, String firstName, String lastName, String email, String role) {}

    // ── Request simulado ─────────────────────────────────────────────────────

    record Request(String uri, String acceptVersionHeader, String acceptMediaType, String queryVersion) {

        // Constructor de conveniencia para URI versioning
        static Request uri(String uri) {
            return new Request(uri, null, null, null);
        }

        // Constructor para header versioning
        static Request header(String baseUri, String version) {
            return new Request(baseUri, version, null, null);
        }

        // Constructor para media type (content negotiation)
        static Request mediaType(String baseUri, String mediaType) {
            return new Request(baseUri, null, mediaType, null);
        }

        // Constructor para query param
        static Request queryParam(String baseUri, String version) {
            return new Request(baseUri, null, null, version);
        }
    }

    // ── Handlers de versión ───────────────────────────────────────────────────

    static String handleV1(long id) {
        UserV1 user = new UserV1(id, "Jorge Martínez", "jorge@example.com");
        return "{ \"id\": " + user.id()
                + ", \"nombre\": \"" + user.nombre()
                + "\", \"email\": \"" + user.email() + "\" }";
    }

    static String handleV2(long id) {
        UserV2 user = new UserV2(id, "Jorge", "Martínez", "jorge@example.com", "ADMIN");
        return "{ \"id\": " + user.id()
                + ", \"firstName\": \"" + user.firstName()
                + "\", \"lastName\": \"" + user.lastName()
                + "\", \"email\": \"" + user.email()
                + "\", \"role\": \"" + user.role() + "\" }";
    }

    // ── Estrategia 1: URI Versioning ──────────────────────────────────────────

    // /api/v1/users/{id}  /api/v2/users/{id}
    //
    // Pros: fácil de cachear (la versión es parte de la URI), visible en logs y proxies,
    //       fácil de probar en browser/Postman sin configuración extra
    // Cons: "rompe" REST purity — la versión no es parte del recurso; duplica rutas;
    //       cambiar versión requiere actualizar todas las URLs en el cliente
    //
    // En Spring:
    //   @GetMapping("/api/v1/users/{id}") public ResponseEntity<UserV1> getUserV1(...)
    //   @GetMapping("/api/v2/users/{id}") public ResponseEntity<UserV2> getUserV2(...)
    static class UriVersionRouter {
        String dispatch(Request req) {
            String uri = req.uri();
            if (uri.startsWith("/api/v1/users/")) {
                long id = Long.parseLong(uri.replace("/api/v1/users/", ""));
                return "200 OK  Content-Type: application/json\n" + handleV1(id);
            }
            if (uri.startsWith("/api/v2/users/")) {
                long id = Long.parseLong(uri.replace("/api/v2/users/", ""));
                return "200 OK  Content-Type: application/json\n" + handleV2(id);
            }
            return "404 Not Found";
        }
    }

    // ── Estrategia 2: Request Header Versioning ──────────────────────────────

    // Accept-version: v1   Accept-version: v2
    //
    // Pros: URIs limpias, no contamina la estructura de recursos
    // Cons: headers custom no son estándar HTTP, difícil de probar en browser,
    //       puede ser ignorado por proxies/CDNs, requiere documentación explícita
    //
    // En Spring (via RequestMappingHandlerMapping custom o condición):
    //   @GetMapping(value="/api/users/{id}", headers="Accept-version=v1")
    static class HeaderVersionRouter {
        String dispatch(Request req) {
            long id = 1L; // URI base /api/users/{id} — el id viene fijo en la demo
            return switch (req.acceptVersionHeader() == null ? "" : req.acceptVersionHeader()) {
                case "v1" -> "200 OK  Accept-version: v1\n" + handleV1(id);
                case "v2" -> "200 OK  Accept-version: v2\n" + handleV2(id);
                default   -> "400 Bad Request  // header Accept-version requerido: v1 | v2";
            };
        }
    }

    // ── Estrategia 3: Media Type (Content Negotiation) ────────────────────────

    // Accept: application/vnd.company.v1+json
    // Accept: application/vnd.company.v2+json
    //
    // Pros: más RESTful — el Content-Type expresa la versión del recurso; mismo URI para todos;
    //       Spring lo soporta nativamente con produces= en @RequestMapping
    // Cons: más verboso, infrecuente fuera de APIs grandes, puede confundir a herramientas simples
    //
    // En Spring:
    //   @GetMapping(value="/api/users/{id}", produces="application/vnd.company.v1+json")
    //   @GetMapping(value="/api/users/{id}", produces="application/vnd.company.v2+json")
    static class MediaTypeVersionRouter {
        private static final String V1_MEDIA = "application/vnd.company.v1+json";
        private static final String V2_MEDIA = "application/vnd.company.v2+json";

        String dispatch(Request req) {
            long id = 1L;
            String accept = req.acceptMediaType() == null ? "" : req.acceptMediaType();
            if (accept.contains("v1+json")) {
                return "200 OK  Content-Type: " + V1_MEDIA + "\n" + handleV1(id);
            }
            if (accept.contains("v2+json")) {
                return "200 OK  Content-Type: " + V2_MEDIA + "\n" + handleV2(id);
            }
            // Sin versión → devolver la última (o la default configurada)
            return "200 OK  Content-Type: " + V2_MEDIA + "  // default: última versión\n" + handleV2(id);
        }
    }

    // ── Estrategia 4: Query Parameter (desaconsejada) ─────────────────────────

    // /api/users/{id}?version=1   /api/users/{id}?version=2
    //
    // Pros: visible, no requiere header custom, retrocompatible con herramientas antiguas
    // Cons: los proxies/CDNs pueden ignorar query params al cachear;
    //       no es semánticamente correcto (la versión no es un filtro del recurso);
    //       desaconsejada por la mayoría de guías de diseño REST
    static class QueryParamVersionRouter {
        String dispatch(Request req) {
            long id = 1L;
            return switch (req.queryVersion() == null ? "" : req.queryVersion()) {
                case "1" -> "200 OK  // WARN: query param versioning no se cachea bien\n" + handleV1(id);
                case "2" -> "200 OK  // WARN: query param versioning no se cachea bien\n" + handleV2(id);
                default  -> "400 Bad Request  // ?version= requerido: 1 | 2";
            };
        }
    }

    // ── Deprecation headers ───────────────────────────────────────────────────

    // RFC 8594 — Sunset Header: fecha en que la versión se retirará.
    // Deprecation: true  — indica que el endpoint está deprecado (sin fecha fija).
    // En Spring: ResponseEntity.ok(body).header("Deprecation", "true")
    //                                   .header("Sunset", "Sat, 01 Jan 2026 00:00:00 GMT")
    static void printDeprecationExample() {
        System.out.println("  HTTP/1.1 200 OK");
        System.out.println("  Content-Type: application/json");
        System.out.println("  Deprecation: true");
        System.out.println("  Sunset: Sat, 01 Jan 2026 00:00:00 GMT");
        System.out.println("  Link: <https://docs.company.com/api/v2>; rel=\"successor-version\"");
        System.out.println("  { \"id\": 1, \"nombre\": \"Jorge Martínez\", ... }");
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═".repeat(65));
        System.out.println("  API VERSIONING — Estrategias REST");
        System.out.println("═".repeat(65));

        // ── Estrategia 1: URI ─────────────────────────────────────────────
        System.out.println("\n─ Estrategia 1: URI Versioning ─");
        System.out.println("  Pros: cacheable, visible en logs, fácil de probar");
        System.out.println("  Cons: versión no es parte del recurso; duplica rutas\n");
        UriVersionRouter uriRouter = new UriVersionRouter();
        System.out.println("  GET /api/v1/users/1");
        System.out.println("  → " + uriRouter.dispatch(Request.uri("/api/v1/users/1")).replace("\n", "\n    "));
        System.out.println();
        System.out.println("  GET /api/v2/users/1");
        System.out.println("  → " + uriRouter.dispatch(Request.uri("/api/v2/users/1")).replace("\n", "\n    "));

        // ── Estrategia 2: Header ──────────────────────────────────────────
        System.out.println("\n─ Estrategia 2: Request Header Versioning ─");
        System.out.println("  Pros: URIs limpias");
        System.out.println("  Cons: difícil de probar en browser; puede ser ignorado por proxies\n");
        HeaderVersionRouter headerRouter = new HeaderVersionRouter();
        System.out.println("  GET /api/users/1  Accept-version: v1");
        System.out.println("  → " + headerRouter.dispatch(Request.header("/api/users/1", "v1")).replace("\n", "\n    "));
        System.out.println();
        System.out.println("  GET /api/users/1  Accept-version: v2");
        System.out.println("  → " + headerRouter.dispatch(Request.header("/api/users/1", "v2")).replace("\n", "\n    "));

        // ── Estrategia 3: Media Type ──────────────────────────────────────
        System.out.println("\n─ Estrategia 3: Media Type / Content Negotiation ─");
        System.out.println("  Pros: más RESTful; Spring lo soporta con produces=");
        System.out.println("  Cons: verboso; raro fuera de APIs grandes\n");
        MediaTypeVersionRouter mediaRouter = new MediaTypeVersionRouter();
        System.out.println("  GET /api/users/1  Accept: application/vnd.company.v1+json");
        System.out.println("  → " + mediaRouter.dispatch(Request.mediaType("/api/users/1", "application/vnd.company.v1+json")).replace("\n", "\n    "));
        System.out.println();
        System.out.println("  GET /api/users/1  Accept: application/vnd.company.v2+json");
        System.out.println("  → " + mediaRouter.dispatch(Request.mediaType("/api/users/1", "application/vnd.company.v2+json")).replace("\n", "\n    "));

        // ── Estrategia 4: Query Param ─────────────────────────────────────
        System.out.println("\n─ Estrategia 4: Query Param (desaconsejada) ─");
        System.out.println("  Cons: proxies pueden ignorar params al cachear; semánticamente incorrecto\n");
        QueryParamVersionRouter qpRouter = new QueryParamVersionRouter();
        System.out.println("  GET /api/users/1?version=2");
        System.out.println("  → " + qpRouter.dispatch(Request.queryParam("/api/users/1", "2")).replace("\n", "\n    "));

        // ── Deprecation headers ───────────────────────────────────────────
        System.out.println("\n─ Retirar versiones: Deprecation + Sunset headers ─\n");
        printDeprecationExample();

        // ── Resumen comparativo ───────────────────────────────────────────
        System.out.println("\n─ Comparativa ─");
        System.out.printf("  %-22s %-12s %-12s %-20s%n", "Estrategia", "Cacheable", "REST purity", "Test en browser");
        System.out.printf("  %-22s %-12s %-12s %-20s%n", "URI (/v1/users)", "SI", "BAJA", "SI");
        System.out.printf("  %-22s %-12s %-12s %-20s%n", "Header custom", "SI*", "MEDIA", "NO");
        System.out.printf("  %-22s %-12s %-12s %-20s%n", "Media type (vnd)", "SI", "ALTA", "NO (fácilmente)");
        System.out.printf("  %-22s %-12s %-12s %-20s%n", "Query param", "NO fiable", "BAJA", "SI");
        System.out.println("  (* depende del proxy/CDN — algunos ignoran headers custom en la cache key)");
        System.out.println("\n" + "═".repeat(65));
    }
}
