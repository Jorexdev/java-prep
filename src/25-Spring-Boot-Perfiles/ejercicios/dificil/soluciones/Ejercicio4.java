import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio4 {

    static class Environment {
        private final List<String> activeProfiles;
        private final List<String> defaultProfiles;
        private final Map<String, String> properties;

        Environment(List<String> activeProfiles, Map<String, String> properties) {
            this.activeProfiles  = activeProfiles;
            this.defaultProfiles = List.of("default");
            this.properties      = properties;
        }

        String getProperty(String key) {
            return properties.getOrDefault(key, null);
        }

        String getProperty(String key, String defaultValue) {
            return properties.getOrDefault(key, defaultValue);
        }

        List<String> getActiveProfiles()  { return activeProfiles; }
        List<String> getDefaultProfiles() { return defaultProfiles; }

        boolean acceptsProfiles(String... profiles) {
            return Arrays.stream(profiles).anyMatch(activeProfiles::contains);
        }
    }

    // Bean que adapta su log level según el perfil
    static class LoggingBean {
        void configure(Environment env) {
            String level = env.acceptsProfiles("dev") ? "DEBUG" :
                           env.acceptsProfiles("prod") ? "WARN" : "INFO";
            System.out.println("[LoggingBean] Log level = " + level
                + "  (perfil: " + env.getActiveProfiles() + ")");
        }
    }

    // Bean que elige su datasource según el perfil
    static class DataSourceBean {
        void configure(Environment env) {
            String url;
            if (env.acceptsProfiles("prod"))
                url = env.getProperty("db.url", "jdbc:postgresql://prod-host/appdb");
            else if (env.acceptsProfiles("test"))
                url = "jdbc:h2:mem:testdb";
            else
                url = "jdbc:h2:mem:devdb";
            System.out.println("[DataSourceBean] URL = " + url);
        }
    }

    // Bean que ajusta timeouts según el perfil
    static class HttpClientBean {
        void configure(Environment env) {
            int timeout = env.acceptsProfiles("prod") ? 5000 :
                          env.acceptsProfiles("dev")  ? 30000 : 10000;
            System.out.println("[HttpClientBean] Timeout = " + timeout + "ms");
        }
    }

    public static void main(String[] args) {
        String[][] scenarios = {
            {"dev"},
            {"prod"},
            {"test"},
        };

        for (String[] profiles : scenarios) {
            Map<String, String> props = new HashMap<>();
            props.put("db.url", "jdbc:postgresql://prod-host/appdb");

            Environment env = new Environment(Arrays.asList(profiles), props);
            System.out.println("=== Perfiles: " + Arrays.toString(profiles) + " ===");
            new LoggingBean().configure(env);
            new DataSourceBean().configure(env);
            new HttpClientBean().configure(env);
            System.out.println();
        }
    }
}
