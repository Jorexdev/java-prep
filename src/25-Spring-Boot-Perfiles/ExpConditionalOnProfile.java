import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

// Simula @Profile como @Conditional: el bean solo se registra si el perfil activo coincide.
// Spring implementa @Profile internamente usando la interfaz Condition.
public class ExpConditionalOnProfile {

    // ── Entorno con perfiles ──────────────────────────────────────────────────

    static class Environment {
        private final Set<String> activeProfiles = new HashSet<>();

        void setActiveProfiles(String... profiles) {
            activeProfiles.addAll(Arrays.asList(profiles));
        }

        boolean accepts(String profile) {
            // Soporta negación: "!prod" → activo cuando prod NO está activo
            if (profile.startsWith("!")) {
                return !activeProfiles.contains(profile.substring(1));
            }
            return activeProfiles.contains(profile);
        }

        Set<String> getActiveProfiles() { return activeProfiles; }
    }

    // ── @Profile como Condition ───────────────────────────────────────────────

    // Equivale a org.springframework.context.annotation.ProfileCondition (clase interna de Spring)
    static class ProfileCondition {
        private final String[] requiredProfiles;

        ProfileCondition(String... profiles) {
            this.requiredProfiles = profiles;
        }

        // Devuelve true si al menos uno de los perfiles requeridos está activo
        boolean matches(Environment env) {
            return Arrays.stream(requiredProfiles).anyMatch(env::accepts);
        }
    }

    // ── Tipos de DataSource ───────────────────────────────────────────────────

    interface DataSource {
        String getConnection();
    }

    // @Component @Profile("dev")
    static class LocalDataSource implements DataSource {
        @Override public String getConnection() {
            return "H2 in-memory @ jdbc:h2:mem:devdb";
        }
    }

    // @Component @Profile("prod")
    static class RdsDataSource implements DataSource {
        @Override public String getConnection() {
            return "AWS RDS @ jdbc:postgresql://rds.amazonaws.com:5432/proddb";
        }
    }

    // @Component @Profile("staging")
    static class StagingDataSource implements DataSource {
        @Override public String getConnection() {
            return "Staging DB @ jdbc:postgresql://staging-server:5432/stagingdb";
        }
    }

    // ── Registro condicional por perfil ───────────────────────────────────────

    static class ProfileAwareBeanRegistry {
        private final Environment env;

        ProfileAwareBeanRegistry(Environment env) { this.env = env; }

        DataSource resolveDataSource() {
            if (new ProfileCondition("dev").matches(env)) {
                System.out.println("  [Profile: dev] → LocalDataSource");
                return new LocalDataSource();
            }
            if (new ProfileCondition("staging").matches(env)) {
                System.out.println("  [Profile: staging] → StagingDataSource");
                return new StagingDataSource();
            }
            if (new ProfileCondition("prod").matches(env)) {
                System.out.println("  [Profile: prod] → RdsDataSource");
                return new RdsDataSource();
            }
            throw new IllegalStateException(
                "Ningún DataSource disponible para perfiles: " + env.getActiveProfiles());
        }
    }

    static void demo(String... profiles) {
        Environment env = new Environment();
        env.setActiveProfiles(profiles);
        System.out.println("Perfiles activos: " + env.getActiveProfiles());
        ProfileAwareBeanRegistry registry = new ProfileAwareBeanRegistry(env);
        DataSource ds = registry.resolveDataSource();
        System.out.println("  Conexión: " + ds.getConnection());
    }

    public static void main(String[] args) {
        System.out.println("=== Perfil dev ===");
        demo("dev");

        System.out.println("\n=== Perfil prod ===");
        demo("prod");

        System.out.println("\n=== Perfil staging ===");
        demo("staging");

        System.out.println("\n=== Perfil desconocido → excepción ===");
        try {
            demo("test");
        } catch (IllegalStateException e) {
            System.out.println("  Error: " + e.getMessage());
        }

        System.out.println("\n=== En Spring Boot ===");
        System.out.println("  SPRING_PROFILES_ACTIVE=prod  →  RdsDataSource auto-wired");
        System.out.println("  --spring.profiles.active=dev →  LocalDataSource auto-wired");
        System.out.println("  @Profile(\"!prod\") activa el bean en cualquier perfil excepto prod");
    }
}
