import java.util.*;

public class Ejercicio3 {

    static class HPA {
        int minReplicas;
        int maxReplicas;
        int targetCPUPercent;
        int currentReplicas;

        HPA(int minReplicas, int maxReplicas, int targetCPUPercent) {
            this.minReplicas     = minReplicas;
            this.maxReplicas     = maxReplicas;
            this.targetCPUPercent= targetCPUPercent;
            this.currentReplicas = minReplicas;
        }

        void evaluate(int cpuPercent, int cycle) {
            int prev = currentReplicas;
            String action;

            if (cpuPercent > targetCPUPercent && currentReplicas < maxReplicas) {
                currentReplicas++;
                action = "SCALE UP  ↑";
            } else if (cpuPercent < 30 && currentReplicas > minReplicas) {
                currentReplicas--;
                action = "SCALE DOWN ↓";
            } else {
                action = "no change  ";
            }

            System.out.printf("  Cycle %2d | CPU=%3d%% | replicas: %d → %d | %s%n",
                    cycle, cpuPercent, prev, currentReplicas, action);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes HPA Simulation ===\n");

        HPA hpa = new HPA(1, 5, 70);
        System.out.printf("HPA: min=%d  max=%d  targetCPU=%d%%%n%n",
                hpa.minReplicas, hpa.maxReplicas, hpa.targetCPUPercent);
        System.out.printf("  %-8s %-10s %-20s %s%n",
                "Cycle", "CPU %", "Replicas", "Action");
        System.out.println("  " + "-".repeat(55));

        // Métricas simuladas: bajo → sube → sube más → baja gradualmente
        int[] metrics = {25, 30, 75, 85, 90, 80, 72, 65, 28, 20};

        for (int i = 0; i < metrics.length; i++) {
            hpa.evaluate(metrics[i], i + 1);
        }

        System.out.printf("%nReplicas finales: %d%n", hpa.currentReplicas);
    }
}
