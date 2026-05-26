import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpReplicaSet {

    static class Pod {
        private static final AtomicInteger counter = new AtomicInteger(1);
        private final String name;
        private boolean running;

        Pod(String prefix) {
            this.name = prefix + "-" + counter.getAndIncrement();
            this.running = true;
        }

        void terminate() { this.running = false; }
        boolean isRunning() { return running; }
        String getName()    { return name; }
    }

    static class ReplicaSet {
        private final String name;
        private final String podPrefix;
        private int desiredReplicas;
        private final List<Pod> pods = new ArrayList<>();

        ReplicaSet(String name, String podPrefix, int desired) {
            this.name = name;
            this.podPrefix = podPrefix;
            this.desiredReplicas = desired;
        }

        void setDesired(int desired) {
            this.desiredReplicas = desired;
        }

        // Bring running count up or down to match desiredReplicas
        void reconcile() {
            long running = pods.stream().filter(Pod::isRunning).count();
            System.out.printf("%n[ReplicaSet %s] reconcile: desired=%d  actual=%d%n",
                    name, desiredReplicas, running);

            if (running < desiredReplicas) {
                long toCreate = desiredReplicas - running;
                for (int i = 0; i < toCreate; i++) {
                    Pod p = new Pod(podPrefix);
                    pods.add(p);
                    System.out.printf("  CREATE pod %s%n", p.getName());
                }
            } else if (running > desiredReplicas) {
                long toKill = running - desiredReplicas;
                // Terminate from the end (most recently added)
                List<Pod> runningPods = pods.stream().filter(Pod::isRunning).toList();
                for (int i = runningPods.size() - 1; i >= runningPods.size() - toKill; i--) {
                    Pod p = runningPods.get(i);
                    p.terminate();
                    System.out.printf("  TERMINATE pod %s%n", p.getName());
                }
            } else {
                System.out.println("  OK — no action needed");
            }

            printStatus();
        }

        // Simulate external kill (e.g., node failure)
        void killPod(String podName) {
            pods.stream()
                    .filter(p -> p.getName().equals(podName) && p.isRunning())
                    .findFirst()
                    .ifPresent(p -> {
                        p.terminate();
                        System.out.printf("[EXTERNAL KILL] pod %s terminated%n", p.getName());
                    });
        }

        private void printStatus() {
            List<String> running = pods.stream()
                    .filter(Pod::isRunning)
                    .map(Pod::getName)
                    .toList();
            System.out.printf("  Running pods (%d): %s%n", running.size(), running);
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  KUBERNETES REPLICASET — simulación");
        System.out.println("═".repeat(60));

        ReplicaSet rs = new ReplicaSet("web-rs", "web", 3);

        // ── Arranque inicial ──────────────────────────────────────
        System.out.println("\n[Paso 1] Arranque con desired=3");
        System.out.println("─".repeat(60));
        rs.reconcile();

        // ── Simular fallo de 2 pods ───────────────────────────────
        System.out.println("\n[Paso 2] Fallo externo de 2 pods");
        System.out.println("─".repeat(60));
        rs.killPod("web-1");
        rs.killPod("web-2");
        rs.reconcile();   // detecta 1 running, crea 2 nuevos

        // ── Escalar a 5 ──────────────────────────────────────────
        System.out.println("\n[Paso 3] Scale up a desired=5");
        System.out.println("─".repeat(60));
        rs.setDesired(5);
        rs.reconcile();

        // ── Escalar a 1 ──────────────────────────────────────────
        System.out.println("\n[Paso 4] Scale down a desired=1");
        System.out.println("─".repeat(60));
        rs.setDesired(1);
        rs.reconcile();

        System.out.println("\n── Conclusión ──");
        System.out.println("  reconcile() es el loop de control: observa estado actual,");
        System.out.println("  calcula la diferencia con el estado deseado y actúa.");
    }
}
