import java.util.*;

// Simula Spring Boot Actuator: infraestructura de observabilidad lista para producción.
// En producción: spring-boot-starter-actuator expone /actuator/** via HTTP.
// MBeans opcionales via spring.jmx.enabled=true.

public class ExpActuator {

    // ── Status values ──────────────────────────────────────────────────────────

    enum Status { UP, DOWN, OUT_OF_SERVICE, UNKNOWN }

    // ── Health ─────────────────────────────────────────────────────────────────

    // Equivale a org.springframework.boot.actuate.health.Health
    static class Health {
        private final Status status;
        private final Map<String, Object> details;

        private Health(Status status, Map<String, Object> details) {
            this.status  = status;
            this.details = details;
        }

        static Health up()   { return new Health(Status.UP, new LinkedHashMap<>()); }
        static Health down() { return new Health(Status.DOWN, new LinkedHashMap<>()); }

        // Builder-style: Health.down().withDetail("error", "timeout")
        Health withDetail(String key, Object value) {
            details.put(key, value);
            return this;
        }

        // Para el JSON de /actuator/health
        Map<String, Object> toMap(String component) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status", status.name());
            if (!details.isEmpty()) m.put("details", details);
            return m;
        }

        Status status() { return status; }
    }

    // ── HealthIndicator ────────────────────────────────────────────────────────

    // Equivale a org.springframework.boot.actuate.health.HealthIndicator
    // Spring auto-detecta todos los beans que implementen esta interfaz
    interface HealthIndicator {
        String name();
        Health health();
    }

    // Comprueba conexión a BD (simula DataSourceHealthIndicator)
    static class DatabaseHealthIndicator implements HealthIndicator {
        private final boolean connected;

        DatabaseHealthIndicator(boolean connected) { this.connected = connected; }

        @Override public String name() { return "db"; }

        @Override
        public Health health() {
            if (connected) {
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("validationQuery", "isValid()");
            }
            return Health.down()
                    .withDetail("error", "Connection refused: jdbc:postgresql://localhost:5432/app")
                    .withDetail("retries", 3);
        }
    }

    // Comprueba espacio en disco (simula DiskSpaceHealthIndicator)
    static class DiskSpaceHealthIndicator implements HealthIndicator {
        private final long freeBytes;
        private final long thresholdBytes;

        DiskSpaceHealthIndicator(long freeBytes, long thresholdBytes) {
            this.freeBytes      = freeBytes;
            this.thresholdBytes = thresholdBytes;
        }

        @Override public String name() { return "diskSpace"; }

        @Override
        public Health health() {
            if (freeBytes >= thresholdBytes) {
                return Health.up()
                        .withDetail("total", "500GB")
                        .withDetail("free",  freeBytes / (1024L * 1024 * 1024) + "GB")
                        .withDetail("threshold", thresholdBytes / (1024L * 1024 * 1024) + "GB");
            }
            return Health.down()
                    .withDetail("free", freeBytes)
                    .withDetail("threshold", thresholdBytes);
        }
    }

    // Custom indicator de negocio: ¿está el servicio externo de pagos accesible?
    static class PaymentGatewayHealthIndicator implements HealthIndicator {
        private final boolean reachable;

        PaymentGatewayHealthIndicator(boolean reachable) { this.reachable = reachable; }

        @Override public String name() { return "paymentGateway"; }

        @Override
        public Health health() {
            if (reachable) {
                return Health.up().withDetail("url", "https://api.stripe.com/v1/charges");
            }
            return Health.down()
                    .withDetail("url", "https://api.stripe.com/v1/charges")
                    .withDetail("httpStatus", 503)
                    .withDetail("message", "Service Unavailable");
        }
    }

    // ── CompositeHealth (GET /actuator/health) ─────────────────────────────────

    // Spring compone todos los HealthIndicators en un único response.
    // El status global es DOWN si cualquier indicator está DOWN.
    static class CompositeHealthEndpoint {
        private final List<HealthIndicator> indicators;

        CompositeHealthEndpoint(HealthIndicator... indicators) {
            this.indicators = List.of(indicators);
        }

        void print() {
            System.out.println("GET /actuator/health");
            System.out.println("{");

            Map<String, Health> results = new LinkedHashMap<>();
            boolean anyDown = false;
            for (HealthIndicator ind : indicators) {
                Health h = ind.health();
                results.put(ind.name(), h);
                if (h.status() == Status.DOWN) anyDown = true;
            }

            Status global = anyDown ? Status.DOWN : Status.UP;
            System.out.println("  \"status\": \"" + global + "\",");
            System.out.println("  \"components\": {");

            int i = 0;
            for (Map.Entry<String, Health> entry : results.entrySet()) {
                Map<String, Object> m = entry.getValue().toMap(entry.getKey());
                System.out.print("    \"" + entry.getKey() + "\": { \"status\": \"" + m.get("status") + "\"");
                if (m.containsKey("details")) {
                    System.out.print(", \"details\": " + m.get("details"));
                }
                System.out.print(" }");
                System.out.println(++i < results.size() ? "," : "");
            }

            System.out.println("  }");
            System.out.println("}");
        }
    }

    // ── Health Groups (Kubernetes probes) ──────────────────────────────────────

    // management.endpoint.health.group.liveness.include=livenessState
    // management.endpoint.health.group.readiness.include=readinessState,db
    // /actuator/health/liveness → solo indica si la app está viva (no en deadlock)
    // /actuator/health/readiness → indica si la app puede recibir tráfico (BD disponible, etc.)
    static class HealthGroup {
        private final String name;
        private final List<HealthIndicator> indicators;

        HealthGroup(String name, HealthIndicator... indicators) {
            this.name       = name;
            this.indicators = List.of(indicators);
        }

        void print() {
            boolean anyDown = indicators.stream()
                    .map(HealthIndicator::health)
                    .anyMatch(h -> h.status() == Status.DOWN);

            System.out.println("GET /actuator/health/" + name);
            System.out.println("{ \"status\": \"" + (anyDown ? "DOWN" : "UP") + "\" }");
        }
    }

    // ── Custom @Endpoint ──────────────────────────────────────────────────────

    // En Spring: @Endpoint(id="miEndpoint") — id debe ser alfanumérico
    // Se expone en /actuator/miEndpoint
    // @ReadOperation  → HTTP GET
    // @WriteOperation → HTTP POST (body del request como parámetros)
    static class FeatureFlagsEndpoint {

        private final Map<String, Boolean> flags = new LinkedHashMap<>(Map.of(
                "new-checkout",    true,
                "dark-mode",       false,
                "payment-v2",      true
        ));

        // @ReadOperation
        Map<String, Boolean> getFlags() { return Collections.unmodifiableMap(flags); }

        // @WriteOperation
        String setFlag(String flagName, boolean enabled) {
            flags.put(flagName, enabled);
            return "Flag '" + flagName + "' → " + enabled;
        }

        void printGet() {
            System.out.println("GET /actuator/featureFlags");
            System.out.println(flags);
        }

        void printPost(String flagName, boolean enabled) {
            String result = setFlag(flagName, enabled);
            System.out.println("POST /actuator/featureFlags  {flagName:" + flagName + ", enabled:" + enabled + "}");
            System.out.println("→ " + result);
            System.out.println("→ Estado actual: " + flags);
        }
    }

    // ── MetricsRegistry (simula /actuator/metrics) ────────────────────────────

    static class MetricsRegistry {
        // nombre → {tag: valor} → valor de la métrica
        private final Map<String, Double> metrics = new LinkedHashMap<>();

        void record(String name, double value) { metrics.put(name, value); }

        void printMetric(String name) {
            System.out.println("GET /actuator/metrics/" + name);
            if (metrics.containsKey(name)) {
                System.out.println("{");
                System.out.println("  \"name\": \"" + name + "\",");
                System.out.println("  \"measurements\": [{ \"statistic\": \"VALUE\", \"value\": " + metrics.get(name) + " }]");
                System.out.println("}");
            } else {
                System.out.println("{ \"error\": \"No metric named '" + name + "'\" }  // → 404");
            }
        }

        void printAll() {
            System.out.println("GET /actuator/metrics");
            System.out.println("{ \"names\": " + metrics.keySet() + " }");
        }
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═".repeat(65));
        System.out.println("  SPRING BOOT ACTUATOR — Simulación de endpoints");
        System.out.println("═".repeat(65));

        // ── /actuator/health — escenario nominal ──────────────────────────
        System.out.println("\n─ Escenario 1: todos los indicadores UP ─\n");
        CompositeHealthEndpoint healthy = new CompositeHealthEndpoint(
                new DatabaseHealthIndicator(true),
                new DiskSpaceHealthIndicator(100L * 1024 * 1024 * 1024, 10L * 1024 * 1024 * 1024),
                new PaymentGatewayHealthIndicator(true)
        );
        healthy.print();

        // ── /actuator/health — escenario con fallo ────────────────────────
        System.out.println("\n─ Escenario 2: BD caída → status global DOWN ─\n");
        CompositeHealthEndpoint degraded = new CompositeHealthEndpoint(
                new DatabaseHealthIndicator(false),
                new DiskSpaceHealthIndicator(100L * 1024 * 1024 * 1024, 10L * 1024 * 1024 * 1024),
                new PaymentGatewayHealthIndicator(true)
        );
        degraded.print();

        // ── Kubernetes health groups ──────────────────────────────────────
        System.out.println("\n─ Kubernetes Probes (health groups) ─\n");
        // liveness: solo verifica que la JVM no está en deadlock (sin checks de BD)
        new HealthGroup("liveness").print();
        System.out.println();
        // readiness: verifica que puede atender tráfico (BD incluida)
        new HealthGroup("readiness", new DatabaseHealthIndicator(false)).print();

        // ── /actuator/metrics ─────────────────────────────────────────────
        System.out.println("\n─ /actuator/metrics ─\n");
        MetricsRegistry metrics = new MetricsRegistry();
        metrics.record("jvm.memory.used", 256_000_000.0);
        metrics.record("http.server.requests", 1423.0);
        metrics.record("hikaricp.connections.active", 5.0);

        metrics.printAll();
        System.out.println();
        metrics.printMetric("jvm.memory.used");
        System.out.println();
        metrics.printMetric("cpu.usage");  // no registrada → 404

        // ── Custom endpoint ───────────────────────────────────────────────
        System.out.println("\n─ Custom @Endpoint: /actuator/featureFlags ─\n");
        FeatureFlagsEndpoint flagsEndpoint = new FeatureFlagsEndpoint();
        flagsEndpoint.printGet();
        System.out.println();
        flagsEndpoint.printPost("dark-mode", true);

        // ── Notas de seguridad ────────────────────────────────────────────
        System.out.println("\n─ Configuración de producción recomendada ─");
        System.out.println("  management.endpoints.web.exposure.include=health,metrics,info");
        System.out.println("  # NO exponer en producción sin auth: env, beans, heapdump, threaddump");
        System.out.println("  management.endpoint.health.show-details=when-authorized");
        System.out.println("  management.server.port=8081  # puerto separado, no expuesto al exterior");
        System.out.println("  # En Kubernetes: livenessProbe y readinessProbe apuntan a /actuator/health/liveness|readiness");

        System.out.println("\n" + "═".repeat(65));
    }
}
