import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

// Ejercicio 1 — Properties reader
// PropertiesSource con get(key) y get(key, default)
public class Ejercicio1 {

    static class PropertiesSource {
        private final Map<String, String> props;

        public PropertiesSource(Map<String, String> props) {
            this.props = Map.copyOf(props);
        }

        public String get(String key) {
            if (!props.containsKey(key)) {
                throw new NoSuchElementException("Propiedad no encontrada: " + key);
            }
            return props.get(key);
        }

        public String get(String key, String defaultValue) {
            return props.getOrDefault(key, defaultValue);
        }
    }

    public static void main(String[] args) {
        Map<String, String> data = new HashMap<>();
        data.put("app.name", "mi-aplicacion");
        data.put("app.version", "2.1.0");
        data.put("server.host", "localhost");
        data.put("server.port", "8080");
        data.put("db.url", "jdbc:postgresql://localhost/mydb");

        PropertiesSource source = new PropertiesSource(data);

        System.out.println("=== Properties reader ===");
        System.out.println("app.name       = " + source.get("app.name"));
        System.out.println("app.version    = " + source.get("app.version"));
        System.out.println("server.host    = " + source.get("server.host"));
        System.out.println("server.port    = " + source.get("server.port"));
        System.out.println("db.url         = " + source.get("db.url"));

        System.out.println();
        System.out.println("=== Con default (clave ausente) ===");
        System.out.println("app.env (miss) = " + source.get("app.env", "development"));
        System.out.println("log.level (miss)= " + source.get("log.level", "INFO"));

        System.out.println();
        System.out.println("=== Sin default (lanza excepción) ===");
        try {
            source.get("no.existe");
        } catch (NoSuchElementException e) {
            System.out.println("Excepción capturada: " + e.getMessage());
        }
    }
}
