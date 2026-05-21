import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Ejercicio3 {

    static class FeatureManager {
        private final Map<String, Set<String>> featureProfiles = new HashMap<>();
        private String activeProfile;

        FeatureManager(String activeProfile) {
            this.activeProfile = activeProfile;
        }

        void defineFeature(String feature, String... requiredProfiles) {
            featureProfiles.put(feature, Set.of(requiredProfiles));
        }

        boolean isEnabled(String feature) {
            Set<String> required = featureProfiles.get(feature);
            if (required == null) return false;
            // Habilitada si el perfil activo está en los perfiles requeridos
            return required.contains(activeProfile);
        }

        void setActiveProfile(String profile) {
            this.activeProfile = profile;
        }

        void printStatus() {
            System.out.println("Perfil activo: " + activeProfile);
            featureProfiles.forEach((feature, profiles) ->
                System.out.printf("  %-25s [%s] → %s%n",
                    feature, String.join(",", profiles),
                    isEnabled(feature) ? "ENABLED" : "disabled"));
        }
    }

    public static void main(String[] args) {
        FeatureManager fm = new FeatureManager("dev");

        fm.defineFeature("nuevo-checkout",       "prod", "staging");
        fm.defineFeature("modo-debug",           "dev", "test");
        fm.defineFeature("cache-redis",          "prod", "staging", "perf");
        fm.defineFeature("email-real",           "prod");
        fm.defineFeature("metricas-detalladas",  "dev", "staging", "perf");

        for (String perfil : new String[]{"dev", "staging", "prod", "test"}) {
            fm.setActiveProfile(perfil);
            System.out.println("\n=== " + perfil.toUpperCase() + " ===");
            fm.printStatus();
        }
    }
}
