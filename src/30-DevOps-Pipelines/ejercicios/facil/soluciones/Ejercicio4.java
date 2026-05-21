import java.util.*;

public class Ejercicio4 {

    static class DeployRecord {
        String version;
        long timestamp;
        boolean success;

        DeployRecord(String version, long timestamp, boolean success) {
            this.version   = version;
            this.timestamp = timestamp;
            this.success   = success;
        }

        @Override
        public String toString() {
            return String.format("v%-8s  ts=%d  %s",
                    version, timestamp, success ? "SUCCESS" : "FAILED ");
        }
    }

    static class DeploymentHistory {
        Deque<DeployRecord> history = new ArrayDeque<>();

        void record(DeployRecord r) {
            history.push(r);
            System.out.printf("  Deploy registrado: %s%n", r);
        }

        DeployRecord rollback() {
            System.out.println("\nIniciando rollback...");
            // Buscar el último deploy exitoso (sin eliminar los fallidos del log)
            for (DeployRecord r : history) {
                if (r.success) {
                    System.out.printf("  → Rollback a: %s%n", r);
                    return r;
                }
            }
            System.out.println("  ERROR: no hay ningún deploy exitoso en el historial.");
            return null;
        }

        void printHistory() {
            System.out.println("\n=== Historial de deploys (más reciente primero) ===");
            int i = 1;
            for (DeployRecord r : history) {
                System.out.printf("  %d. %s%n", i++, r);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Deployment Rollback Demo ===\n");

        DeploymentHistory dh = new DeploymentHistory();

        System.out.println("Registrando historial de deploys:");
        dh.record(new DeployRecord("1.0.0", 1000L, true));
        dh.record(new DeployRecord("1.1.0", 2000L, true));
        dh.record(new DeployRecord("1.2.0", 3000L, true));
        dh.record(new DeployRecord("1.3.0", 4000L, false));  // fallo
        dh.record(new DeployRecord("1.3.1", 5000L, false));  // fallo (hotfix también falló)

        dh.printHistory();

        DeployRecord target = dh.rollback();
        if (target != null) {
            System.out.printf("  Sistema restaurado a versión %s%n", target.version);
        }

        System.out.println("\n\n--- Rollback con todos los deploys fallidos ---");
        DeploymentHistory dh2 = new DeploymentHistory();
        dh2.record(new DeployRecord("2.0.0", 100L, false));
        dh2.record(new DeployRecord("2.0.1", 200L, false));
        dh2.rollback();
    }
}
