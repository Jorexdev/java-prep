import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Simula profile groups, introducidos en Spring Boot 2.4.
// Un grupo expande un perfil de alto nivel en un conjunto de perfiles concretos.
// spring.profiles.group.production=cloud,monitoring,security
public class ExpProfileGroups {

    // ── Registro de perfiles ──────────────────────────────────────────────────

    static class ProfileRegistry {
        // Grupos: nombre del grupo → perfiles que lo componen
        private final Map<String, List<String>> groups = new HashMap<>();
        // Perfiles activos (después de expandir grupos)
        private final Set<String> active = new HashSet<>();

        // Registra un grupo — equivale a spring.profiles.group.<name>=<members>
        void defineGroup(String groupName, String... members) {
            groups.put(groupName, Arrays.asList(members));
        }

        // Activa un perfil, expandiéndolo si es un grupo
        void activate(String profile) {
            if (groups.containsKey(profile)) {
                // Expansión del grupo — igual a lo que hace Spring Boot al leer active profiles
                System.out.println("  [ProfileRegistry] '" + profile + "' es un grupo → expandiendo");
                List<String> members = groups.get(profile);
                active.addAll(members);
                System.out.println("  Perfiles activos: " + active);
            } else {
                active.add(profile);
                System.out.println("  [ProfileRegistry] '" + profile + "' activado directamente");
            }
        }

        boolean isActive(String profile) { return active.contains(profile); }
        Set<String> getActive()          { return active; }
    }

    // ── Beans condicionales por perfil ────────────────────────────────────────

    static class BeanSelector {
        private final ProfileRegistry profiles;

        BeanSelector(ProfileRegistry profiles) { this.profiles = profiles; }

        // @Bean @Profile("cloud")
        void cloudStorageBean() {
            System.out.println("  [CloudStorage] Conectado a S3");
        }

        // @Bean @Profile("monitoring")
        void monitoringBean() {
            System.out.println("  [Monitoring] Prometheus endpoint activo en :9090");
        }

        // @Bean @Profile("security")
        void securityBean() {
            System.out.println("  [Security] TLS + OAuth2 configurados");
        }

        // @Bean @Profile("local")
        void localDevBean() {
            System.out.println("  [LocalDev] H2 console activa en /h2-console");
        }

        void wireAll() {
            System.out.println("  Beans activos para perfiles " + profiles.getActive() + ":");
            if (profiles.isActive("cloud"))      cloudStorageBean();
            if (profiles.isActive("monitoring")) monitoringBean();
            if (profiles.isActive("security"))   securityBean();
            if (profiles.isActive("local"))      localDevBean();
        }
    }

    public static void main(String[] args) {
        // ── Definición de grupos (en application.properties):
        // spring.profiles.group.production=cloud,monitoring,security
        // spring.profiles.group.development=local,monitoring

        System.out.println("=== Definición de grupos ===");
        ProfileRegistry registry = new ProfileRegistry();
        registry.defineGroup("production",  "cloud", "monitoring", "security");
        registry.defineGroup("development", "local", "monitoring");
        System.out.println("  production  → cloud, monitoring, security");
        System.out.println("  development → local, monitoring");

        System.out.println("\n=== Activar perfil 'production' ===");
        registry.activate("production");
        new BeanSelector(registry).wireAll();

        System.out.println("\n=== Activar perfil 'development' ===");
        ProfileRegistry devRegistry = new ProfileRegistry();
        devRegistry.defineGroup("production",  "cloud", "monitoring", "security");
        devRegistry.defineGroup("development", "local", "monitoring");
        devRegistry.activate("development");
        new BeanSelector(devRegistry).wireAll();

        System.out.println("\n=== Activar perfil simple sin grupo ===");
        ProfileRegistry simpleRegistry = new ProfileRegistry();
        simpleRegistry.defineGroup("production",  "cloud", "monitoring", "security");
        simpleRegistry.activate("local");  // no es un grupo → activación directa
        new BeanSelector(simpleRegistry).wireAll();
    }
}
