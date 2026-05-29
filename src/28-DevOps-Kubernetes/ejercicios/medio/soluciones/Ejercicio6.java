import java.util.*;

public class Ejercicio6 {

    static class HPA {
        int minReplicas;
        int maxReplicas;
        int targetCPUPercent;
        long scaleUpCooldownMs;
        long scaleDownCooldownMs;

        int currentReplicas;
        long lastScaleUpTime   = -1;
        long lastScaleDownTime = -1;

        HPA(int minReplicas, int maxReplicas, int targetCPUPercent,
            long scaleUpCooldownMs, long scaleDownCooldownMs) {
            this.minReplicas          = minReplicas;
            this.maxReplicas          = maxReplicas;
            this.targetCPUPercent     = targetCPUPercent;
            this.scaleUpCooldownMs    = scaleUpCooldownMs;
            this.scaleDownCooldownMs  = scaleDownCooldownMs;
            this.currentReplicas      = minReplicas;
        }

        // Evalúa la métrica y escala si procede; now = tiempo simulado en ms
        void evaluate(int cpuPercent, long now, int cycle) {
            System.out.printf("Ciclo %2d | CPU=%3d%% | replicas=%d", cycle, cpuPercent, currentReplicas);

            if (cpuPercent > targetCPUPercent) {
                // Necesita scale-up
                if (currentReplicas >= maxReplicas) {
                    System.out.println(" → ya en maxReplicas, sin acción");
                    return;
                }
                if (lastScaleUpTime >= 0 && (now - lastScaleUpTime) < scaleUpCooldownMs) {
                    long remaining = scaleUpCooldownMs - (now - lastScaleUpTime);
                    System.out.printf(" → SCALE-UP bloqueado por cooldown (%ds restantes)%n",
                            remaining / 1000);
                    return;
                }
                currentReplicas++;
                lastScaleUpTime = now;
                System.out.printf(" → SCALE-UP → %d replicas%n", currentReplicas);

            } else if (cpuPercent < 30) {
                // Necesita scale-down
                if (currentReplicas <= minReplicas) {
                    System.out.println(" → ya en minReplicas, sin acción");
                    return;
                }
                if (lastScaleDownTime >= 0 && (now - lastScaleDownTime) < scaleDownCooldownMs) {
                    long remaining = scaleDownCooldownMs - (now - lastScaleDownTime);
                    System.out.printf(" → SCALE-DOWN bloqueado por cooldown (%ds restantes)%n",
                            remaining / 1000);
                    return;
                }
                currentReplicas--;
                lastScaleDownTime = now;
                System.out.printf(" → SCALE-DOWN → %d replicas%n", currentReplicas);

            } else {
                System.out.println(" → CPU en rango, sin acción");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== HPA con Cooldown ===");
        System.out.printf("  minReplicas=1, maxReplicas=5, targetCPU=70%%, " +
                "scaleUpCooldown=30s, scaleDownCooldown=60s%n%n");

        // CPU measurements por ciclo (cada ciclo = 15s simulados)
        int[] cpuMetrics = { 40, 80, 85, 90, 75, 25, 20, 15, 20, 78, 88, 30 };
        long intervalMs  = 15_000;  // 15 segundos por ciclo (simulado)

        HPA hpa = new HPA(1, 5, 70, 30_000, 60_000);

        // scaleUpCooldown  = 30s → 2 ciclos de 15s
        // scaleDownCooldown = 60s → 4 ciclos de 15s

        for (int i = 0; i < cpuMetrics.length; i++) {
            long now = (long) i * intervalMs;
            hpa.evaluate(cpuMetrics[i], now, i + 1);
        }

        System.out.println("\n=== Estado final ===");
        System.out.printf("  Replicas activas: %d%n", hpa.currentReplicas);
    }
}
