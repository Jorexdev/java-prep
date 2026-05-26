import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Muestra la abstracción completa de Environment: perfiles activos, perfiles por defecto
// y búsqueda de propiedades con soporte para expresiones Profiles.of().
public class ExpEnvironmentAbstraction {

    // ── Soporte para expresiones de perfil ────────────────────────────────────

    // Equivale a org.springframework.core.env.Profiles
    // Acepta expresiones: "staging | prod", "dev & !docker", "!prod"
    interface ProfileExpression {
        boolean matches(Set<String> activeProfiles);

        // Parsea expresiones simples: A | B, A & B, !A, A
        static ProfileExpression of(String expression) {
            String expr = expression.trim();
            if (expr.contains("|")) {
                String[] parts = expr.split("\\|");
                return active -> Arrays.stream(parts)
                    .map(String::trim)
                    .anyMatch(p -> matchSingle(p, active));
            }
            if (expr.contains("&")) {
                String[] parts = expr.split("&");
                return active -> Arrays.stream(parts)
                    .map(String::trim)
                    .allMatch(p -> matchSingle(p, active));
            }
            return active -> matchSingle(expr, active);
        }

        private static boolean matchSingle(String profile, Set<String> active) {
            if (profile.startsWith("!")) return !active.contains(profile.substring(1));
            return active.contains(profile);
        }
    }

    // ── Implementación de MockEnvironment ─────────────────────────────────────

    // Equivale a org.springframework.mock.env.MockEnvironment
    static class MockEnvironment {
        private final Set<String> activeProfiles  = new HashSet<>();
        private final Set<String> defaultProfiles = new HashSet<>();
        // Propiedades planas + propiedades por perfil
        private final Map<String, String> properties        = new HashMap<>();
        private final Map<String, Map<String, String>> profileProperties = new HashMap<>();

        void setActiveProfiles(String... profiles) {
            activeProfiles.addAll(Arrays.asList(profiles));
        }

        void setDefaultProfiles(String... profiles) {
            defaultProfiles.addAll(Arrays.asList(profiles));
        }

        // Propiedad global (application.properties)
        void setProperty(String key, String value) {
            properties.put(key, value);
        }

        // Propiedad específica de perfil (application-staging.properties)
        void setProfileProperty(String profile, String key, String value) {
            profileProperties.computeIfAbsent(profile, k -> new HashMap<>()).put(key, value);
        }

        Set<String> getActiveProfiles()  { return activeProfiles; }
        Set<String> getDefaultProfiles() { return defaultProfiles; }

        // Resuelve propiedad: primero busca en perfiles activos, luego en globales
        String getProperty(String key) {
            for (String profile : activeProfiles) {
                Map<String, String> pp = profileProperties.get(profile);
                if (pp != null && pp.containsKey(key)) return pp.get(key);
            }
            return properties.get(key);
        }

        String getProperty(String key, String defaultValue) {
            String val = getProperty(key);
            return val != null ? val : defaultValue;
        }

        // Evalúa una expresión de perfiles — equivale a env.acceptsProfiles(Profiles.of(...))
        boolean acceptsProfiles(String expression) {
            Set<String> relevant = activeProfiles.isEmpty() ? defaultProfiles : activeProfiles;
            return ProfileExpression.of(expression).matches(relevant);
        }
    }

    public static void main(String[] args) {
        MockEnvironment env = new MockEnvironment();

        // Configuración de perfiles
        env.setDefaultProfiles("default");  // activo si no hay ningún perfil explícito
        env.setActiveProfiles("staging");

        // Propiedades globales
        env.setProperty("app.name",    "java-prep");
        env.setProperty("app.version", "1.0.0");
        env.setProperty("db.url",      "jdbc:h2:mem:test");  // será sobreescrito por staging

        // Propiedades específicas de perfil
        env.setProfileProperty("staging", "db.url",      "jdbc:postgresql://staging:5432/app");
        env.setProfileProperty("staging", "db.username", "staging_user");
        env.setProfileProperty("prod",    "db.url",      "jdbc:postgresql://prod:5432/app");
        env.setProfileProperty("prod",    "db.username", "prod_user");

        System.out.println("=== Estado del environment ===");
        System.out.println("Perfiles activos:  " + env.getActiveProfiles());
        System.out.println("Perfiles default:  " + env.getDefaultProfiles());

        System.out.println("\n=== Resolución de propiedades ===");
        System.out.println("app.name    → " + env.getProperty("app.name"));
        System.out.println("db.url      → " + env.getProperty("db.url"));      // staging sobreescribe
        System.out.println("db.username → " + env.getProperty("db.username")); // solo en staging
        System.out.println("db.password → " + env.getProperty("db.password", "(no configurado)"));

        System.out.println("\n=== acceptsProfiles con expresiones ===");
        System.out.println("\"staging | prod\"  → " + env.acceptsProfiles("staging | prod"));
        System.out.println("\"staging & !prod\" → " + env.acceptsProfiles("staging & !prod"));
        System.out.println("\"prod\"            → " + env.acceptsProfiles("prod"));
        System.out.println("\"!prod\"           → " + env.acceptsProfiles("!prod"));
        System.out.println("\"dev\"             → " + env.acceptsProfiles("dev"));

        System.out.println("\n=== En Spring Boot ===");
        System.out.println("application-staging.properties sobreescribe application.properties");
        System.out.println("env.acceptsProfiles(Profiles.of(\"staging | prod\")) → uso frecuente en @Conditional");
    }
}
