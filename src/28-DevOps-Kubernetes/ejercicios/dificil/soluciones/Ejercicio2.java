import java.util.*;

public class Ejercicio2 {

    enum PodState { PENDING, RUNNING, TERMINATED }

    static class StatefulPod {
        String name;
        int ordinal;
        PodState state = PodState.PENDING;

        StatefulPod(String setName, int ordinal) {
            this.name    = setName + "-" + ordinal;
            this.ordinal = ordinal;
        }

        void log(String event) {
            System.out.printf("  [%d] %-20s %-12s → %s%n",
                    System.currentTimeMillis() % 100000, name, event, state);
        }

        void start() throws InterruptedException {
            state = PodState.PENDING;
            log("PENDING");
            Thread.sleep(50);
            state = PodState.RUNNING;
            log("RUNNING");
        }

        void terminate() throws InterruptedException {
            log("Terminando...");
            Thread.sleep(50);
            state = PodState.TERMINATED;
            log("TERMINATED");
        }
    }

    static class StatefulSet {
        String name;
        int replicas;
        List<StatefulPod> pods = new ArrayList<>();

        StatefulSet(String name, int replicas) {
            this.name     = name;
            this.replicas = replicas;
        }

        void create() throws InterruptedException {
            System.out.println("\n=== StatefulSet '" + name + "' — Creación en orden ===");
            for (int i = 0; i < replicas; i++) {
                StatefulPod pod = new StatefulPod(name, i);
                pods.add(pod);
                pod.start();
                // Esperar a que esté RUNNING antes de crear el siguiente
                if (pod.state != PodState.RUNNING) {
                    throw new IllegalStateException(pod.name + " no llegó a RUNNING");
                }
            }
            System.out.println("  Todos los pods en RUNNING.");
        }

        void delete() throws InterruptedException {
            System.out.println("\n=== StatefulSet '" + name + "' — Eliminación en orden inverso ===");
            List<StatefulPod> reversed = new ArrayList<>(pods);
            Collections.reverse(reversed);
            for (StatefulPod pod : reversed) {
                pod.terminate();
            }
            pods.clear();
            System.out.println("  Todos los pods eliminados.");
        }

        void printStatus() {
            System.out.println("\n  Estado actual de pods:");
            pods.forEach(p -> System.out.printf("    %-20s %s%n", p.name, p.state));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Kubernetes StatefulSet Ordering ===");
        System.out.printf("  %-8s %-20s %-12s %s%n", "Time", "Pod", "Event", "State");
        System.out.println("  " + "-".repeat(55));

        StatefulSet ss = new StatefulSet("postgres", 3);
        ss.create();
        ss.printStatus();
        ss.delete();
    }
}
