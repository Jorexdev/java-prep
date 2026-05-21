// Ejercicio 2 — Config overlay
// application.properties base + application-dev.properties que sobreescribe algunas claves.
// Las propiedades del perfil ganan sobre las base.

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class Ejercicio2 {

    static class Properties {
        private final Map<String, String> data = new LinkedHashMap<>();
        private final String sourceName;

        Properties(String sourceName) {
            this.sourceName = sourceName;
        }

        Properties set(String key, String value) {
            data.put(key, value);
            return this;
        }

        Map<String, String> getData() {
            return data;
        }

        String getSourceName() {
            return sourceName;
        }
    }

    static class PropertyResolver {
        private final Properties base;
        private final Map<String, Properties> profileProperties = new HashMap<>();

        PropertyResolver(Properties base) {
            this.base = base;
        }

        void addProfileProperties(String profile, Properties props) {
            profileProperties.put(profile, props);
        }

        /**
         * Fusiona: base + perfil activo (perfil tiene prioridad).
         * Equivale a lo que hace Spring Boot al cargar application.properties
         * y application-{profile}.properties.
         */
        Map<String, String> resolve(String activeProfile) {
            // Empezar con base
            Map<String, String> merged = new TreeMap<>(base.getData());

            // Si hay propiedades para el perfil activo, sobreescribir
            if (activeProfile != null && profileProperties.containsKey(activeProfile)) {
                Properties profileProps = profileProperties.get(activeProfile);
                System.out.println("  Aplicando override desde " + profileProps.getSourceName());
                profileProps.getData().forEach((k, v) -> {
                    String previous = merged.get(k);
                    if (previous != null && !previous.equals(v)) {
                        System.out.printf("    Override: %s = '%s' → '%s'%n", k, previous, v);
                    } else if (previous == null) {
                        System.out.printf("    Nueva:    %s = '%s'%n", k, v);
                    }
                    merged.put(k, v);
                });
            } else {
                System.out.println("  No hay override para perfil: " + activeProfile);
            }

            return merged;
        }
    }

    static void showConfig(Map<String, String> config, String label) {
        System.out.println("Config final [" + label + "]:");
        config.forEach((k, v) -> System.out.printf("  %-30s = %s%n", k, v));
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 2 — Config overlay ===\n");

        // application.properties (base)
        Properties base = new Properties("application.properties")
            .set("app.name", "java-prep")
            .set("app.timeout", "30000")
            .set("app.debug", "false")
            .set("app.max-connections", "10")
            .set("logging.level.root", "INFO");

        // application-dev.properties
        Properties devProps = new Properties("application-dev.properties")
            .set("app.timeout", "5000")         // sobreescribe
            .set("app.debug", "true")            // sobreescribe
            .set("app.max-connections", "2")     // sobreescribe
            .set("logging.level.root", "DEBUG")  // sobreescribe
            .set("dev.tools.enabled", "true");   // nueva clave solo en dev

        // application-prod.properties
        Properties prodProps = new Properties("application-prod.properties")
            .set("app.timeout", "60000")
            .set("app.max-connections", "50")
            .set("logging.level.root", "WARN")
            .set("monitoring.enabled", "true");

        PropertyResolver resolver = new PropertyResolver(base);
        resolver.addProfileProperties("dev", devProps);
        resolver.addProfileProperties("prod", prodProps);

        System.out.println("--- Sin perfil activo (solo base) ---");
        Map<String, String> noProfile = resolver.resolve(null);
        showConfig(noProfile, "sin perfil");

        System.out.println("--- Perfil 'dev' activo ---");
        Map<String, String> devConfig = resolver.resolve("dev");
        showConfig(devConfig, "dev");

        System.out.println("--- Perfil 'prod' activo ---");
        Map<String, String> prodConfig = resolver.resolve("prod");
        showConfig(prodConfig, "prod");

        System.out.println("--- Orden de precedencia en Spring Boot ---");
        System.out.println("  (mayor precedencia primero)");
        System.out.println("  1. CLI args (--app.timeout=...)");
        System.out.println("  2. System properties (-Dapp.timeout=...)");
        System.out.println("  3. application-{profile}.properties");
        System.out.println("  4. application.properties");
        System.out.println("  5. @PropertySource annotations");
        System.out.println("  6. Default values en @Value");
    }
}
