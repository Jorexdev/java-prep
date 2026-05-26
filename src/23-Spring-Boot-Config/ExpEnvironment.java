import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Simula el Environment de Spring y su jerarquía de PropertySources.
// La clave que aparece primero en la cadena gana (mayor prioridad).
public class ExpEnvironment {

    // ── PropertySource ────────────────────────────────────────────────────────

    // Equivale a org.springframework.core.env.PropertySource
    static class PropertySource {
        private final String name;
        private final Map<String, String> properties;

        PropertySource(String name, Map<String, String> properties) {
            this.name       = name;
            this.properties = properties;
        }

        String get(String key) { return properties.get(key); }
        boolean contains(String key) { return properties.containsKey(key); }
        String name() { return name; }
    }

    // ── Cadena de PropertySources ─────────────────────────────────────────────

    // Equivale a MutablePropertySources + ConfigurableEnvironment
    static class PropertySourceChain {
        // Lista ordenada por prioridad: índice 0 = mayor prioridad
        private final List<PropertySource> sources = new ArrayList<>();

        void addFirst(PropertySource source) { sources.add(0, source); }
        void addLast(PropertySource source)  { sources.add(source); }

        // Busca en orden hasta encontrar la primera definición
        String getProperty(String key) {
            for (PropertySource source : sources) {
                if (source.contains(key)) {
                    System.out.println("  [ENV] '" + key + "' encontrado en [" + source.name() + "]");
                    return source.get(key);
                }
            }
            return null;
        }

        String getProperty(String key, String defaultValue) {
            String val = getProperty(key);
            return val != null ? val : defaultValue;
        }
    }

    public static void main(String[] args) {
        // ── Fuentes de propiedades — de mayor a menor prioridad ───────────────
        // En Spring Boot el orden real es:
        // 1. Argumentos de línea de comandos
        // 2. Variables de entorno del sistema operativo
        // 3. Propiedades de sistema JVM (-D flags)
        // 4. application-{profile}.properties
        // 5. application.properties
        // 6. Valores por defecto en @ConfigurationProperties

        Map<String, String> systemProps = new HashMap<>();
        systemProps.put("db.url",     "jdbc:postgresql://prod-server/db");  // override de prod
        systemProps.put("app.debug",  "false");

        Map<String, String> appProperties = new HashMap<>();
        appProperties.put("db.url",      "jdbc:h2:mem:test");               // será sobreescrito
        appProperties.put("db.username", "admin");
        appProperties.put("app.name",    "java-prep");
        appProperties.put("app.debug",   "true");                           // será sobreescrito

        Map<String, String> defaults = new HashMap<>();
        defaults.put("db.url",          "jdbc:h2:mem:default");
        defaults.put("db.username",     "sa");
        defaults.put("db.maxPoolSize",  "10");
        defaults.put("app.name",        "app");

        PropertySourceChain env = new PropertySourceChain();
        env.addLast(new PropertySource("defaults",            defaults));
        env.addLast(new PropertySource("application.properties", appProperties));
        env.addFirst(new PropertySource("systemProperties",   systemProps));

        System.out.println("Prioridad (alta → baja):");
        System.out.println("  [systemProperties] → [application.properties] → [defaults]");

        System.out.println("\n=== db.url (definida en system + application + defaults) ===");
        System.out.println("  Valor: " + env.getProperty("db.url"));

        System.out.println("\n=== db.username (definida en application + defaults) ===");
        System.out.println("  Valor: " + env.getProperty("db.username"));

        System.out.println("\n=== db.maxPoolSize (solo en defaults) ===");
        System.out.println("  Valor: " + env.getProperty("db.maxPoolSize"));

        System.out.println("\n=== app.debug (definida en system + application, system gana) ===");
        System.out.println("  Valor: " + env.getProperty("app.debug"));

        System.out.println("\n=== clave inexistente con default ===");
        System.out.println("  Valor: " + env.getProperty("app.version", "1.0.0-SNAPSHOT"));
    }
}
