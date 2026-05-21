// Ejercicio 2 — Default profile
// Tres implementaciones: H2 (dev), Postgres (prod), Default (sin perfil).
// Demostrar los tres casos: dev, prod, sin perfil.

// @Profile("default") — se activa cuando NO hay ningún perfil explícito
// @Profile("dev")
// @Profile("prod")

public class Ejercicio2 {

    interface DataSource {
        String getUrl();
        String getDescription();
    }

    // @Profile("dev")
    static class H2DataSource implements DataSource {
        @Override public String getUrl() { return "jdbc:h2:mem:testdb"; }
        @Override public String getDescription() { return "H2 en memoria para desarrollo"; }
    }

    // @Profile("prod")
    static class PostgresDataSource implements DataSource {
        @Override public String getUrl() { return "jdbc:postgresql://prod-server:5432/myapp"; }
        @Override public String getDescription() { return "PostgreSQL para producción"; }
    }

    // @Profile("default") — activo cuando spring.profiles.active no está definido
    static class DefaultDataSource implements DataSource {
        @Override public String getUrl() { return "jdbc:h2:file:./default-db"; }
        @Override public String getDescription() { return "H2 en fichero — perfil por defecto"; }
    }

    static class ProfileContainer {
        private final String activeProfile;

        ProfileContainer(String activeProfile) {
            // null o vacío → perfil "default"
            this.activeProfile = (activeProfile == null || activeProfile.isBlank())
                    ? "default"
                    : activeProfile;
        }

        DataSource getDataSource() {
            return switch (activeProfile) {
                case "dev"     -> new H2DataSource();
                case "prod"    -> new PostgresDataSource();
                case "default" -> new DefaultDataSource();
                default -> throw new IllegalArgumentException("Perfil no reconocido: " + activeProfile);
            };
        }

        String getResolvedProfile() {
            return activeProfile;
        }
    }

    static void demo(String rawProfile) {
        ProfileContainer container = new ProfileContainer(rawProfile);
        DataSource ds = container.getDataSource();

        System.out.println("Input perfil   : " + (rawProfile == null ? "null" : "\"" + rawProfile + "\""));
        System.out.println("Perfil resuelto: " + container.getResolvedProfile());
        System.out.println("Bean activo    : " + ds.getClass().getSimpleName());
        System.out.println("URL            : " + ds.getUrl());
        System.out.println("Descripción    : " + ds.getDescription());
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 2 — Default profile ===\n");

        System.out.println("--- Caso 1: perfil \"dev\" ---");
        demo("dev");

        System.out.println("--- Caso 2: perfil \"prod\" ---");
        demo("prod");

        System.out.println("--- Caso 3: sin perfil (null) ---");
        demo(null);

        System.out.println("--- Caso 4: sin perfil (vacío) ---");
        demo("");

        System.out.println("--- Equivalencia en Spring ---");
        System.out.println("@Profile(\"default\") se activa automáticamente");
        System.out.println("cuando spring.profiles.active no está configurado.");
        System.out.println("Es equivalente a no poner @Profile, pero más explícito.");
    }
}
