// Ejercicio 5 — @Profile negación simulada
// ProfileCondition(String profile, boolean negate): activo cuando perfil coincide (o no coincide).

import java.util.List;

public class Ejercicio5 {

    // Simula @Profile("!prod") — activo cuando el perfil NO es "prod"
    static class ProfileCondition {
        private final String targetProfile;
        private final boolean negate;

        ProfileCondition(String targetProfile, boolean negate) {
            this.targetProfile = targetProfile;
            this.negate = negate;
        }

        boolean matches(List<String> activeProfiles) {
            boolean profileIsActive = activeProfiles.contains(targetProfile);
            return negate ? !profileIsActive : profileIsActive;
        }

        String describe() {
            return negate
                ? "@Profile(\"!" + targetProfile + "\")"
                : "@Profile(\"" + targetProfile + "\")";
        }
    }

    // Beans que usan la condición
    static class DevOnlyService {
        static final ProfileCondition CONDITION = new ProfileCondition("prod", true);
        // Activo cuando perfil NO es prod → equivalente a @Profile("!prod")

        void run() {
            System.out.println("[DevOnlyService] ejecutando — solo disponible fuera de prod");
        }
    }

    static class ProdOnlyService {
        static final ProfileCondition CONDITION = new ProfileCondition("prod", false);
        // Activo cuando perfil ES prod → equivalente a @Profile("prod")

        void run() {
            System.out.println("[ProdOnlyService] ejecutando — solo disponible en prod");
        }
    }

    static class DevDebugService {
        static final ProfileCondition CONDITION = new ProfileCondition("debug", false);

        void run() {
            System.out.println("[DevDebugService] ejecutando — solo disponible con perfil debug");
        }
    }

    static void demo(List<String> activeProfiles) {
        System.out.println("Perfiles activos: " + activeProfiles);

        // Lista de (condición, nombre del bean, Runnable)
        Object[][] beans = {
            { DevOnlyService.CONDITION,   "DevOnlyService",   (Runnable) new DevOnlyService()::run },
            { ProdOnlyService.CONDITION,  "ProdOnlyService",  (Runnable) new ProdOnlyService()::run },
            { DevDebugService.CONDITION,  "DevDebugService",  (Runnable) new DevDebugService()::run },
        };

        for (Object[] entry : beans) {
            ProfileCondition cond = (ProfileCondition) entry[0];
            String beanName = (String) entry[1];
            Runnable action = (Runnable) entry[2];
            boolean active = cond.matches(activeProfiles);
            System.out.printf("  %-35s %-20s → %s%n",
                cond.describe(), beanName, active ? "ACTIVO" : "inactivo");
            if (active) action.run();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 5 — @Profile negación simulada ===\n");

        demo(List.of("dev"));
        demo(List.of("prod"));
        demo(List.of("dev", "debug"));
        demo(List.of("prod", "debug"));

        System.out.println("--- Equivalencia en Spring ---");
        System.out.println("@Profile(\"!prod\")  → activo en todos los perfiles excepto prod");
        System.out.println("@Profile(\"prod\")   → activo solo cuando prod está activo");
        System.out.println("Desde Spring 5.1: @Profile(\"dev & !prod\") para expresiones compuestas");
    }
}
