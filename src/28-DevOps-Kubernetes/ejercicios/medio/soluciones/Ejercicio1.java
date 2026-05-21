import java.util.*;

public class Ejercicio1 {

    enum PodState { PENDING, RUNNING, TERMINATED }

    static int podId = 0;

    static class Pod {
        String name;
        String image;
        PodState state = PodState.PENDING;

        Pod(String baseName, String image) {
            this.name  = baseName + "-" + (++podId);
            this.image = image;
        }

        void start() {
            state = PodState.RUNNING;
            System.out.printf("    Pod %-20s RUNNING (image=%s)%n", name, image);
        }

        void terminate() {
            state = PodState.TERMINATED;
            System.out.printf("    Pod %-20s TERMINATED%n", name);
        }

        @Override
        public String toString() {
            return String.format("[%-20s img=%-15s %s]", name, image, state);
        }
    }

    static class Deployment {
        String name;
        int replicas;
        String image;
        List<Pod> pods;

        Deployment(String name, int replicas, String image) {
            this.name     = name;
            this.replicas = replicas;
            this.image    = image;
            this.pods     = new ArrayList<>();
            for (int i = 0; i < replicas; i++) {
                Pod p = new Pod(name, image);
                p.start();
                pods.add(p);
            }
        }

        void printPods(String title) {
            System.out.println("  " + title);
            pods.forEach(p -> System.out.println("    " + p));
        }

        void rollingUpdate(String newImage) {
            System.out.printf("%n=== Rolling Update %s: %s → %s ===%n",
                    name, image, newImage);
            System.out.printf("    maxSurge=1  maxUnavailable=0  replicas=%d%n%n", replicas);

            for (int i = 0; i < pods.size(); i++) {
                Pod old = pods.get(i);
                System.out.printf("  [Paso %d/%d]%n", i + 1, pods.size());

                // 1. Crear y levantar nueva replica (surge +1)
                Pod newPod = new Pod(name, newImage);
                newPod.start();

                // 2. Eliminar la vieja (vuelve a replicas original)
                old.terminate();
                pods.set(i, newPod);

                printPods("Estado actual:");
                System.out.println();
            }

            image = newImage;
            System.out.println("Rolling update completado.");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Deployment Rolling Update ===\n");
        System.out.println("Creando Deployment inicial:");

        Deployment dep = new Deployment("web-app", 3, "web-app:v1.0");
        dep.printPods("\nEstado inicial:");

        dep.rollingUpdate("web-app:v2.0");
        dep.printPods("\nEstado final:");
    }
}
