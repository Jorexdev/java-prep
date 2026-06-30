import java.util.*;

// Simula la generación de una spec OpenAPI 3.0 con Java puro.
//
// OpenAPI 2.0 (Swagger) vs 3.0:
//   2.0: "swagger": "2.0" | host + basePath + schemes | body parameter | definitions
//   3.0: "openapi": "3.0.x" | servers[] array | requestBody | components/schemas
//
// springdoc-openapi vs springfox:
//   springfox — abandonado en 2021, no soporta Spring Boot 3 (conflictos con Spring MVC 6)
//   springdoc — activo, soporta Spring Boot 3, compatible con Jakarta EE
//
// Anotaciones Spring clave (aparecen como comentarios):
//   @OpenAPIDefinition  — metadatos globales (info, servers, security)
//   @Tag                — agrupa operaciones en la UI de Swagger
//   @Operation          — documenta un endpoint (summary, description, operationId)
//   @ApiResponse        — documenta un código de respuesta con su esquema
//   @Parameter          — documenta un path/query/header parameter
//   @Schema             — describe un modelo (tipo, formato, ejemplo, validaciones)
//   @SecurityRequirement — indica qué esquema de seguridad requiere la operación

// ── SchemaSpec ────────────────────────────────────────────────────────────────

// @Schema(description = "...", example = "...")
class SchemaSpec {
    private final String name;
    private final Map<String, String> properties = new LinkedHashMap<>();
    private final List<String> required = new ArrayList<>();

    SchemaSpec(String name) { this.name = name; }

    // Simula @Schema(type = "string", format = "email")
    SchemaSpec property(String field, String type, String format, boolean req) {
        properties.put(field, format != null ? type + " (format: " + format + ")" : type);
        if (req) required.add(field);
        return this;
    }

    String toYaml(int indent) {
        String pad = " ".repeat(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(pad).append(name).append(":\n");
        sb.append(pad).append("  type: object\n");
        if (!required.isEmpty()) {
            sb.append(pad).append("  required: ").append(required).append("\n");
        }
        sb.append(pad).append("  properties:\n");
        properties.forEach((k, v) -> {
            sb.append(pad).append("    ").append(k).append(":\n");
            String[] parts = v.split(" \\(format: ");
            sb.append(pad).append("      type: ").append(parts[0]).append("\n");
            if (parts.length > 1) {
                sb.append(pad).append("      format: ")
                  .append(parts[1].replace(")", "")).append("\n");
            }
        });
        return sb.toString();
    }

    String name() { return name; }
}

// ── EndpointSpec ──────────────────────────────────────────────────────────────

// @Operation(summary = "...", operationId = "...", tags = {...})
// @ApiResponse(responseCode = "200", description = "...", content = @Content(schema = @Schema(ref = "...")))
class EndpointSpec {
    private final String method;
    private final String path;
    private final String summary;
    private final String operationId;
    private final String tag;
    private final List<String[]> parameters = new ArrayList<>();  // [name, in, type, desc]
    private final Map<String, String> responses = new LinkedHashMap<>();  // code → description
    private String requestBodySchema;

    EndpointSpec(String method, String path, String summary, String operationId, String tag) {
        this.method = method;
        this.path = path;
        this.summary = summary;
        this.operationId = operationId;
        this.tag = tag;
    }

    // @Parameter(name = "id", in = ParameterIn.PATH, required = true)
    EndpointSpec param(String name, String in, String type, String desc) {
        parameters.add(new String[]{name, in, type, desc});
        return this;
    }

    // @RequestBody @Schema(ref = "ProductoRequest")
    EndpointSpec requestBody(String schemaName) {
        this.requestBodySchema = schemaName;
        return this;
    }

    // @ApiResponse(responseCode = "201", description = "Created")
    EndpointSpec response(String code, String desc) {
        responses.put(code, desc);
        return this;
    }

    String method() { return method; }
    String path()   { return path; }

    String toYaml(int indent) {
        String pad = " ".repeat(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(pad).append(method.toLowerCase()).append(":\n");
        sb.append(pad).append("  tags: [").append(tag).append("]\n");
        sb.append(pad).append("  summary: ").append(summary).append("\n");
        sb.append(pad).append("  operationId: ").append(operationId).append("\n");

        if (!parameters.isEmpty()) {
            sb.append(pad).append("  parameters:\n");
            for (String[] p : parameters) {
                sb.append(pad).append("    - name: ").append(p[0]).append("\n");
                sb.append(pad).append("      in: ").append(p[1]).append("\n");
                sb.append(pad).append("      schema:\n");
                sb.append(pad).append("        type: ").append(p[2]).append("\n");
                sb.append(pad).append("      description: ").append(p[3]).append("\n");
            }
        }

        if (requestBodySchema != null) {
            sb.append(pad).append("  requestBody:\n");
            sb.append(pad).append("    required: true\n");
            sb.append(pad).append("    content:\n");
            sb.append(pad).append("      application/json:\n");
            sb.append(pad).append("        schema:\n");
            sb.append(pad).append("          $ref: '#/components/schemas/").append(requestBodySchema).append("'\n");
        }

        sb.append(pad).append("  responses:\n");
        responses.forEach((code, desc) -> {
            sb.append(pad).append("    '").append(code).append("':\n");
            sb.append(pad).append("      description: ").append(desc).append("\n");
        });
        return sb.toString();
    }
}

// ── ControllerSpec ────────────────────────────────────────────────────────────

// Equivale a la configuración de OpenAPI que agrega springdoc al escanear
// un @RestController anotado con @Tag.
//
// GroupedOpenApi — divide la documentación en grupos (v1/v2, por módulo)
// OpenApiCustomizer — personaliza la spec programáticamente (añadir esquemas globales,
//                     cabeceras de seguridad, etc.)
class ControllerSpec {
    private final String title;
    private final String version;
    private final String serverUrl;
    // securityScheme: nombre → tipo (bearerAuth → http/bearer, apiKey → apiKey/header)
    private final String securityScheme;
    private final Map<String, List<EndpointSpec>> paths = new LinkedHashMap<>();
    private final List<SchemaSpec> schemas = new ArrayList<>();

    ControllerSpec(String title, String version, String serverUrl, String securityScheme) {
        this.title           = title;
        this.version         = version;
        this.serverUrl       = serverUrl;
        this.securityScheme  = securityScheme;
    }

    ControllerSpec addEndpoint(EndpointSpec ep) {
        paths.computeIfAbsent(ep.path(), k -> new ArrayList<>()).add(ep);
        return this;
    }

    ControllerSpec addSchema(SchemaSpec schema) {
        schemas.add(schema);
        return this;
    }

    // Simula la salida de springdoc en formato pseudo-YAML (no es YAML real,
    // sino una aproximación legible que muestra la estructura de la spec).
    void printSpec() {
        System.out.println("openapi: '3.0.3'");
        System.out.println("info:");
        System.out.println("  title: " + title);
        System.out.println("  version: '" + version + "'");
        System.out.println("servers:");
        System.out.println("  - url: " + serverUrl);

        // @SecurityRequirement — referencia el esquema definido en components/securitySchemes
        System.out.println("security:");
        System.out.println("  - " + securityScheme + ": []");

        System.out.println("paths:");
        paths.forEach((path, endpoints) -> {
            System.out.println("  " + path + ":");
            endpoints.forEach(ep -> System.out.print(ep.toYaml(4)));
        });

        System.out.println("components:");
        System.out.println("  securitySchemes:");
        System.out.println("    " + securityScheme + ":");
        System.out.println("      type: http");
        System.out.println("      scheme: bearer");
        System.out.println("      bearerFormat: JWT");
        System.out.println("  schemas:");
        schemas.forEach(s -> System.out.print(s.toYaml(4)));
    }
}

// ── Main ──────────────────────────────────────────────────────────────────────

public class ExpOpenAPI {

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  ExpOpenAPI — Swagger/OpenAPI 3.0 con springdoc");
        System.out.println("═".repeat(60));
        System.out.println();

        // ── Diferencias 2.0 vs 3.0 ──────────────────────────────────────────
        System.out.println("── OpenAPI 2.0 (Swagger) vs 3.0 ─────────────────────────────");
        System.out.println("  2.0  host + basePath + schemes → 3.0  servers[]");
        System.out.println("  2.0  body parameter            → 3.0  requestBody (separado)");
        System.out.println("  2.0  definitions               → 3.0  components/schemas");
        System.out.println("  2.0  \"swagger\": \"2.0\"         → 3.0  \"openapi\": \"3.0.x\"");
        System.out.println("  2.0  produces/consumes global  → 3.0  content type por operación");
        System.out.println();

        // ── springdoc vs springfox ───────────────────────────────────────────
        System.out.println("── springdoc-openapi vs springfox ───────────────────────────");
        System.out.println("  springfox: último release en 2020, incompatible con Spring Boot 3");
        System.out.println("  springdoc: mantenido activamente, soporta Jakarta EE + Boot 3");
        System.out.println("  Migración: reemplazar @ApiOperation/@ApiParam → @Operation/@Parameter");
        System.out.println();

        // ── Construcción de la spec ──────────────────────────────────────────
        SchemaSpec productoRequest = new SchemaSpec("ProductoRequest")
            .property("nombre", "string", null, true)
            .property("precio", "number", "double", true)
            .property("email",  "string", "email",  false);

        SchemaSpec productoResponse = new SchemaSpec("ProductoResponse")
            .property("id",     "integer", "int64", true)
            .property("nombre", "string",  null,    true)
            .property("precio", "number",  "double", true);

        EndpointSpec listar = new EndpointSpec(
            "GET", "/api/productos",
            "Listar todos los productos", "listarProductos", "Productos"
        ).response("200", "Lista de productos").response("401", "No autenticado");

        EndpointSpec crear = new EndpointSpec(
            "POST", "/api/productos",
            "Crear un producto", "crearProducto", "Productos"
        ).requestBody("ProductoRequest")
         .response("201", "Producto creado")
         .response("400", "Datos inválidos")
         .response("401", "No autenticado");

        EndpointSpec obtener = new EndpointSpec(
            "GET", "/api/productos/{id}",
            "Obtener producto por ID", "obtenerProducto", "Productos"
        ).param("id", "path", "integer", "ID del producto")
         .response("200", "Producto encontrado")
         .response("404", "No encontrado");

        EndpointSpec eliminar = new EndpointSpec(
            "DELETE", "/api/productos/{id}",
            "Eliminar producto", "eliminarProducto", "Productos"
        ).param("id", "path", "integer", "ID del producto")
         .response("204", "Eliminado correctamente")
         .response("404", "No encontrado");

        ControllerSpec spec = new ControllerSpec(
            "Java Prep API", "1.0.0",
            "http://localhost:8080", "bearerAuth"
        );
        spec.addEndpoint(listar)
            .addEndpoint(crear)
            .addEndpoint(obtener)
            .addEndpoint(eliminar)
            .addSchema(productoRequest)
            .addSchema(productoResponse);

        System.out.println("── Spec generada (pseudo-YAML) ──────────────────────────────");
        System.out.println();
        spec.printSpec();

        System.out.println();
        System.out.println("── Customización con springdoc ──────────────────────────────");
        System.out.println("  GroupedOpenApi.builder().group(\"v1\").pathsToMatch(\"/api/v1/**\")");
        System.out.println("  → genera /v3/api-docs/v1 y una UI separada por grupo");
        System.out.println();
        System.out.println("  OpenApiCustomizer: bean que recibe OpenAPI y lo modifica");
        System.out.println("  → añadir headers globales, inyectar schemas extra, etc.");
        System.out.println();
        System.out.println("  operationId convention: por defecto = methodName#N");
        System.out.println("  → sobreescribir con @Operation(operationId = \"crearProducto\")");
        System.out.println("    para que los clientes generados tengan nombres legibles");
        System.out.println();
        System.out.println("═".repeat(60));
    }
}
