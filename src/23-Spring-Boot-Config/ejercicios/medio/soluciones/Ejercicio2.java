import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Ejercicio 2 (Medio) — Property source priority chain (4 fuentes)
// defaultValues < propertiesFile < envVars < cliArgs
public class Ejercicio2 {

    interface PropertySource {
        String getName();
        Optional<String> get(String key);
        Map<String, String> asMap();
    }

    static class MapPropertySource implements PropertySource {
        private final String name;
        private final Map<String, String> props;

        MapPropertySource(String name, Map<String, String> props) {
            this.name = name;
            this.props = Map.copyOf(props);
        }

        @Override public String getName() { return name; }
        @Override public Optional<String> get(String key) { return Optional.ofNullable(props.get(key)); }
        @Override public Map<String, String> asMap() { return props; }
    }

    /** Parsea args de la forma --clave=valor */
    static class CliArgsPropertySource implements PropertySource {
        private final Map<String, String> props = new HashMap<>();

        CliArgsPropertySource(String[] args) {
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=")) {
                    String stripped = arg.substring(2);
                    int eq = stripped.indexOf('=');
                    props.put(stripped.substring(0, eq), stripped.substring(eq + 1));
                }
            }
        }

        @Override public String getName() { return "cliArgs"; }
        @Override public Optional<String> get(String key) { return Optional.ofNullable(props.get(key)); }
        @Override public Map<String, String> asMap() { return Map.copyOf(props); }
    }

    static class PropertySourceChain {
        // De menor a mayor prioridad
        private final List<PropertySource> sources = new ArrayList<>();

        public void addSource(PropertySource source) {
            sources.add(source);
        }

        public Optional<String> get(String key) {
            for (int i = sources.size() - 1; i >= 0; i--) {
                Optional<String> val = sources.get(i).get(key);
                if (val.isPresent()) {
                    return val;
                }
            }
            return Optional.empty();
        }

        /** Devuelve la fuente que provee el valor ganador de una clave */
        public String getSource(String key) {
            for (int i = sources.size() - 1; i >= 0; i--) {
                if (sources.get(i).get(key).isPresent()) return sources.get(i).getName();
            }
            return "ninguna";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Property source priority chain (4 fuentes) ===");
        System.out.println("Prioridad: defaultValues < propertiesFile < envVars < cliArgs");
        System.out.println();

        Map<String, String> defaults = new HashMap<>();
        defaults.put("server.port", "8080");
        defaults.put("app.name", "default-app");
        defaults.put("log.level", "DEBUG");
        defaults.put("db.pool", "5");

        Map<String, String> propsFile = new HashMap<>();
        propsFile.put("server.port", "9090");
        propsFile.put("app.name", "app-from-file");
        propsFile.put("db.url", "jdbc:h2:mem:filedb");

        Map<String, String> envVars = new HashMap<>();
        envVars.put("server.port", "8443");
        envVars.put("app.name", "app-from-env");
        envVars.put("db.url", "jdbc:postgresql://staging/mydb");

        // CLI args con la prioridad más alta
        String[] cliArgsArr = {"--server.port=443", "--app.name=cli-app"};
        CliArgsPropertySource cliSource = new CliArgsPropertySource(cliArgsArr);
        System.out.println("CLI args recibidos:");
        for (String a : cliArgsArr) System.out.println("  " + a);
        System.out.println();

        PropertySourceChain chain = new PropertySourceChain();
        chain.addSource(new MapPropertySource("defaultValues", defaults));
        chain.addSource(new MapPropertySource("propertiesFile", propsFile));
        chain.addSource(new MapPropertySource("envVars", envVars));
        chain.addSource(cliSource);

        String[] keys = {"server.port", "app.name", "db.url", "log.level", "db.pool"};
        System.out.printf("%-30s %-18s %s%n", "CLAVE", "FUENTE GANADORA", "VALOR");
        System.out.println("-".repeat(65));
        for (String key : keys) {
            String val = chain.get(key).orElse("N/A");
            String src = chain.getSource(key);
            System.out.printf("%-30s %-18s %s%n", key, src, val);
        }

        System.out.println();
        System.out.println("Nota: server.port y app.name están en las 4 fuentes → gana cliArgs");
    }
}
