import java.util.LinkedHashMap;
import java.util.Map;

// Ejercicio 3 (Difícil) — Typed starter properties
// ObservabilityProperties compartida entre beans del starter
public class Ejercicio3 {

    // Simula @ConfigurationProperties(prefix="observability")
    // con validación de tipos y valores por defecto
    static class ObservabilityProperties {
        private final boolean enabled;
        private final double samplingRate;
        private final String endpoint;
        private final int batchSize;
        private final long flushIntervalMs;

        ObservabilityProperties(Map<String, String> config) {
            this.enabled = Boolean.parseBoolean(
                config.getOrDefault("observability.enabled", "false"));
            this.samplingRate = Double.parseDouble(
                config.getOrDefault("observability.sampling-rate", "1.0"));
            this.endpoint = config.getOrDefault(
                "observability.endpoint", "http://localhost:4317");
            this.batchSize = Integer.parseInt(
                config.getOrDefault("observability.batch-size", "100"));
            this.flushIntervalMs = Long.parseLong(
                config.getOrDefault("observability.flush-interval-ms", "5000"));
        }

        public boolean isEnabled() { return enabled; }
        public double getSamplingRate() { return samplingRate; }
        public String getEndpoint() { return endpoint; }
        public int getBatchSize() { return batchSize; }
        public long getFlushIntervalMs() { return flushIntervalMs; }

        @Override
        public String toString() {
            return "ObservabilityProperties{" +
                    "enabled=" + enabled +
                    ", samplingRate=" + samplingRate +
                    ", endpoint='" + endpoint + "'" +
                    ", batchSize=" + batchSize +
                    ", flushIntervalMs=" + flushIntervalMs + "}";
        }
    }

    // Bean que lee la instancia compartida de propiedades
    static class MetricsCollector {
        private final ObservabilityProperties props;

        MetricsCollector(ObservabilityProperties props) { this.props = props; }

        public void collect(String metric, double value) {
            System.out.printf("  [MetricsCollector] %s=%.2f → %s (batch=%d)%n",
                metric, value, props.getEndpoint(), props.getBatchSize());
        }

        @Override
        public String toString() {
            return "MetricsCollector{endpoint='" + props.getEndpoint()
                    + "', batchSize=" + props.getBatchSize() + "}";
        }
    }

    static class TracingFilter {
        private final ObservabilityProperties props;

        TracingFilter(ObservabilityProperties props) { this.props = props; }

        public boolean shouldSample() {
            return Math.random() < props.getSamplingRate();
        }

        public void trace(String spanName) {
            System.out.printf("  [TracingFilter] span='%s' samplingRate=%.0f%% → %s%n",
                spanName, props.getSamplingRate() * 100, props.getEndpoint());
        }

        @Override
        public String toString() {
            return "TracingFilter{samplingRate=" + (props.getSamplingRate() * 100) + "%" + "}";
        }
    }

    static class HealthIndicator {
        private final ObservabilityProperties props;

        HealthIndicator(ObservabilityProperties props) { this.props = props; }

        public void report() {
            System.out.println("  [HealthIndicator] flushInterval=" + props.getFlushIntervalMs()
                    + "ms → " + props.getEndpoint());
        }

        @Override
        public String toString() {
            return "HealthIndicator{flushInterval=" + props.getFlushIntervalMs() + "ms}";
        }
    }

    static class ObservabilityContext {
        MetricsCollector metricsCollector;
        TracingFilter tracingFilter;
        HealthIndicator healthIndicator;
    }

    // Auto-config del starter
    static class ObservabilityAutoConfig {
        public ObservabilityContext configure(Map<String, String> config) {
            ObservabilityProperties props = new ObservabilityProperties(config);
            System.out.println("Properties: " + props);

            if (!props.isEnabled()) {
                System.out.println("observability.enabled=false → starter desactivado");
                return null;
            }

            // Todos los beans comparten la MISMA instancia de properties
            ObservabilityContext ctx = new ObservabilityContext();
            ctx.metricsCollector = new MetricsCollector(props);
            ctx.tracingFilter = new TracingFilter(props);
            ctx.healthIndicator = new HealthIndicator(props);

            System.out.println("Beans creados:");
            System.out.println("  " + ctx.metricsCollector);
            System.out.println("  " + ctx.tracingFilter);
            System.out.println("  " + ctx.healthIndicator);

            return ctx;
        }
    }

    static void demo(String label, Map<String, String> config) {
        System.out.println("=== " + label + " ===");
        ObservabilityAutoConfig autoConfig = new ObservabilityAutoConfig();
        ObservabilityContext ctx = autoConfig.configure(config);
        if (ctx != null) {
            System.out.println("Usando los beans:");
            ctx.metricsCollector.collect("http.requests", 42.0);
            ctx.tracingFilter.trace("POST /api/orders");
            ctx.healthIndicator.report();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Typed starter properties ===");
        System.out.println();

        // Configuración 1: producción
        demo("Producción: sampling 10%, batch grande", Map.of(
            "observability.enabled", "true",
            "observability.endpoint", "https://otel.prod.example.com:4317",
            "observability.sampling-rate", "0.1",
            "observability.batch-size", "1000",
            "observability.flush-interval-ms", "10000"
        ));

        // Configuración 2: desarrollo
        demo("Desarrollo: sampling 100%, batch pequeño", Map.of(
            "observability.enabled", "true",
            "observability.endpoint", "http://localhost:4317",
            "observability.sampling-rate", "1.0",
            "observability.batch-size", "10",
            "observability.flush-interval-ms", "1000"
        ));

        // Configuración 3: desactivado
        demo("Desactivado", Map.of("observability.enabled", "false"));
    }
}
