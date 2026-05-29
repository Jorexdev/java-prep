import java.util.*;

public class Ejercicio5 {

    enum DriftType { MISSING, EXTRA, DRIFTED }

    static class DriftEntry {
        DriftType type;
        String resource;
        String desiredSpec;
        String actualSpec;

        DriftEntry(DriftType type, String resource, String desired, String actual) {
            this.type        = type;
            this.resource    = resource;
            this.desiredSpec = desired;
            this.actualSpec  = actual;
        }

        @Override public String toString() {
            return switch (type) {
                case MISSING -> String.format("  [MISSING]  %-25s desired='%s'", resource, desiredSpec);
                case EXTRA   -> String.format("  [EXTRA]    %-25s actual='%s'", resource, actualSpec);
                case DRIFTED -> String.format("  [DRIFTED]  %-25s desired='%s' actual='%s'",
                                              resource, desiredSpec, actualSpec);
            };
        }
    }

    static class GitOpsController {
        // desired = lo que está en git
        Map<String, String> desiredState;
        // actual = lo que está desplegado en el cluster
        Map<String, String> actualState;

        GitOpsController(Map<String, String> desired, Map<String, String> actual) {
            this.desiredState = new LinkedHashMap<>(desired);
            this.actualState  = new LinkedHashMap<>(actual);
        }

        List<DriftEntry> detectDrift() {
            List<DriftEntry> drift = new ArrayList<>();

            // Recursos en desired pero no en actual → MISSING
            for (Map.Entry<String, String> entry : desiredState.entrySet()) {
                if (!actualState.containsKey(entry.getKey())) {
                    drift.add(new DriftEntry(DriftType.MISSING,
                            entry.getKey(), entry.getValue(), null));
                } else if (!actualState.get(entry.getKey()).equals(entry.getValue())) {
                    drift.add(new DriftEntry(DriftType.DRIFTED,
                            entry.getKey(), entry.getValue(),
                            actualState.get(entry.getKey())));
                }
            }

            // Recursos en actual pero no en desired → EXTRA
            for (Map.Entry<String, String> entry : actualState.entrySet()) {
                if (!desiredState.containsKey(entry.getKey())) {
                    drift.add(new DriftEntry(DriftType.EXTRA,
                            entry.getKey(), null, entry.getValue()));
                }
            }

            return drift;
        }

        boolean reconcile() {
            List<DriftEntry> drift = detectDrift();
            if (drift.isEmpty()) {
                System.out.println("  No hay drift. Estado sincronizado.");
                return false;
            }

            System.out.printf("  Drift detectado (%d entradas):%n", drift.size());
            drift.forEach(System.out::println);
            System.out.println("  Reconciliando...");

            for (DriftEntry entry : drift) {
                switch (entry.type) {
                    case MISSING, DRIFTED -> {
                        actualState.put(entry.resource, entry.desiredSpec);
                        System.out.printf("    APPLY  %s → '%s'%n", entry.resource, entry.desiredSpec);
                    }
                    case EXTRA -> {
                        actualState.remove(entry.resource);
                        System.out.printf("    DELETE %s%n", entry.resource);
                    }
                }
            }
            System.out.println("  Reconciliación completa.");
            return true;
        }

        void printState(String title) {
            System.out.println("\n" + title);
            System.out.println("  Desired:");
            desiredState.forEach((k, v) -> System.out.printf("    %-30s = %s%n", k, v));
            System.out.println("  Actual:");
            actualState.forEach((k, v) -> System.out.printf("    %-30s = %s%n", k, v));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== GitOps Drift Detection y Reconciliación ===");

        // Estado inicial: git y cluster en sync
        Map<String, String> desired = new LinkedHashMap<>();
        desired.put("deployment/api",      "image=api:v1.2, replicas=3");
        desired.put("service/api",          "port=8080, type=ClusterIP");
        desired.put("configmap/app-config", "log-level=INFO");

        Map<String, String> actual = new LinkedHashMap<>(desired);

        GitOpsController controller = new GitOpsController(desired, actual);
        controller.printState("Estado inicial (en sync):");

        System.out.println("\n--- Ciclo 1: alguien modifica manualmente el cluster ---");
        // Simula drift manual: alguien cambió replicas en el cluster sin pasar por git
        controller.actualState.put("deployment/api", "image=api:v1.2, replicas=5");
        // Y eliminó el configmap manualmente
        controller.actualState.remove("configmap/app-config");
        // Y añadió un recurso extra no gestionado
        controller.actualState.put("deployment/debug-tool", "image=busybox:latest, replicas=1");

        System.out.println("  [Drift manual introducido]");
        controller.reconcile();

        System.out.println("\n--- Ciclo 2: se actualiza git con nueva imagen ---");
        controller.desiredState.put("deployment/api", "image=api:v1.3, replicas=3");
        controller.desiredState.put("deployment/worker", "image=worker:v1.0, replicas=2");
        controller.reconcile();

        System.out.println("\n--- Ciclo 3: sin cambios, estado sincronizado ---");
        controller.reconcile();

        controller.printState("\nEstado final:");
    }
}
