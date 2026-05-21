import java.util.LinkedHashMap;
import java.util.Map;

// Ejercicio 5 (Medio) — Custom starter: observability
// Activa MetricsCollector, HealthIndicator, TracingFilter si observability.enabled=true
public class Ejercicio5 {

    // Propiedades del starter (simula @ConfigurationProperties(prefix="observability"))
    static class ObservabilityProperties {
        final boolean enabled;
        final String endpoint;
        final double samplingRate;

        ObservabilityProperties(Map<String, String> config) {
            this.enabled = Boolean.parseBoolean(
                config.getOrDefault("observability.enabled", "false"));
            this.endpoint = config.getOrDefault(
                "observability.endpoint", "http://localhost:4317");
            this.samplingRate = Double.parseDouble(
                config.getOrDefault("observability.sampling-rate", "1.0"));
        }

        @Override
        public String toString() {
            return "ObservabilityProperties{enabled=" + enabled
                    + ", endpoint='" + endpoint + "'"
                    + ", samplingRate=" + samplingRate + "}";
        }
    }

    // Beans del starter
    static class MetricsCollector {
        private final String endpoint;
        MetricsCollector(String endpoint) { this.endpoint = endpoint; }
        public void collect() { System.out.println("  [MetricsCollector] Enviando métricas a " + endpoint); }
        @Override public String toString() { return "MetricsCollector{endpoint='" + endpoint + "'}"; }
    }

    static class HealthIndicator {
        public String check() { return "UP"; }
        @Override public String toString() { return "HealthIndicator{status=UP}"; }
    }

    static class TracingFilter {
        private final double samplingRate;
        TracingFilter(double samplingRate) { this.samplingRate = samplingRate; }
        public void trace(String spanName) {
            System.out.println("  [TracingFilter] Trace span='" + spanName
                    + "' (samplingRate=" + samplingRate + ")");
        }
        @Override public String toString() { return "TracingFilter{samplingRate=" + samplingRate + "}"; }
    }

    static class BeanContainer {
        private final Map<String, Object> beans = new LinkedHashMap<>();

        public void register(String name, Object bean) {
            beans.put(name, bean);
        }

        public boolean isEmpty() { return beans.isEmpty(); }

        public void printAll() {
            if (isEmpty()) {
                System.out.println("  (ningún bean registrado)");
                return;
            }
            beans.forEach((name, bean) -> System.out.println("  " + name + " = " + bean));
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String name) { return (T) beans.get(name); }
    }

    // El auto-config del starter
    static class ObservabilityAutoConfig {

        public void configure(Map<String, String> config, BeanContainer container) {
            ObservabilityProperties props = new ObservabilityProperties(config);
            System.out.println("[ObservabilityAutoConfig] Props: " + props);

            if (!props.enabled) {
                System.out.println("[ObservabilityAutoConfig] observability.enabled=false → OMITIDO");
                return;
            }

            System.out.println("[ObservabilityAutoConfig] Registrando beans...");
            container.register("metricsCollector", new MetricsCollector(props.endpoint));
            container.register("healthIndicator", new HealthIndicator());
            container.register("tracingFilter", new TracingFilter(props.samplingRate));
            System.out.println("[ObservabilityAutoConfig] 3 beans registrados");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Custom starter: observability ===");
        System.out.println();

        ObservabilityAutoConfig autoConfig = new ObservabilityAutoConfig();

        // Escenario 1: observability.enabled=true
        System.out.println("--- Escenario 1: observability.enabled=true ---");
        Map<String, String> config1 = new LinkedHashMap<>();
        config1.put("observability.enabled", "true");
        config1.put("observability.endpoint", "http://otel-collector:4317");
        config1.put("observability.sampling-rate", "0.1");

        BeanContainer container1 = new BeanContainer();
        autoConfig.configure(config1, container1);
        System.out.println("Beans activos:");
        container1.printAll();

        System.out.println();
        System.out.println("Demo de uso de los beans:");
        MetricsCollector mc = container1.get("metricsCollector");
        mc.collect();
        HealthIndicator hi = container1.get("healthIndicator");
        System.out.println("  [HealthIndicator] status=" + hi.check());
        TracingFilter tf = container1.get("tracingFilter");
        tf.trace("GET /api/users");

        System.out.println();

        // Escenario 2: observability.enabled=false
        System.out.println("--- Escenario 2: observability.enabled=false ---");
        Map<String, String> config2 = Map.of("observability.enabled", "false");
        BeanContainer container2 = new BeanContainer();
        autoConfig.configure(config2, container2);
        System.out.println("Beans activos:");
        container2.printAll();

        System.out.println();

        // Escenario 3: propiedad ausente → false por defecto
        System.out.println("--- Escenario 3: observability.enabled ausente (default false) ---");
        BeanContainer container3 = new BeanContainer();
        autoConfig.configure(Map.of(), container3);
        System.out.println("Beans activos:");
        container3.printAll();
    }
}
