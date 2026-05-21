import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Ejercicio 1 (Fácil) — @ConditionalOnProperty
// Solo registra DataSource si "db.enabled" = "true"
public class Ejercicio1 {

    // Bean simulado
    static class DataSource {
        private final String url;

        DataSource(String url) { this.url = url; }

        @Override
        public String toString() { return "DataSource{url='" + url + "'}"; }
    }

    static class ConditionalContainer {
        private final Map<String, Object> beans = new LinkedHashMap<>();
        private final Map<String, String> config;

        ConditionalContainer(Map<String, String> config) {
            this.config = config;
        }

        // Simula @ConditionalOnProperty(name="db.enabled", havingValue="true")
        public void registerDataSourceIfEnabled() {
            String enabled = config.getOrDefault("db.enabled", "false");
            System.out.println("[Condition] db.enabled = '" + enabled + "'");

            if ("true".equalsIgnoreCase(enabled)) {
                String url = config.getOrDefault("db.url", "jdbc:h2:mem:defaultdb");
                DataSource ds = new DataSource(url);
                beans.put("dataSource", ds);
                System.out.println("[AutoConfig] DataSource registrado: " + ds);
            } else {
                System.out.println("[AutoConfig] DataSource OMITIDO (condición no cumplida)");
            }
        }

        public Optional<Object> getBean(String name) {
            return Optional.ofNullable(beans.get(name));
        }

        public boolean hasBean(String name) { return beans.containsKey(name); }
    }

    public static void main(String[] args) {
        System.out.println("=== @ConditionalOnProperty ===");
        System.out.println();

        // Escenario 1: db.enabled = true → registra DataSource
        System.out.println("--- Escenario 1: db.enabled=true ---");
        Map<String, String> config1 = new HashMap<>();
        config1.put("db.enabled", "true");
        config1.put("db.url", "jdbc:postgresql://localhost/mydb");

        ConditionalContainer container1 = new ConditionalContainer(config1);
        container1.registerDataSourceIfEnabled();
        System.out.println("Tiene dataSource: " + container1.hasBean("dataSource"));

        System.out.println();

        // Escenario 2: db.enabled = false → NO registra DataSource
        System.out.println("--- Escenario 2: db.enabled=false ---");
        Map<String, String> config2 = new HashMap<>();
        config2.put("db.enabled", "false");

        ConditionalContainer container2 = new ConditionalContainer(config2);
        container2.registerDataSourceIfEnabled();
        System.out.println("Tiene dataSource: " + container2.hasBean("dataSource"));

        System.out.println();

        // Escenario 3: db.enabled ausente → NO registra (default false)
        System.out.println("--- Escenario 3: db.enabled ausente ---");
        ConditionalContainer container3 = new ConditionalContainer(new HashMap<>());
        container3.registerDataSourceIfEnabled();
        System.out.println("Tiene dataSource: " + container3.hasBean("dataSource"));
    }
}
