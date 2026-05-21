// Ejercicio 3 — Profile-specific config
// Mapas de configuración por perfil. ProfileConfig.getConfig(profile) devuelve el correcto.

import java.util.HashMap;
import java.util.Map;

public class Ejercicio3 {

    static class ProfileConfig {

        private static final Map<String, String> configBase;
        private static final Map<String, String> configDev;
        private static final Map<String, String> configProd;

        static {
            configBase = new HashMap<>();
            configBase.put("app.name", "java-prep");
            configBase.put("app.version", "1.0.0");
            configBase.put("db.pool.size", "5");

            configDev = new HashMap<>();
            configDev.put("db.url", "jdbc:h2:mem:devdb");
            configDev.put("db.pool.size", "2");
            configDev.put("cache.enabled", "false");
            configDev.put("debug.mode", "true");
            configDev.put("logging.level", "DEBUG");

            configProd = new HashMap<>();
            configProd.put("db.url", "jdbc:postgresql://prod-db:5432/myapp");
            configProd.put("db.pool.size", "20");
            configProd.put("cache.enabled", "true");
            configProd.put("debug.mode", "false");
            configProd.put("logging.level", "WARN");
        }

        /**
         * Devuelve el mapa de config para el perfil dado.
         * Las propiedades del perfil se superponen sobre las base.
         */
        static Map<String, String> getConfig(String profile) {
            // Empezar con la config base
            Map<String, String> merged = new HashMap<>(configBase);

            // Superponer las del perfil específico
            Map<String, String> profileSpecific = switch (profile) {
                case "dev"  -> configDev;
                case "prod" -> configProd;
                default -> new HashMap<>();
            };

            merged.putAll(profileSpecific);
            return merged;
        }
    }

    static void printConfig(String profile) {
        Map<String, String> config = ProfileConfig.getConfig(profile);
        System.out.println("Configuración para perfil: \"" + profile + "\"");
        System.out.println("-".repeat(45));
        config.entrySet().stream()
              .sorted(Map.Entry.comparingByKey())
              .forEach(e -> System.out.printf("  %-22s = %s%n", e.getKey(), e.getValue()));
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 3 — Profile-specific config ===\n");

        printConfig("dev");
        printConfig("prod");
        printConfig("unknown");

        // Verificar que dev y prod tienen valores diferentes en claves comunes
        Map<String, String> dev  = ProfileConfig.getConfig("dev");
        Map<String, String> prod = ProfileConfig.getConfig("prod");

        System.out.println("--- Diferencias entre dev y prod ---");
        String[] keysToCompare = {"db.url", "db.pool.size", "cache.enabled", "logging.level"};
        System.out.printf("%-25s %-35s %s%n", "Clave", "Dev", "Prod");
        System.out.println("-".repeat(75));
        for (String key : keysToCompare) {
            System.out.printf("%-25s %-35s %s%n",
                key,
                dev.getOrDefault(key, "N/A"),
                prod.getOrDefault(key, "N/A"));
        }
    }
}
