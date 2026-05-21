import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Ejercicio 5 — Config precedence
// PropertySourceChain: defaults < propertiesFile < envVars
public class Ejercicio5 {

    interface PropertySource {
        String getName();
        Optional<String> get(String key);
    }

    static class MapPropertySource implements PropertySource {
        private final String name;
        private final Map<String, String> props;

        MapPropertySource(String name, Map<String, String> props) {
            this.name = name;
            this.props = Map.copyOf(props);
        }

        @Override
        public String getName() { return name; }

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(props.get(key));
        }
    }

    /**
     * Cadena de fuentes de propiedades ordenadas de menor a mayor prioridad.
     * La última fuente agregada es la de mayor prioridad.
     */
    static class PropertySourceChain {
        // Almacenadas de menor a mayor prioridad
        private final List<PropertySource> sources = new ArrayList<>();

        public void addSource(PropertySource source) {
            sources.add(source);
        }

        /**
         * Devuelve el valor de la fuente de mayor prioridad que contenga la clave.
         */
        public Optional<String> get(String key) {
            // Iterar de mayor a menor prioridad (de atrás hacia adelante)
            for (int i = sources.size() - 1; i >= 0; i--) {
                Optional<String> val = sources.get(i).get(key);
                if (val.isPresent()) {
                    System.out.println("  '" + key + "' encontrada en [" + sources.get(i).getName() + "]");
                    return val;
                }
            }
            System.out.println("  '" + key + "' no encontrada en ninguna fuente");
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Config precedence ===");
        System.out.println("Prioridad: defaults < propertiesFile < envVars");
        System.out.println();

        // Fuente 1: defaults (prioridad baja)
        Map<String, String> defaults = new HashMap<>();
        defaults.put("app.name", "default-app");
        defaults.put("server.port", "8080");
        defaults.put("log.level", "DEBUG");
        defaults.put("db.pool", "5");

        // Fuente 2: properties file (prioridad media)
        Map<String, String> propsFile = new HashMap<>();
        propsFile.put("app.name", "mi-app-from-file");
        propsFile.put("server.port", "9090");
        propsFile.put("db.url", "jdbc:h2:mem:filedb");

        // Fuente 3: env vars (prioridad alta)
        Map<String, String> envVars = new HashMap<>();
        envVars.put("app.name", "mi-app-produccion");
        envVars.put("db.url", "jdbc:postgresql://prod-host/mydb");

        PropertySourceChain chain = new PropertySourceChain();
        chain.addSource(new MapPropertySource("defaults", defaults));
        chain.addSource(new MapPropertySource("propertiesFile", propsFile));
        chain.addSource(new MapPropertySource("envVars", envVars));

        System.out.println("--- app.name (en las 3 fuentes → gana envVars) ---");
        System.out.println("  Valor: " + chain.get("app.name").orElse("N/A"));

        System.out.println();
        System.out.println("--- server.port (en defaults y propertiesFile → gana propertiesFile) ---");
        System.out.println("  Valor: " + chain.get("server.port").orElse("N/A"));

        System.out.println();
        System.out.println("--- db.url (en propertiesFile y envVars → gana envVars) ---");
        System.out.println("  Valor: " + chain.get("db.url").orElse("N/A"));

        System.out.println();
        System.out.println("--- log.level (solo en defaults → usa defaults) ---");
        System.out.println("  Valor: " + chain.get("log.level").orElse("N/A"));

        System.out.println();
        System.out.println("--- db.pool (solo en defaults → usa defaults) ---");
        System.out.println("  Valor: " + chain.get("db.pool").orElse("N/A"));

        System.out.println();
        System.out.println("--- no.existe (ausente en todas) ---");
        System.out.println("  Valor: " + chain.get("no.existe").orElse("N/A"));
    }
}
