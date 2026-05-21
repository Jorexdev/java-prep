import java.util.*;

public class Ejercicio1 {

    enum PodState {
        PENDING, RUNNING, SUCCEEDED, FAILED, CRASHLOOPBACKOFF
    }

    static class Pod {
        String name;
        String namespace;
        String image;
        Map<String, String> labels;
        PodState state;
        int restarts;

        Pod(String name, String namespace, String image, Map<String, String> labels) {
            this.name      = name;
            this.namespace = namespace;
            this.image     = image;
            this.labels    = new LinkedHashMap<>(labels);
            this.state     = PodState.PENDING;
            this.restarts  = 0;
        }

        void transition(PodState next) {
            System.out.printf("  [%s/%s] %s → %s%n", namespace, name, state, next);
            state = next;
        }

        void start() {
            if (state == PodState.PENDING) transition(PodState.RUNNING);
        }

        void succeed() {
            if (state == PodState.RUNNING) transition(PodState.SUCCEEDED);
        }

        void fail() {
            if (state == PodState.RUNNING) {
                restarts++;
                if (restarts >= 3) {
                    transition(PodState.CRASHLOOPBACKOFF);
                } else {
                    transition(PodState.FAILED);
                }
            }
        }

        void print() {
            System.out.printf("  %-20s ns=%-12s img=%-25s state=%-20s restarts=%d%n",
                    name, namespace, image, state, restarts);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Pod Lifecycle ===\n");

        // Pod 1: ciclo normal
        Pod pod1 = new Pod("frontend-abc", "default", "nginx:latest",
                Map.of("app", "frontend"));
        System.out.println("[Pod 1: ciclo normal]");
        pod1.start();
        pod1.succeed();

        // Pod 2: falla una vez
        Pod pod2 = new Pod("worker-xyz", "jobs", "my-worker:1.0",
                Map.of("app", "worker"));
        System.out.println("\n[Pod 2: falla una vez]");
        pod2.start();
        pod2.fail();

        // Pod 3: CrashLoopBackOff tras 3 reinicios
        Pod pod3 = new Pod("broken-pod", "default", "broken-app:latest",
                Map.of("app", "broken"));
        System.out.println("\n[Pod 3: CrashLoopBackOff]");
        pod3.start();
        pod3.fail();    // restart 1 → FAILED
        pod3.start();   // vuelve a RUNNING para simular el reinicio
        pod3.fail();    // restart 2 → FAILED
        pod3.start();
        pod3.fail();    // restart 3 → CRASHLOOPBACKOFF

        // Pod 4: se queda en PENDING
        Pod pod4 = new Pod("pending-pod", "staging", "big-image:2.0",
                Map.of("app", "bigapp", "env", "staging"));
        System.out.println("\n[Pod 4: permanece en PENDING (no hay recursos)]");
        System.out.printf("  [%s/%s] estado: %s (sin nodo disponible)%n",
                pod4.namespace, pod4.name, pod4.state);

        System.out.println("\n=== Estado final de los pods ===");
        for (Pod p : List.of(pod1, pod2, pod3, pod4)) {
            p.print();
        }
    }
}
