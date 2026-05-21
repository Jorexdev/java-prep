import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    static class Dependency {
        final String coordinates;
        Dependency(String coords) { this.coordinates = coords; }
        @Override public String toString() { return coordinates; }
    }

    static class Profile {
        final String id;
        final List<Dependency> dependencies;
        final Map<String, String> properties;

        Profile(String id, List<Dependency> dependencies, Map<String, String> properties) {
            this.id = id;
            this.dependencies = dependencies;
            this.properties = properties;
        }
    }

    static class Pom {
        final String artifactId;
        final List<Dependency> baseDependencies;
        final Map<String, String> baseProperties;
        final List<Profile> profiles = new ArrayList<>();

        Pom(String artifactId, List<Dependency> deps, Map<String, String> props) {
            this.artifactId = artifactId;
            this.baseDependencies = deps;
            this.baseProperties = props;
        }

        void addProfile(Profile profile) { profiles.add(profile); }

        // Activa los perfiles con los IDs indicados y retorna el POM efectivo
        EffectivePom activate(List<String> activeProfileIds) {
            Map<String, String> mergedProps = new LinkedHashMap<>(baseProperties);
            List<Dependency> mergedDeps = new ArrayList<>(baseDependencies);

            for (String id : activeProfileIds) {
                profiles.stream()
                        .filter(p -> p.id.equals(id))
                        .findFirst()
                        .ifPresentOrElse(profile -> {
                            System.out.println("[INFO] Activando perfil: " + profile.id);
                            mergedProps.putAll(profile.properties);      // sobreescribir
                            mergedDeps.addAll(profile.dependencies);     // añadir
                        }, () -> System.out.println("[WARN] Perfil no encontrado: " + id));
            }
            return new EffectivePom(artifactId, mergedDeps, mergedProps, activeProfileIds);
        }
    }

    static class EffectivePom {
        final String artifactId;
        final List<Dependency> dependencies;
        final Map<String, String> properties;
        final List<String> activeProfiles;

        EffectivePom(String artifactId, List<Dependency> deps,
                     Map<String, String> props, List<String> activeProfiles) {
            this.artifactId = artifactId;
            this.dependencies = deps;
            this.properties = props;
            this.activeProfiles = activeProfiles;
        }

        void print() {
            System.out.println("=== Effective POM: " + artifactId + " ===");
            System.out.println("Perfiles activos: " + activeProfiles);
            System.out.println("Propiedades:");
            properties.forEach((k, v) -> System.out.printf("  %-25s = %s%n", k, v));
            System.out.println("Dependencias (" + dependencies.size() + "):");
            dependencies.forEach(d -> System.out.println("  " + d));
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Pom pom = new Pom("my-service",
            List.of(
                new Dependency("org.springframework:spring-context:6.1.2"),
                new Dependency("org.slf4j:slf4j-api:2.0.9")
            ),
            Map.of(
                "db.url",       "jdbc:h2:mem:testdb",
                "db.username",  "sa",
                "log.level",    "DEBUG",
                "app.mode",     "default"
            )
        );

        // Perfil dev: H2 en memoria, nivel debug, dependencia H2
        pom.addProfile(new Profile("dev",
            List.of(new Dependency("com.h2database:h2:2.2.224")),
            Map.of(
                "db.url",    "jdbc:h2:mem:devdb",
                "log.level", "DEBUG",
                "app.mode",  "development"
            )
        ));

        // Perfil prod: base de datos real, nivel warn, driver PostgreSQL
        pom.addProfile(new Profile("prod",
            List.of(new Dependency("org.postgresql:postgresql:42.7.0")),
            Map.of(
                "db.url",       "jdbc:postgresql://prod-server:5432/mydb",
                "db.username",  "prod_user",
                "log.level",    "WARN",
                "app.mode",     "production"
            )
        ));

        System.out.println("=== mvn package -Pdev ===");
        pom.activate(List.of("dev")).print();

        System.out.println("=== mvn package -Pprod ===");
        pom.activate(List.of("prod")).print();

        System.out.println("=== mvn package (sin perfil) ===");
        pom.activate(List.of()).print();
    }
}
