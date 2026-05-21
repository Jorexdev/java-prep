import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Ejercicio 3 — Type conversion
// TypedConfig convierte String a tipos Java concretos
public class Ejercicio3 {

    static class TypedConfig {
        private final Map<String, String> props;

        public TypedConfig(Map<String, String> props) {
            this.props = props;
        }

        private String require(String key) {
            String val = props.get(key);
            if (val == null) throw new IllegalArgumentException("Clave no encontrada: " + key);
            return val;
        }

        // String → Integer
        public int getInt(String key) {
            return Integer.parseInt(require(key).trim());
        }

        // String → Boolean ("true"/"false", case-insensitive)
        public boolean getBoolean(String key) {
            return Boolean.parseBoolean(require(key).trim());
        }

        // String → List<String> separando por coma
        public List<String> getList(String key) {
            String raw = require(key);
            return Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        // String → duración en segundos: "5s" → 5, "2m" → 120, "1h" → 3600
        public long getDurationSeconds(String key) {
            String raw = require(key).trim();
            if (raw.endsWith("s")) {
                return Long.parseLong(raw.substring(0, raw.length() - 1));
            } else if (raw.endsWith("m")) {
                return Long.parseLong(raw.substring(0, raw.length() - 1)) * 60;
            } else if (raw.endsWith("h")) {
                return Long.parseLong(raw.substring(0, raw.length() - 1)) * 3600;
            }
            throw new IllegalArgumentException("Formato de duración no soportado: " + raw);
        }
    }

    public static void main(String[] args) {
        Map<String, String> data = new HashMap<>();
        data.put("server.port", "8080");
        data.put("db.pool.size", "10");
        data.put("feature.enabled", "true");
        data.put("cache.disabled", "false");
        data.put("allowed.origins", "http://localhost:3000, https://example.com, https://api.example.com");
        data.put("allowed.methods", "GET, POST, PUT, DELETE");
        data.put("session.timeout", "30m");
        data.put("cache.ttl", "5s");
        data.put("token.expiry", "2h");

        TypedConfig config = new TypedConfig(data);

        System.out.println("=== Type conversion ===");
        System.out.println();

        System.out.println("--- Integer ---");
        System.out.println("server.port    = " + config.getInt("server.port") + "  (tipo: int)");
        System.out.println("db.pool.size   = " + config.getInt("db.pool.size") + " (tipo: int)");

        System.out.println();
        System.out.println("--- Boolean ---");
        System.out.println("feature.enabled  = " + config.getBoolean("feature.enabled") + " (tipo: boolean)");
        System.out.println("cache.disabled   = " + config.getBoolean("cache.disabled") + " (tipo: boolean)");

        System.out.println();
        System.out.println("--- List<String> ---");
        List<String> origins = config.getList("allowed.origins");
        System.out.println("allowed.origins  = " + origins + " (size=" + origins.size() + ")");
        List<String> methods = config.getList("allowed.methods");
        System.out.println("allowed.methods  = " + methods + " (size=" + methods.size() + ")");

        System.out.println();
        System.out.println("--- Duration (en segundos) ---");
        System.out.println("session.timeout = " + config.getDurationSeconds("session.timeout") + "s  (era: 30m)");
        System.out.println("cache.ttl       = " + config.getDurationSeconds("cache.ttl") + "s    (era: 5s)");
        System.out.println("token.expiry    = " + config.getDurationSeconds("token.expiry") + "s (era: 2h)");
    }
}
