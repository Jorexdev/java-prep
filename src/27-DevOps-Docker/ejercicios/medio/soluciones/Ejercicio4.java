import java.util.*;
import java.util.function.Supplier;

public class Ejercicio4 {

    enum ContainerState { RUNNING, UNHEALTHY, HEALTHY }

    static class HealthCheck {
        String command;
        int intervalMs;
        int timeoutMs;
        int retries;

        HealthCheck(String command, int intervalMs, int timeoutMs, int retries) {
            this.command     = command;
            this.intervalMs  = intervalMs;
            this.timeoutMs   = timeoutMs;
            this.retries     = retries;
        }
    }

    static class Container {
        String name;
        ContainerState state;
        HealthCheck healthCheck;
        int consecutiveFails;
        int totalChecks;

        Container(String name, HealthCheck healthCheck) {
            this.name             = name;
            this.state            = ContainerState.RUNNING;
            this.healthCheck      = healthCheck;
            this.consecutiveFails = 0;
            this.totalChecks      = 0;
        }

        void runChecks(Supplier<Boolean> probeResult, int attempts) {
            System.out.printf("Container '%s' — health check: %s  (retries=%d)%n",
                    name, healthCheck.command, healthCheck.retries);
            System.out.printf("%-8s %-10s %-12s %-20s%n",
                    "Intento", "Resultado", "Consec.fails", "Estado container");
            System.out.println("-".repeat(54));

            for (int i = 1; i <= attempts && state != ContainerState.UNHEALTHY; i++) {
                totalChecks++;
                boolean ok = probeResult.get();

                if (ok) {
                    consecutiveFails = 0;
                    state = ContainerState.HEALTHY;
                } else {
                    consecutiveFails++;
                    if (consecutiveFails >= healthCheck.retries) {
                        state = ContainerState.UNHEALTHY;
                    }
                }

                System.out.printf("%-8d %-10s %-12d %-20s%n",
                        i, ok ? "OK" : "FAIL", consecutiveFails, state);
            }

            System.out.printf("%nEstado final: %s%n", state);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Docker Health Check Simulation ===\n");

        HealthCheck hc = new HealthCheck("curl -f http://localhost:8080/health", 30, 10, 3);
        Container c = new Container("app-server", hc);

        // Intentos: 1=OK, 2=FAIL, 3=FAIL, 4=FAIL (3 consecutivos → UNHEALTHY), 5 no se ejecuta
        boolean[] results = {true, false, false, false, true};
        int[] idx = {0};
        Supplier<Boolean> probe = () -> {
            boolean r = results[idx[0]];
            idx[0] = Math.min(idx[0] + 1, results.length - 1);
            return r;
        };

        c.runChecks(probe, 5);

        System.out.println("\n--- Segundo container: intermitente pero no consecutivo ---\n");
        HealthCheck hc2 = new HealthCheck("pg_isready -U postgres", 15, 5, 3);
        Container c2 = new Container("postgres-db", hc2);

        boolean[] results2 = {true, false, true, false, true};
        int[] idx2 = {0};
        Supplier<Boolean> probe2 = () -> {
            boolean r = results2[idx2[0]];
            idx2[0] = Math.min(idx2[0] + 1, results2.length - 1);
            return r;
        };

        c2.runChecks(probe2, 5);
    }
}
