import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ejercicio2 {

    static class Dependency {
        final String groupId;
        final String artifactId;
        final String version;

        Dependency(String g, String a, String v) {
            this.groupId = g; this.artifactId = a; this.version = v;
        }

        String coords() { return groupId + ":" + artifactId + ":" + version; }
        String key()    { return groupId + ":" + artifactId; }

        // Versión como comparable (simplificado: mayor número = mayor versión)
        int majorVersion() {
            try { return Integer.parseInt(version.split("\\.")[0]); }
            catch (NumberFormatException e) { return 0; }
        }
    }

    static class Pom {
        final String artifactId;
        final Map<String, String> properties;
        final List<Dependency> dependencies;
        final List<Dependency> transitiveDeps;

        Pom(String artifactId, Map<String, String> properties,
            List<Dependency> direct, List<Dependency> transitive) {
            this.artifactId    = artifactId;
            this.properties    = properties;
            this.dependencies  = direct;
            this.transitiveDeps = transitive;
        }
    }

    // Interfaz de regla del enforcer
    interface EnforcerRule {
        List<String> validate(Pom pom);
        String name();
    }

    static class RequireJava implements EnforcerRule {
        private final int requiredVersion;

        RequireJava(int version) { this.requiredVersion = version; }

        @Override public String name() { return "RequireJava(" + requiredVersion + ")"; }

        @Override
        public List<String> validate(Pom pom) {
            List<String> violations = new ArrayList<>();
            String javaVersion = pom.properties.getOrDefault("java.version", "0");
            try {
                int declared = Integer.parseInt(javaVersion);
                if (declared < requiredVersion) {
                    violations.add("java.version=" + declared + " < requerido " + requiredVersion);
                }
            } catch (NumberFormatException e) {
                violations.add("java.version no es un número válido: " + javaVersion);
            }
            return violations;
        }
    }

    static class BannedDependency implements EnforcerRule {
        private final String bannedKey; // "groupId:artifactId"

        BannedDependency(String coordinates) {
            String[] parts = coordinates.split(":");
            this.bannedKey = parts[0] + ":" + parts[1];
        }

        @Override public String name() { return "BannedDependency(" + bannedKey + ")"; }

        @Override
        public List<String> validate(Pom pom) {
            List<String> violations = new ArrayList<>();
            pom.dependencies.stream()
                    .filter(d -> d.key().equals(bannedKey))
                    .forEach(d -> violations.add("Dependencia prohibida encontrada: " + d.coords()));
            return violations;
        }
    }

    static class RequireUpperBoundDeps implements EnforcerRule {
        @Override public String name() { return "RequireUpperBoundDeps"; }

        @Override
        public List<String> validate(Pom pom) {
            List<String> violations = new ArrayList<>();
            for (Dependency direct : pom.dependencies) {
                pom.transitiveDeps.stream()
                        .filter(t -> t.key().equals(direct.key()))
                        .filter(t -> t.majorVersion() > direct.majorVersion())
                        .forEach(t -> violations.add(
                            "Upper bound violation: " + direct.coords()
                            + " pero transitiva requiere " + t.version));
            }
            return violations;
        }
    }

    static class EnforcerPlugin {
        private final List<EnforcerRule> rules = new ArrayList<>();

        void addRule(EnforcerRule rule) { rules.add(rule); }

        void enforce(Pom pom) {
            System.out.println("[INFO] --- maven-enforcer-plugin:enforce ---");
            boolean allPassed = true;
            for (EnforcerRule rule : rules) {
                List<String> violations = rule.validate(pom);
                if (violations.isEmpty()) {
                    System.out.println("[INFO]   [PASS] " + rule.name());
                } else {
                    allPassed = false;
                    System.out.println("[ERROR]  [FAIL] " + rule.name());
                    violations.forEach(v -> System.out.println("[ERROR]    -> " + v));
                }
            }
            System.out.println();
            if (allPassed) {
                System.out.println("[INFO] Todas las reglas pasaron.");
            } else {
                System.out.println("[ERROR] BUILD FAILURE — El enforcer encontró violaciones.");
            }
        }
    }

    public static void main(String[] args) {
        // POM con violaciones
        Pom pom = new Pom("my-app",
            Map.of("java.version", "11"),  // viola RequireJava(21)
            List.of(
                new Dependency("org.springframework", "spring-context", "6.1.2"),
                new Dependency("log4j",               "log4j",          "1.2.17"), // BANNED
                new Dependency("com.example",         "utils",          "1.0")
            ),
            List.of(
                new Dependency("com.example", "utils", "3.0") // transitiva con versión MAYOR que direct 1.0
            )
        );

        EnforcerPlugin enforcer = new EnforcerPlugin();
        enforcer.addRule(new RequireJava(21));
        enforcer.addRule(new BannedDependency("log4j:log4j:1.2.17"));
        enforcer.addRule(new RequireUpperBoundDeps());

        System.out.println("=== POM: " + pom.artifactId + " ===");
        System.out.println("java.version = " + pom.properties.get("java.version"));
        System.out.println("Dependencias directas:");
        pom.dependencies.forEach(d -> System.out.println("  " + d.coords()));
        System.out.println("Dependencias transitivas:");
        pom.transitiveDeps.forEach(d -> System.out.println("  " + d.coords()));
        System.out.println();

        enforcer.enforce(pom);
    }
}
