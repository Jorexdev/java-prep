// Ejercicio 1 — Profile groups
// "prod" expande a ["prod-db", "prod-cache", "prod-security"].
// ProfileGroupRegistry resuelve los perfiles activos incluyendo los sub-perfiles.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Ejercicio1 {

    static class ProfileGroupRegistry {
        private final Map<String, List<String>> groups = new HashMap<>();

        void registerGroup(String groupProfile, String... memberProfiles) {
            groups.put(groupProfile, List.of(memberProfiles));
        }

        // Expande los perfiles activos incluyendo los sub-perfiles de los grupos
        Set<String> expand(List<String> activeProfiles) {
            Set<String> expanded = new HashSet<>(activeProfiles);
            for (String profile : activeProfiles) {
                if (groups.containsKey(profile)) {
                    expanded.addAll(groups.get(profile));
                }
            }
            return expanded;
        }

        void printGroups() {
            System.out.println("Grupos registrados:");
            groups.forEach((g, members) ->
                System.out.println("  " + g + " → " + members));
        }
    }

    // Bean que se activa según perfil
    static class ManagedBean {
        private final String name;
        private final String requiredProfile;

        ManagedBean(String name, String requiredProfile) {
            this.name = name;
            this.requiredProfile = requiredProfile;
        }

        boolean isActive(Set<String> activeProfiles) {
            return activeProfiles.contains(requiredProfile);
        }

        @Override
        public String toString() {
            return name + " [@Profile(\"" + requiredProfile + "\")]";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 1 — Profile groups ===\n");

        ProfileGroupRegistry registry = new ProfileGroupRegistry();
        // En Spring Boot 2.4+: spring.profiles.group.prod=prod-db,prod-cache,prod-security
        registry.registerGroup("prod",    "prod-db", "prod-cache", "prod-security");
        registry.registerGroup("dev",     "dev-tools", "dev-db");
        registry.registerGroup("staging", "staging-db", "prod-cache"); // staging reutiliza prod-cache

        registry.printGroups();
        System.out.println();

        List<ManagedBean> beans = List.of(
            new ManagedBean("PostgresDB",        "prod-db"),
            new ManagedBean("RedisCache",         "prod-cache"),
            new ManagedBean("SecurityFilter",     "prod-security"),
            new ManagedBean("H2DB",               "dev-db"),
            new ManagedBean("DevTools",           "dev-tools"),
            new ManagedBean("StagingDB",          "staging-db"),
            new ManagedBean("BaseApp",            "default")
        );

        String[][] scenarios = {
            {"prod"},
            {"dev"},
            {"staging"},
            {"prod", "debug"},
        };

        for (String[] scenario : scenarios) {
            List<String> active = List.of(scenario);
            Set<String> expanded = registry.expand(active);
            System.out.println("Perfiles activos  : " + active);
            System.out.println("Perfiles expandidos: " + expanded);
            System.out.println("Beans activos:");
            beans.stream()
                 .filter(b -> b.isActive(expanded))
                 .forEach(b -> System.out.println("  ✓ " + b));
            System.out.println();
        }

        System.out.println("--- En Spring Boot (application.properties) ---");
        System.out.println("spring.profiles.group.prod=prod-db,prod-cache,prod-security");
        System.out.println("spring.profiles.active=prod   → activa también prod-db, prod-cache, prod-security");
    }
}
