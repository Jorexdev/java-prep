import java.util.*;
import java.util.function.Supplier;

public class Ejercicio2 {

    enum PodState { RUNNING, RESTARTING }

    static class LivenessProbe {
        int failureThreshold;
        int periodMs;

        LivenessProbe(int failureThreshold, int periodMs) {
            this.failureThreshold = failureThreshold;
            this.periodMs         = periodMs;
        }
    }

    static class Pod {
        String name;
        PodState state = PodState.RUNNING;
        int consecutiveFails = 0;
        int restarts         = 0;
        LivenessProbe probe;

        Pod(String name, LivenessProbe probe) {
            this.name  = name;
            this.probe = probe;
        }

        void runProbe(boolean healthy, int cycle) {
            if (!healthy) {
                consecutiveFails++;
            } else {
                consecutiveFails = 0;
            }

            String status;
            if (consecutiveFails >= probe.failureThreshold) {
                state = PodState.RESTARTING;
                restarts++;
                consecutiveFails = 0;
                status = "RESTART #" + restarts;
            } else {
                state = PodState.RUNNING;
                status = healthy ? "OK" : "FAIL (" + consecutiveFails + "/" + probe.failureThreshold + ")";
            }

            System.out.printf("  Cycle %2d | probe=%-5s | consec_fails=%-2d | state=%-12s | restarts=%d  %s%n",
                    cycle, healthy ? "pass" : "FAIL", consecutiveFails, state, restarts, status);

            if (state == PodState.RESTARTING) {
                state = PodState.RUNNING; // el pod vuelve a RUNNING tras el reinicio
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Liveness Probe ===\n");

        LivenessProbe probe = new LivenessProbe(3, 10);
        Pod pod = new Pod("app-pod", probe);

        System.out.printf("Pod '%s'  failureThreshold=%d%n%n", pod.name, probe.failureThreshold);
        System.out.printf("  %-8s %-10s %-16s %-14s %-12s%n",
                "Cycle", "probe", "consec_fails", "state", "restarts");
        System.out.println("  " + "-".repeat(65));

        // Secuencia de resultados: false = falla
        boolean[] results = {true, true, false, false, false, true, false, false, false, true};

        for (int i = 0; i < results.length; i++) {
            pod.runProbe(results[i], i + 1);
        }

        System.out.printf("%nResumen: %d reinicios totales en %d ciclos.%n",
                pod.restarts, results.length);
    }
}
