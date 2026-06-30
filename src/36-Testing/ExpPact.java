import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Consumer-Driven Contract Testing: el consumer define qué espera del provider.
// El consumer genera un archivo .pact JSON que el provider descarga y verifica
// contra su implementación real — sin que ambos estén levantados al mismo tiempo.

// ── Modelos de interacción ────────────────────────────────────────────────────

class PactRequest {
    final String method;
    final String path;
    final Map<String, String> headers;
    final String body;

    PactRequest(String method, String path, Map<String, String> headers, String body) {
        this.method  = method;
        this.path    = path;
        this.headers = headers;
        this.body    = body;
    }
}

class PactResponse {
    final int status;
    final Map<String, String> headers;
    final String body;

    PactResponse(int status, Map<String, String> headers, String body) {
        this.status  = status;
        this.headers = headers;
        this.body    = body;
    }
}

// Un Interaction es la unidad mínima de contrato: "si recibes ESTA request, debes devolver ESTA response"
class Interaction {
    final String description;
    final PactRequest  request;
    final PactResponse response;

    Interaction(String description, PactRequest request, PactResponse response) {
        this.description = description;
        this.request     = request;
        this.response    = response;
    }
}

// ── Pact Broker — almacena y distribuye pacts entre equipos ──────────────────

// En producción: Pactflow (SaaS) o Pact Broker self-hosted (Docker image pactfoundation/pact-broker)
// CI usa "can-i-deploy" antes del deploy para verificar que el par consumer+provider es compatible
class PactBroker {

    // key = "consumer:provider"  → pact JSON almacenado
    private final Map<String, String> store = new LinkedHashMap<>();
    // clave → verificado por el provider
    private final Map<String, Boolean> verified = new LinkedHashMap<>();

    void publish(String consumer, String provider, String pactJson) {
        String key = consumer + ":" + provider;
        store.put(key, pactJson);
        verified.put(key, false);  // recién publicado, aún no verificado
        System.out.println("  [Broker] Pact publicado: " + consumer + " → " + provider);
    }

    String download(String consumer, String provider) {
        String key = consumer + ":" + provider;
        if (!store.containsKey(key)) throw new IllegalStateException("Pact no encontrado: " + key);
        System.out.println("  [Broker] Pact descargado por el provider '" + provider + "'");
        return store.get(key);
    }

    void markVerified(String consumer, String provider) {
        verified.put(consumer + ":" + provider, true);
    }

    // Comando CLI real: pact-broker can-i-deploy --pacticipant X --version Y
    boolean canIDeploy(String consumer, String provider) {
        String key = consumer + ":" + provider;
        return store.containsKey(key) && Boolean.TRUE.equals(verified.get(key));
    }
}

// ── PactConsumer — describe lo que espera del provider ───────────────────────

// Anotación real de la librería:
//   @ExtendWith(PactConsumerTestExt.class)
//   @PactTestFor(providerName = "user-api", pactVersion = PactSpecVersion.V3)
class PactConsumer {

    private final String name;
    private final List<Interaction> interactions = new ArrayList<>();

    PactConsumer(String name) { this.name = name; }

    // En Pact real: @Pact(consumer = "...", provider = "...")
    // El método retorna un RequestResponsePact construido con PactDslWithProvider
    PactConsumer addInteraction(String description, PactRequest req, PactResponse resp) {
        interactions.add(new Interaction(description, req, resp));
        return this;
    }

    // Serializa a .pact JSON — en producción Pact lo genera automáticamente en target/pacts/
    // El JSON incluye metadata (pactSpecification, version) y las interacciones completas
    String generatePact(String providerName) {
        var sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"consumer\": { \"name\": \"").append(name).append("\" },\n");
        sb.append("  \"provider\": { \"name\": \"").append(providerName).append("\" },\n");
        sb.append("  \"interactions\": [\n");
        for (int i = 0; i < interactions.size(); i++) {
            Interaction it = interactions.get(i);
            sb.append("    {\n");
            sb.append("      \"description\": \"").append(it.description).append("\",\n");
            sb.append("      \"request\": { \"method\": \"").append(it.request.method)
              .append("\", \"path\": \"").append(it.request.path).append("\" },\n");
            sb.append("      \"response\": { \"status\": ").append(it.response.status).append(" }\n");
            sb.append("    }").append(i < interactions.size() - 1 ? "," : "").append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"metadata\": { \"pactSpecification\": { \"version\": \"3.0\" } }\n");
        sb.append("}");
        return sb.toString();
    }

    List<Interaction> getInteractions() { return interactions; }
    String getName() { return name; }
}

// ── PactProvider — verifica el pact contra la implementación real ─────────────

// Anotación real:
//   @Provider("user-api")
//   @PactBroker(url = "http://pact-broker:9292")
//   @ExtendWith(PactVerificationInvocationContextProvider.class)
class PactProvider {

    private final String name;

    PactProvider(String name) { this.name = name; }

    // Simula la implementación real del provider (en producción es el controlador Spring)
    // Pact replay: arranca la app, envía cada request del pact y compara la response real
    PactResponse handle(PactRequest req) {
        if ("GET".equals(req.method) && "/api/users/1".equals(req.path)) {
            return new PactResponse(200,
                Map.of("Content-Type", "application/json"),
                "{\"id\": 1, \"name\": \"Ana García\", \"email\": \"ana@example.com\"}");
        }
        if ("POST".equals(req.method) && "/api/users".equals(req.path)) {
            return new PactResponse(201,
                Map.of("Content-Type", "application/json"),
                "{\"id\": 2, \"name\": \"Carlos\", \"email\": \"carlos@example.com\"}");
        }
        if ("DELETE".equals(req.method) && req.path.startsWith("/api/users/")) {
            // Simula un breaking change: endpoint eliminado → el pact fallará
            return new PactResponse(404, Map.of(), "{}");
        }
        return new PactResponse(404, Map.of(), "{}");
    }

    // Descarga el pact y verifica que cada interacción funciona contra la implementación real
    // En Pact real: @TestTemplate con @ExtendWith(PactVerificationInvocationContextProvider.class)
    boolean verify(List<Interaction> interactions, PactBroker broker, String consumerName) {
        System.out.println("  [Provider] Verificando contrato @Provider(\"" + name + "\")...\n");
        int passed = 0, failed = 0;

        for (Interaction it : interactions) {
            PactResponse actual = handle(it.request);
            if (actual.status == it.response.status) {
                System.out.printf("    PASS  \"%s\"%n", it.description);
                System.out.printf("          %s %s → %d ✓%n",
                    it.request.method, it.request.path, actual.status);
                passed++;
            } else {
                System.out.printf("    FAIL  \"%s\"%n", it.description);
                System.out.printf("          %s %s → esperado %d, real %d%n",
                    it.request.method, it.request.path, it.response.status, actual.status);
                failed++;
            }
        }

        boolean allPassed = failed == 0;
        System.out.printf("%n  [Provider] %d PASS | %d FAIL%n", passed, failed);

        if (allPassed) broker.markVerified(consumerName, name);
        return allPassed;
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpPact {
    public static void main(String[] args) {

        System.out.println("=== Consumer-Driven Contract Testing con Pact ===\n");

        PactBroker broker = new PactBroker();

        // ─── Fase 1: Consumer escribe el contrato en sus tests ────────────────
        System.out.println("[ Fase 1 — Consumer define el contrato ]");
        System.out.println("  El consumer describe qué requests enviará y qué responses espera.");
        System.out.println("  No necesita al provider levantado — Pact levanta un mock server local.\n");

        PactConsumer consumer = new PactConsumer("user-service-consumer");

        consumer
            .addInteraction(
                "obtener usuario existente",
                new PactRequest("GET", "/api/users/1", Map.of("Accept", "application/json"), null),
                new PactResponse(200, Map.of("Content-Type", "application/json"),
                    "{\"id\": 1, \"name\": \"Ana García\"}"))
            .addInteraction(
                "crear nuevo usuario",
                new PactRequest("POST", "/api/users",
                    Map.of("Content-Type", "application/json"),
                    "{\"name\": \"Carlos\", \"email\": \"carlos@example.com\"}"),
                new PactResponse(201, Map.of(), "{}"));

        consumer.getInteractions().forEach(it ->
            System.out.printf("  Interacción: %-40s → status %d%n",
                "\"" + it.description + "\"", it.response.status));

        // ─── Fase 2: Consumer genera el .pact JSON y lo publica en el broker ──
        System.out.println("\n[ Fase 2 — Consumer genera y publica el pact ]");
        System.out.println("  En producción: mvn test genera target/pacts/consumer-provider.json");
        System.out.println("  y el plugin maven-pact-publisher lo sube al broker.\n");

        String pactJson = consumer.generatePact("user-api");

        // Mostramos solo las primeras líneas del JSON para no saturar la salida
        String[] lines = pactJson.split("\n");
        for (int i = 0; i < Math.min(lines.length, 10); i++) System.out.println("  " + lines[i]);
        if (lines.length > 10) System.out.println("  ...(" + (lines.length - 10) + " líneas más)\n");

        broker.publish("user-service-consumer", "user-api", pactJson);

        // ─── Fase 3: Provider descarga el pact y verifica ─────────────────────
        System.out.println("\n[ Fase 3 — Provider verifica el contrato ]");
        System.out.println("  El provider no necesita al consumer arriba.");
        System.out.println("  Descarga el pact, hace replay de las requests y compara responses.\n");

        broker.download("user-service-consumer", "user-api");

        PactProvider provider = new PactProvider("user-api");
        boolean verified = provider.verify(
            consumer.getInteractions(), broker, "user-service-consumer");

        // ─── Fase 4: CI — can-i-deploy antes de desplegar ─────────────────────
        System.out.println("\n[ Fase 4 — CI: can-i-deploy ]");
        System.out.println("  El broker sabe qué pacts están verificados y bloquea deploys incompatibles.\n");

        boolean safe = broker.canIDeploy("user-service-consumer", "user-api");
        System.out.println("  $ pact-broker can-i-deploy \\");
        System.out.println("      --pacticipant user-service-consumer --version 1.0.0 \\");
        System.out.println("      --to-environment production");
        System.out.println("  → " + (safe ? "OK — seguro desplegar" : "BLOQUEADO — contrato no verificado"));

        // ─── Diferencias clave ────────────────────────────────────────────────
        System.out.println("\n[ Diferencias clave ]");
        System.out.println("  Pact vs Integration Test : no necesitas levantar el provider para testear");
        System.out.println("    el consumer, ni al consumer para verificar el provider.");
        System.out.println("  Pact vs Schema Validation: Avro/JSON Schema valida la estructura del");
        System.out.println("    mensaje (campos, tipos). Pact valida el comportamiento completo");
        System.out.println("    (request → response) — incluyendo status codes y body semantics.");
        System.out.println("  Cuándo usarlo: microservicios con equipos distintos donde un cambio de");
        System.out.println("    API del provider puede romper al consumer sin que nadie lo sepa hasta");
        System.out.println("    que falla en producción.");
    }
}
