import java.util.*;
import java.util.function.Supplier;

public class Ejercicio5 {

    enum PodStatus { PENDING, RUNNING, FAILED }

    enum DeploymentStatus { IN_PROGRESS, COMPLETED, DEGRADED }

    static class Pod {
        String name;
        String image;
        PodStatus status;

        Pod(String name, String image) {
            this.name   = name;
            this.image  = image;
            this.status = PodStatus.RUNNING;
        }

        @Override public String toString() {
            return String.format("[%-20s | %-12s | %s]", name, image, status);
        }
    }

    static class RollingDeployment {
        String name;
        int maxUnavailable;
        int maxSurge;
        List<Pod> pods;
        DeploymentStatus status = DeploymentStatus.IN_PROGRESS;

        RollingDeployment(String name, int replicas, String image,
                          int maxUnavailable, int maxSurge) {
            this.name           = name;
            this.maxUnavailable = maxUnavailable;
            this.maxSurge       = maxSurge;
            this.pods           = new ArrayList<>();
            for (int i = 0; i < replicas; i++) {
                pods.add(new Pod(name + "-" + i, image));
            }
        }

        void printPods(String title) {
            System.out.println("\n" + title);
            pods.forEach(p -> System.out.println("  " + p));
        }

        // Realiza el rolling update con zero-downtime
        // healthCheckFn: Supplier que devuelve true=healthy, false=unhealthy
        void rollingUpdate(String newImage, Supplier<Boolean> healthCheckFn) {
            int total = pods.size();
            System.out.printf("%n=== Rolling Update '%s': → %s ===%n", name, newImage);
            System.out.printf("    replicas=%d | maxUnavailable=%d | maxSurge=%d%n%n",
                    total, maxUnavailable, maxSurge);

            List<Pod> snapshot = new ArrayList<>(pods);  // para rollback

            for (int i = 0; i < total; i++) {
                Pod old = pods.get(i);
                System.out.printf("Actualizando pod %d/%d: %s%n", i + 1, total, old.name);

                // Crear nuevo pod (surge)
                Pod newPod = new Pod(old.name + "-new", newImage);
                newPod.status = PodStatus.PENDING;
                System.out.printf("  Creando   %s ... PENDING%n", newPod.name);

                // Health check con hasta 3 intentos
                int attempts = 0;
                boolean healthy = false;
                while (attempts < 3 && !healthy) {
                    attempts++;
                    healthy = healthCheckFn.get();
                    System.out.printf("  HealthCheck intento %d → %s%n",
                            attempts, healthy ? "OK" : "FAIL");
                }

                if (!healthy) {
                    newPod.status = PodStatus.FAILED;
                    System.out.printf("  Pod %s falló 3 health checks consecutivos.%n", newPod.name);
                    System.out.println("  Deployment → DEGRADED. Deteniendo rollout.");
                    status = DeploymentStatus.DEGRADED;
                    // No se eliminan pods viejos → zero-downtime mantenido
                    printPods("Estado al detectar DEGRADED:");
                    return;
                }

                newPod.status = PodStatus.RUNNING;
                System.out.printf("  %s RUNNING → eliminando %s%n", newPod.name, old.name);
                old.status = PodStatus.FAILED;
                pods.set(i, newPod);
            }

            status = DeploymentStatus.COMPLETED;
            System.out.println("\nRolling update completado con zero-downtime.");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes Rolling Update — Zero-Downtime ===");

        // Demo 1: pod-2 falla repetidamente el health check → DEGRADED
        RollingDeployment deploy = new RollingDeployment("web-app", 4, "web-app:v1", 1, 1);
        deploy.printPods("Estado inicial:");

        // health check: pod-0 OK, pod-1 OK, pod-2 siempre FAIL, pod-3 OK
        int[] podIndex = {0};
        // Para pod-2 (índice 2): 3 intentos todos FAIL
        boolean[][] checks = {
            {true},             // pod-0: 1 intento, OK
            {true},             // pod-1: 1 intento, OK
            {false, false, false}, // pod-2: 3 intentos, todos FAIL
            {true}              // pod-3: no llegará
        };
        int[] attemptIdx = new int[4];

        Supplier<Boolean> hc = () -> {
            int pi  = podIndex[0];
            int att = attemptIdx[pi]++;
            boolean result = att < checks[pi].length ? checks[pi][att] : false;
            // Avanzar al siguiente pod si hemos agotado los intentos de este (max 3 → el caller avanza)
            // El caller controla cuándo pasar al siguiente pod; aquí solo devolvemos el resultado
            return result;
        };

        // Versión más directa: Supplier que lleva su propia secuencia
        Iterator<Boolean> seq = Arrays.asList(
                true,           // pod-0 intento 1 → OK
                true,           // pod-1 intento 1 → OK
                false, false, false  // pod-2 intentos 1,2,3 → todos FAIL
        ).iterator();

        deploy.rollingUpdate("web-app:v2", () -> seq.hasNext() && seq.next());

        System.out.printf("%nEstado del deployment: %s%n", deploy.status);
        deploy.printPods("Pods activos (zero-downtime preservado):");

        // Demo 2: update exitoso
        System.out.println("\n\n=== Demo 2: update exitoso ===");
        RollingDeployment deploy2 = new RollingDeployment("api", 3, "api:v1", 1, 1);
        deploy2.printPods("Estado inicial:");
        deploy2.rollingUpdate("api:v2", () -> true);
        System.out.printf("%nEstado: %s%n", deploy2.status);
        deploy2.printPods("Estado final:");
    }
}
