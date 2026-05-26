import java.util.*;

public class ExpHealthChecks {

    static class Pod {
        private final String name;
        private int tick;         // current time step
        private boolean alive;
        private boolean ready;

        Pod(String name) {
            this.name = name;
            this.tick = 0;
            this.alive = true;
            this.ready = false;
        }

        void advanceTick() { tick++; }

        // Simulates warm-up: not ready for first 2 ticks, alive until tick 6
        boolean isAlive() {
            return alive;
        }

        boolean isReady() {
            // ready after warm-up period
            return alive && tick >= 2;
        }

        void restart() {
            tick = 0;
            alive = true;
            ready = false;
            System.out.printf("  [RESTART] Pod %s restarted (liveness failure)%n", name);
        }

        // External trigger: simulate liveness failure at a specific tick
        void simulateLivenessFailure() {
            alive = false;
        }

        String getName() { return name; }
        int getTick()    { return tick; }
    }

    static class ProbeController {
        private final List<Pod> endpoints = new ArrayList<>();   // pods currently in Service
        private final List<Pod> allPods   = new ArrayList<>();

        void register(Pod pod) {
            allPods.add(pod);
        }

        // Run one probe cycle: check liveness then readiness for all pods
        void probeCycle() {
            for (Pod pod : new ArrayList<>(allPods)) {
                pod.advanceTick();

                // Liveness probe: failure → restart pod
                if (!pod.isAlive()) {
                    System.out.printf("  [LIVENESS FAIL] %s (tick=%d) → restarting%n",
                            pod.getName(), pod.getTick());
                    endpoints.remove(pod);
                    pod.restart();
                    continue;
                }

                // Readiness probe: failure → remove from Service endpoints
                if (!pod.isReady()) {
                    if (endpoints.contains(pod)) {
                        endpoints.remove(pod);
                        System.out.printf("  [READINESS FAIL] %s (tick=%d) → removed from endpoints%n",
                                pod.getName(), pod.getTick());
                    } else {
                        System.out.printf("  [READINESS FAIL] %s (tick=%d) → still warming up%n",
                                pod.getName(), pod.getTick());
                    }
                } else {
                    // Ready: add to endpoints if not already there
                    if (!endpoints.contains(pod)) {
                        endpoints.add(pod);
                        System.out.printf("  [READINESS OK]   %s (tick=%d) → added to endpoints%n",
                                pod.getName(), pod.getTick());
                    } else {
                        System.out.printf("  [LIVENESS OK]    %s (tick=%d) → alive and ready%n",
                                pod.getName(), pod.getTick());
                    }
                }
            }
            System.out.printf("  Service endpoints: %s%n",
                    endpoints.stream().map(Pod::getName).toList());
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  KUBERNETES HEALTH CHECKS — simulación");
        System.out.println("═".repeat(60));

        Pod pod = new Pod("api-pod-1");
        ProbeController ctrl = new ProbeController();
        ctrl.register(pod);

        System.out.println("\n[Tick 1] Pod arrancando — warm-up en curso");
        System.out.println("─".repeat(60));
        ctrl.probeCycle();

        System.out.println("\n[Tick 2] Aún en warm-up (readiness falla 2 checks seguidos)");
        System.out.println("─".repeat(60));
        ctrl.probeCycle();

        System.out.println("\n[Tick 3] Warm-up completado — pod listo");
        System.out.println("─".repeat(60));
        ctrl.probeCycle();

        System.out.println("\n[Tick 4] Simulando fallo de liveness (OOM, deadlock, etc.)");
        System.out.println("─".repeat(60));
        pod.simulateLivenessFailure();
        ctrl.probeCycle();

        System.out.println("\n[Tick 5-6] Pod reiniciado — nuevo warm-up");
        System.out.println("─".repeat(60));
        ctrl.probeCycle();
        ctrl.probeCycle();

        System.out.println("\n── Conclusión ──");
        System.out.println("  Liveness  → fallo reinicia el pod (soluciona deadlocks, OOMs)");
        System.out.println("  Readiness → fallo saca el pod de los endpoints (sin tráfico durante warm-up)");
    }
}
