// Ejercicio 2 — Profile inheritance
// prod-eu hereda de prod, prod hereda de base.
// El sistema resuelve la cadena y produce el config final.

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Ejercicio2 {

    static class ProfileDefinition {
        final String name;
        final String parent;       // null si es raíz
        final Map<String, String> ownProperties = new LinkedHashMap<>();

        ProfileDefinition(String name, String parent) {
            this.name = name;
            this.parent = parent;
        }

        ProfileDefinition set(String key, String value) {
            ownProperties.put(key, value);
            return this;
        }
    }

    static class ProfileInheritanceResolver {
        private final Map<String, ProfileDefinition> profiles = new LinkedHashMap<>();

        void register(ProfileDefinition profile) {
            profiles.put(profile.name, profile);
        }

        /**
         * Resuelve el config final para un perfil, aplicando herencia.
         * El perfil hijo tiene mayor prioridad que el padre.
         */
        Map<String, String> resolve(String profileName) {
            List<ProfileDefinition> chain = buildChain(profileName);

            System.out.print("  Cadena de herencia: ");
            List<String> chainNames = new ArrayList<>();
            for (ProfileDefinition p : chain) chainNames.add(p.name);
            System.out.println(String.join(" ← ", chainNames));

            // Aplicar de mayor a menor prioridad (primero el más alto en la jerarquía)
            Map<String, String> merged = new TreeMap<>();
            for (int i = chain.size() - 1; i >= 0; i--) {
                ProfileDefinition def = chain.get(i);
                for (Map.Entry<String, String> entry : def.ownProperties.entrySet()) {
                    String key = entry.getKey();
                    String newValue = entry.getValue();
                    String oldValue = merged.get(key);
                    if (oldValue != null && !oldValue.equals(newValue)) {
                        System.out.printf("    Override [%s]: %s = '%s' → '%s'%n",
                            def.name, key, oldValue, newValue);
                    }
                    merged.put(key, newValue);
                }
            }
            return merged;
        }

        private List<ProfileDefinition> buildChain(String profileName) {
            List<ProfileDefinition> chain = new ArrayList<>();
            String current = profileName;
            while (current != null) {
                ProfileDefinition def = profiles.get(current);
                if (def == null) throw new IllegalArgumentException("Perfil no registrado: " + current);
                chain.add(def);
                current = def.parent;
            }
            return chain;
        }
    }

    static void printConfig(Map<String, String> config, String label) {
        System.out.println("Config final para '" + label + "':");
        System.out.println("-".repeat(50));
        config.forEach((k, v) -> System.out.printf("  %-30s = %s%n", k, v));
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 2 — Profile inheritance ===\n");

        ProfileInheritanceResolver resolver = new ProfileInheritanceResolver();

        // Perfil base: configuración común para todos los entornos
        resolver.register(new ProfileDefinition("base", null)
            .set("app.name", "java-prep")
            .set("app.version", "1.0.0")
            .set("db.pool.min", "1")
            .set("db.pool.max", "5")
            .set("db.timeout", "30000")
            .set("cache.enabled", "false")
            .set("monitoring.enabled", "false")
            .set("logging.level", "INFO")
        );

        // Perfil prod: hereda de base y sobreescribe/añade
        resolver.register(new ProfileDefinition("prod", "base")
            .set("db.url", "jdbc:postgresql://prod-db:5432/app")
            .set("db.pool.min", "5")
            .set("db.pool.max", "20")
            .set("cache.enabled", "true")
            .set("cache.ttl", "3600")
            .set("monitoring.enabled", "true")
            .set("logging.level", "WARN")
            .set("security.https", "true")
        );

        // Perfil prod-eu: hereda de prod y añade configuración específica de la región UE
        resolver.register(new ProfileDefinition("prod-eu", "prod")
            .set("db.url", "jdbc:postgresql://prod-eu-db.eu-west-1:5432/app")
            .set("db.pool.max", "30")     // más conexiones para UE
            .set("region", "eu-west-1")
            .set("compliance.gdpr", "true")
            .set("data.residency", "EU")
            .set("cdn.endpoint", "https://cdn.eu.example.com")
        );

        // Perfil prod-us: hereda de prod con config para EEUU
        resolver.register(new ProfileDefinition("prod-us", "prod")
            .set("db.url", "jdbc:postgresql://prod-us-db.us-east-1:5432/app")
            .set("region", "us-east-1")
            .set("cdn.endpoint", "https://cdn.us.example.com")
        );

        System.out.println("=== Resolviendo perfil 'base' ===");
        printConfig(resolver.resolve("base"), "base");

        System.out.println("=== Resolviendo perfil 'prod' ===");
        printConfig(resolver.resolve("prod"), "prod");

        System.out.println("=== Resolviendo perfil 'prod-eu' ===");
        printConfig(resolver.resolve("prod-eu"), "prod-eu");

        System.out.println("=== Resolviendo perfil 'prod-us' ===");
        printConfig(resolver.resolve("prod-us"), "prod-us");

        // Verificar que prod-eu tiene GDPR pero prod-us no
        Map<String, String> eu = resolver.resolve("prod-eu");
        Map<String, String> us = resolver.resolve("prod-us");
        System.out.println("--- Diferencias EU vs US ---");
        System.out.println("GDPR en prod-eu : " + eu.getOrDefault("compliance.gdpr", "N/A"));
        System.out.println("GDPR en prod-us : " + us.getOrDefault("compliance.gdpr", "N/A"));
        System.out.println("Region EU       : " + eu.get("region"));
        System.out.println("Region US       : " + us.get("region"));
    }
}
