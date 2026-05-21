import java.util.*;

public class Ejercicio2 {

    static class Request {
        int id;
        boolean canaryFails; // true = falla si va al canary

        Request(int id, boolean canaryFails) {
            this.id          = id;
            this.canaryFails = canaryFails;
        }
    }

    static class CanaryRouter {
        String stableVersion;
        String canaryVersion;
        int    canaryPercent = 0;
        int    stableOk      = 0;
        int    stableErr     = 0;
        int    canaryOk      = 0;
        int    canaryErr     = 0;

        CanaryRouter(String stable, String canary) {
            this.stableVersion = stable;
            this.canaryVersion = canary;
        }

        // Devuelve true si error rate en canary > 5%
        boolean processRequests(List<Request> requests) {
            int canaryCount  = (int) Math.ceil(requests.size() * canaryPercent / 100.0);
            int stableCount  = requests.size() - canaryCount;

            System.out.printf("  Tráfico: %d%% canary (%d req), %d%% stable (%d req)%n",
                    canaryPercent, canaryCount, 100 - canaryPercent, stableCount);

            for (int i = 0; i < requests.size(); i++) {
                Request req   = requests.get(i);
                boolean isCanary = i < canaryCount;

                if (isCanary) {
                    if (req.canaryFails) canaryErr++; else canaryOk++;
                } else {
                    stableOk++;
                }
            }

            double canaryErrorRate = (canaryOk + canaryErr) == 0
                    ? 0
                    : (double) canaryErr / (canaryOk + canaryErr) * 100;

            System.out.printf("  Canary: ok=%d err=%d errorRate=%.1f%%%n",
                    canaryOk, canaryErr, canaryErrorRate);
            System.out.printf("  Stable: ok=%d err=%d%n", stableOk, stableErr);

            return canaryErrorRate > 5.0;
        }

        void rollback() {
            System.out.println("  ROLLBACK: canary eliminado, 100% tráfico → " + stableVersion);
            canaryPercent = 0;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Canary Release Demo ===\n");

        String stable = "app:v1.0";
        String canary = "app:v2.0";

        // 20 requests simuladas: las últimas 5 fallan en canary (alta tasa de error)
        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            requests.add(new Request(i + 1, i >= 15)); // las 5 últimas fallan en canary
        }

        CanaryRouter router = new CanaryRouter(stable, canary);
        int[] stages = {5, 25, 100};

        System.out.printf("Stable: %s | Canary: %s%n%n", stable, canary);

        boolean rolledBack = false;
        for (int pct : stages) {
            router.canaryPercent = pct;
            System.out.printf("--- Fase %d%% ----%n", pct);

            boolean tooManyErrors = router.processRequests(requests);

            if (tooManyErrors) {
                System.out.printf("  ERROR RATE EXCESIVA en canary al %d%%!%n", pct);
                router.rollback();
                rolledBack = true;
                break;
            }
            System.out.printf("  OK (error rate aceptable)%n");
        }

        if (!rolledBack) {
            System.out.printf("%nCanary completado: 100%% tráfico → %s%n", canary);
        } else {
            System.out.printf("%nResultado: ROLLBACK completado. Tráfico estabilizado en %s%n", stable);
        }
    }
}
