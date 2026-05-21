// Ejercicio 4 — Multiple active profiles
// Lista de perfiles activos. FeatureFlags se activa si alguno de sus perfiles requeridos está activo.

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Ejercicio4 {

    // Representa un bean/feature que requiere ciertos perfiles para activarse
    static class FeatureFlags {
        private final String name;
        private final Set<String> requiredProfiles; // activo si ALGUNO de estos está activo
        private final boolean requireAll;           // true = requiere TODOS; false = requiere ALGUNO

        FeatureFlags(String name, boolean requireAll, String... profiles) {
            this.name = name;
            this.requiredProfiles = Set.of(profiles);
            this.requireAll = requireAll;
        }

        boolean isActive(List<String> activeProfiles) {
            if (requireAll) {
                return activeProfiles.containsAll(requiredProfiles);
            } else {
                return requiredProfiles.stream().anyMatch(activeProfiles::contains);
            }
        }

        @Override
        public String toString() {
            String mode = requireAll ? "ALL" : "ANY";
            return name + " [requiere " + mode + " de " + requiredProfiles + "]";
        }
    }

    static void checkFeatures(List<String> activeProfiles, List<FeatureFlags> features) {
        System.out.println("Perfiles activos: " + activeProfiles);
        System.out.println("-".repeat(55));
        for (FeatureFlags f : features) {
            String status = f.isActive(activeProfiles) ? "ACTIVO  ✓" : "inactivo ✗";
            System.out.printf("  %-35s → %s%n", f.toString(), status);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 4 — Multiple active profiles ===\n");

        // Definir feature flags con sus perfiles requeridos
        List<FeatureFlags> features = List.of(
            new FeatureFlags("DevTools",       false, "dev"),
            new FeatureFlags("DebugPanel",     false, "debug"),
            new FeatureFlags("DevDebugMode",   true,  "dev", "debug"),   // requiere AMBOS
            new FeatureFlags("ProdMonitoring", false, "prod"),
            new FeatureFlags("AnyEnvFeature",  false, "dev", "prod", "test"),
            new FeatureFlags("ProdOrStaging",  false, "prod", "staging")
        );

        System.out.println("=== Combo 1: [dev, debug] ===");
        checkFeatures(List.of("dev", "debug"), features);

        System.out.println("=== Combo 2: [dev] ===");
        checkFeatures(List.of("dev"), features);

        System.out.println("=== Combo 3: [prod] ===");
        checkFeatures(List.of("prod"), features);

        System.out.println("=== Combo 4: [staging] ===");
        checkFeatures(List.of("staging"), features);

        System.out.println("=== Combo 5: [dev, debug, prod] ===");
        checkFeatures(List.of("dev", "debug", "prod"), features);

        System.out.println("--- Nota Spring ---");
        System.out.println("@Profile({\"dev\", \"debug\"}) equivale a ANY (al menos uno).");
        System.out.println("Para AND, usar expresiones: @Profile(\"dev & debug\") [Spring 5.1+]");
    }
}
