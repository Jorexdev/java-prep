// Ejercicio 1 — Profile básico
// Interfaz DataSource con implementaciones H2 (dev) y Postgres (prod).
// ProfileSelector instancia la correcta según el perfil activo.

// @Component
// @Profile("dev")
// class H2DataSource implements DataSource { ... }

// @Component
// @Profile("prod")
// class PostgresDataSource implements DataSource { ... }

public class Ejercicio1 {

    interface DataSource {
        String getUrl();
        String getDriverClass();
    }

    // @Profile("dev")
    static class H2DataSource implements DataSource {
        @Override
        public String getUrl() {
            return "jdbc:h2:mem:testdb";
        }
        @Override
        public String getDriverClass() {
            return "org.h2.Driver";
        }
        @Override
        public String toString() {
            return "H2DataSource [url=" + getUrl() + ", driver=" + getDriverClass() + "]";
        }
    }

    // @Profile("prod")
    static class PostgresDataSource implements DataSource {
        @Override
        public String getUrl() {
            return "jdbc:postgresql://localhost:5432/myapp";
        }
        @Override
        public String getDriverClass() {
            return "org.postgresql.Driver";
        }
        @Override
        public String toString() {
            return "PostgresDataSource [url=" + getUrl() + ", driver=" + getDriverClass() + "]";
        }
    }

    static class ProfileSelector {
        private final String activeProfile;

        ProfileSelector(String activeProfile) {
            this.activeProfile = activeProfile;
        }

        DataSource selectDataSource() {
            return switch (activeProfile) {
                case "dev"  -> new H2DataSource();
                case "prod" -> new PostgresDataSource();
                default -> throw new IllegalArgumentException("Perfil desconocido: " + activeProfile);
            };
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 1 — Profile básico ===\n");

        String[] profiles = {"dev", "prod"};

        for (String profile : profiles) {
            ProfileSelector selector = new ProfileSelector(profile);
            DataSource ds = selector.selectDataSource();
            System.out.println("Perfil activo : " + profile);
            System.out.println("Bean activo   : " + ds);
            System.out.println("URL           : " + ds.getUrl());
            System.out.println("Driver        : " + ds.getDriverClass());
            System.out.println();
        }

        // Simular lo que haría Spring con @Profile
        System.out.println("--- Comportamiento equivalente en Spring ---");
        System.out.println("Con @Profile(\"dev\")  → H2DataSource se registra en el contexto");
        System.out.println("Con @Profile(\"prod\") → PostgresDataSource se registra en el contexto");
        System.out.println("Spring inyecta el bean correcto donde se declare DataSource como dependencia");
    }
}
