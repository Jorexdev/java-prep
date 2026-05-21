import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// Ejercicio 4 (Medio) — Profile-specific overlay
// base.properties + perfil.properties: el perfil sobreescribe la base
public class Ejercicio4 {

    static class ProfileAwareConfig {
        private final Map<String, String> baseProps;
        private final Map<String, Map<String, String>> profileProps;

        ProfileAwareConfig(Map<String, String> baseProps,
                           Map<String, Map<String, String>> profileProps) {
            this.baseProps = baseProps;
            this.profileProps = profileProps;
        }

        /**
         * Fusiona base con el perfil indicado.
         * Las propiedades del perfil sobreescriben las base.
         * Retorna un nuevo mapa con el resultado final.
         */
        public Map<String, String> load(String profile) {
            Map<String, String> result = new HashMap<>(baseProps);

            Map<String, String> overlay = profileProps.getOrDefault(profile, Map.of());
            result.putAll(overlay); // el perfil sobreescribe

            System.out.println("Cargando perfil '" + profile + "':");
            System.out.println("  Base tiene " + baseProps.size() + " propiedades");
            System.out.println("  Overlay '" + profile + "' tiene " + overlay.size() + " propiedades");
            System.out.println("  Resultado final: " + result.size() + " propiedades");
            return result;
        }
    }

    static void printProps(Map<String, String> props) {
        new TreeMap<>(props).forEach((k, v) -> System.out.println("  " + k + " = " + v));
    }

    static void printDiff(String profile, Map<String, String> base, Map<String, String> result) {
        System.out.println("  Cambios respecto a base en perfil '" + profile + "':");
        for (Map.Entry<String, String> e : result.entrySet()) {
            String baseVal = base.get(e.getKey());
            if (baseVal == null) {
                System.out.println("    [NUEVO] " + e.getKey() + " = " + e.getValue());
            } else if (!baseVal.equals(e.getValue())) {
                System.out.println("    [SOBREESCRITO] " + e.getKey()
                        + ": '" + baseVal + "' → '" + e.getValue() + "'");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Profile-specific overlay ===");
        System.out.println();

        // Propiedades base (application.properties)
        Map<String, String> base = new HashMap<>();
        base.put("app.name", "java-prep");
        base.put("server.port", "8080");
        base.put("log.level", "DEBUG");
        base.put("db.url", "jdbc:h2:mem:testdb");
        base.put("db.pool.size", "5");
        base.put("cache.enabled", "false");

        // application-dev.properties
        Map<String, String> dev = new HashMap<>();
        dev.put("log.level", "TRACE");
        dev.put("cache.enabled", "true");
        dev.put("server.port", "8081");

        // application-prod.properties
        Map<String, String> prod = new HashMap<>();
        prod.put("server.port", "443");
        prod.put("log.level", "WARN");
        prod.put("db.url", "jdbc:postgresql://prod-host:5432/mydb");
        prod.put("db.pool.size", "50");
        prod.put("cache.enabled", "true");
        prod.put("app.ssl.enabled", "true");

        Map<String, Map<String, String>> profiles = new HashMap<>();
        profiles.put("dev", dev);
        profiles.put("prod", prod);

        ProfileAwareConfig config = new ProfileAwareConfig(base, profiles);

        System.out.println("--- Base properties ---");
        printProps(base);
        System.out.println();

        System.out.println("=== Perfil: dev ===");
        Map<String, String> devResult = config.load("dev");
        printProps(devResult);
        System.out.println();
        printDiff("dev", base, devResult);

        System.out.println();
        System.out.println("=== Perfil: prod ===");
        Map<String, String> prodResult = config.load("prod");
        printProps(prodResult);
        System.out.println();
        printDiff("prod", base, prodResult);
    }
}
