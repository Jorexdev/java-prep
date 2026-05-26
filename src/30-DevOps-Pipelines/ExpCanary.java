import java.util.*;

public class ExpCanary {

    static class VersionMetrics {
        private int requests;
        private int errors;

        void record(boolean isError) {
            requests++;
            if (isError) errors++;
        }

        double errorRate() {
            return requests == 0 ? 0.0 : (double) errors / requests * 100.0;
        }

        @Override public String toString() {
            return String.format("requests=%d  errors=%d  errorRate=%.1f%%", requests, errors, errorRate());
        }
    }

    static class TrafficRouter {
        private int canaryPercent;   // percentage of traffic to canary (0–100)

        TrafficRouter(int initialCanaryPercent) {
            this.canaryPercent = initialCanaryPercent;
        }

        // Returns true if request should go to canary
        boolean routeToCanary(int requestIndex) {
            return (requestIndex % 100) < canaryPercent;
        }

        void setCanaryPercent(int pct) {
            this.canaryPercent = pct;
            System.out.printf("  [TRAFFIC SPLIT] canary=%d%%  stable=%d%%%n", pct, 100 - pct);
        }

        int getCanaryPercent() { return canaryPercent; }
    }

    static class MetricsCollector {
        final VersionMetrics stable = new VersionMetrics();
        final VersionMetrics canary = new VersionMetrics();

        // Simulate a batch of 100 virtual requests; canary error rate can be tuned
        void simulateTraffic(TrafficRouter router, double canaryErrorRate) {
            for (int i = 0; i < 100; i++) {
                if (router.routeToCanary(i)) {
                    canary.record(Math.random() * 100 < canaryErrorRate);
                } else {
                    stable.record(Math.random() * 100 < 0.5);  // stable: 0.5% baseline
                }
            }
        }

        void printMetrics() {
            System.out.printf("    stable → %s%n", stable);
            System.out.printf("    canary → %s%n", canary);
        }
    }

    static class CanaryController {
        private static final double ERROR_THRESHOLD = 5.0;  // % above which rollback
        private final TrafficRouter router;
        private final MetricsCollector metrics;
        private boolean promoted = false;
        private boolean rolledBack = false;

        CanaryController(TrafficRouter router, MetricsCollector metrics) {
            this.router = router;
            this.metrics = metrics;
        }

        // Evaluate metrics and decide to promote, increment or rollback
        void evaluate() {
            double rate = metrics.canary.errorRate();
            System.out.printf("    Evaluando canary: errorRate=%.1f%%  threshold=%.1f%%%n",
                    rate, ERROR_THRESHOLD);
            if (rate > ERROR_THRESHOLD) {
                rolledBack = true;
                router.setCanaryPercent(0);
                System.out.println("    [ROLLBACK] Canary retirado del pool.");
            } else if (router.getCanaryPercent() >= 100) {
                promoted = true;
                System.out.println("    [PROMOTE] Canary promovido a stable (100% tráfico).");
            } else {
                System.out.println("    [OK] Canary saludable — siguiente incremento.");
            }
        }

        boolean isPromoted()   { return promoted; }
        boolean isRolledBack() { return rolledBack; }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  CANARY RELEASE — simulación");
        System.out.println("═".repeat(60));

        MetricsCollector metrics = new MetricsCollector();
        TrafficRouter router = new TrafficRouter(0);
        CanaryController ctrl = new CanaryController(router, metrics);

        int[] increments = {10, 25, 50, 100};
        // Canary has a low error rate → should promote after all increments
        double canaryErrorRate = 1.2;

        System.out.println("\n[Canary deployment — error rate simulado: " + canaryErrorRate + "%]");

        for (int pct : increments) {
            if (ctrl.isPromoted() || ctrl.isRolledBack()) break;
            System.out.println("\n─".repeat(31));
            System.out.printf("[Incremento] canary al %d%%%n", pct);
            router.setCanaryPercent(pct);
            metrics.simulateTraffic(router, canaryErrorRate);
            metrics.printMetrics();
            ctrl.evaluate();
        }

        // ── Segundo escenario: canary con alta tasa de errores ────
        System.out.println("\n\n" + "═".repeat(60));
        System.out.println("  CANARY con alta tasa de errores → rollback");
        System.out.println("═".repeat(60));

        MetricsCollector metrics2 = new MetricsCollector();
        TrafficRouter router2 = new TrafficRouter(0);
        CanaryController ctrl2 = new CanaryController(router2, metrics2);

        System.out.println("\n[Canary deployment — error rate simulado: 15%]");
        System.out.println("─".repeat(60));
        router2.setCanaryPercent(10);
        metrics2.simulateTraffic(router2, 15.0);
        metrics2.printMetrics();
        ctrl2.evaluate();

        System.out.println("\n── Conclusión ──");
        System.out.println("  El canary expone la nueva versión a una fracción del tráfico.");
        System.out.println("  Si las métricas superan el umbral, el rollback es automático.");
    }
}
