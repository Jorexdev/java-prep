import java.util.*;

public class Ejercicio2 {

    enum PodState { PENDING, RUNNING, TERMINATED }

    static int podCounter = 0;

    static class PodTemplate {
        String image;
        Map<String, String> labels;

        PodTemplate(String image, Map<String, String> labels) {
            this.image  = image;
            this.labels = labels;
        }
    }

    static class Pod {
        String name;
        String image;
        PodState state;

        Pod(String name, String image) {
            this.name  = name;
            this.image = image;
            this.state = PodState.RUNNING;
        }

        @Override
        public String toString() {
            return String.format("%-20s img=%-20s state=%s", name, image, state);
        }
    }

    static class ReplicaSet {
        String name;
        int desired;
        PodTemplate template;
        List<Pod> pods = new ArrayList<>();

        ReplicaSet(String name, int desired, PodTemplate template) {
            this.name     = name;
            this.desired  = desired;
            this.template = template;
        }

        void reconcile() {
            List<Pod> active = pods.stream()
                    .filter(p -> p.state != PodState.TERMINATED)
                    .collect(java.util.stream.Collectors.toList());
            int current = active.size();

            System.out.printf("Reconcile %s: desired=%d, current=%d%n",
                    name, desired, current);

            if (current < desired) {
                int toCreate = desired - current;
                System.out.printf("  Creando %d pod(s)...%n", toCreate);
                for (int i = 0; i < toCreate; i++) {
                    Pod p = new Pod(name + "-pod-" + (++podCounter), template.image);
                    pods.add(p);
                    System.out.println("    + " + p.name);
                }
            } else if (current > desired) {
                int toDelete = current - desired;
                System.out.printf("  Eliminando %d pod(s) en exceso...%n", toDelete);
                for (int i = 0; i < toDelete; i++) {
                    Pod p = active.get(active.size() - 1 - i);
                    p.state = PodState.TERMINATED;
                    System.out.println("    - " + p.name);
                }
            } else {
                System.out.println("  Sin cambios necesarios.");
            }
        }

        void printPods(String title) {
            System.out.println("\n" + title);
            pods.stream().filter(p -> p.state != PodState.TERMINATED)
                    .forEach(p -> System.out.println("  " + p));
        }

        void killPod(String podName) {
            pods.stream().filter(p -> p.name.equals(podName))
                    .findFirst()
                    .ifPresent(p -> {
                        p.state = PodState.TERMINATED;
                        System.out.println("Pod eliminado: " + podName);
                    });
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes ReplicaSet Reconciliation ===\n");

        PodTemplate tpl = new PodTemplate("web-app:1.0",
                Map.of("app", "web", "tier", "frontend"));
        ReplicaSet rs = new ReplicaSet("web-rs", 3, tpl);

        // Estado inicial
        rs.reconcile();
        rs.printPods("Pods activos:");

        // Simular muerte de un pod
        System.out.println("\n--- Simulando muerte de un pod ---");
        rs.killPod(rs.name + "-pod-1");
        rs.printPods("Pods activos (tras kill):");

        // Reconciliar
        System.out.println();
        rs.reconcile();
        rs.printPods("Pods activos (tras reconcile):");

        // Escalar a 5
        System.out.println("\n--- Escalando desired a 5 ---");
        rs.desired = 5;
        rs.reconcile();
        rs.printPods("Pods activos (desired=5):");

        // Escalar a 2
        System.out.println("\n--- Escalando desired a 2 ---");
        rs.desired = 2;
        rs.reconcile();
        rs.printPods("Pods activos (desired=2):");
    }
}
