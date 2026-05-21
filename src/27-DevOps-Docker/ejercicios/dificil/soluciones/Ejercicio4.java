import java.util.*;
import java.util.function.Supplier;

public class Ejercicio4 {

    enum ContainerState { RUNNING, STOPPED }

    static class Container {
        String name;
        String image;
        ContainerState state;

        Container(String name, String image) {
            this.name  = name;
            this.image = image;
            this.state = ContainerState.RUNNING;
        }

        @Override
        public String toString() {
            return String.format("[%s | image=%-20s | %s]", name, image, state);
        }
    }

    static class Service {
        String name;
        List<Container> replicas;
        String currentImage;

        Service(String name, String image, int count) {
            this.name         = name;
            this.currentImage = image;
            this.replicas     = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                replicas.add(new Container(name + "-" + i, image));
            }
        }

        void print(String title) {
            System.out.println("\n" + title);
            replicas.forEach(r -> System.out.println("  " + r));
        }

        boolean rollingUpdate(String newImage, Supplier<Boolean> healthCheck) {
            System.out.printf("%n=== Rolling Update: %s → %s ===%n", currentImage, newImage);
            List<Container> snapshot = new ArrayList<>(replicas);

            for (int i = 0; i < replicas.size(); i++) {
                Container old = replicas.get(i);
                System.out.printf("%nReplica %d/%d: reemplazando %s%n",
                        i + 1, replicas.size(), old.name);

                Container newC = new Container(old.name + "-new", newImage);
                System.out.printf("  Levantando %s... ", newC.name);
                boolean healthy = healthCheck.get();

                if (!healthy) {
                    System.out.println("FAIL (health check)");
                    System.out.println("  Iniciando ROLLBACK...");
                    rollback(snapshot);
                    return false;
                }

                System.out.println("OK (healthy)");
                old.state = ContainerState.STOPPED;
                replicas.set(i, newC);
                System.out.printf("  Eliminado %s%n", old.name);
            }

            currentImage = newImage;
            System.out.println("\nRolling update completado exitosamente.");
            return true;
        }

        void rollback(List<Container> snapshot) {
            replicas.clear();
            replicas.addAll(snapshot);
            for (Container c : replicas) c.state = ContainerState.RUNNING;
            System.out.println("  Rollback completado → imagen restaurada: " + currentImage);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Docker Rolling Update Demo ===");

        Service svc = new Service("web-app", "web-app:1.0", 3);
        svc.print("Estado inicial:");

        // Health check: replica 0 OK, replica 1 FAIL, replica 2 no se llega a actualizar
        int[] call = {0};
        boolean[] results = {true, false, true};  // la 2.ª falla
        Supplier<Boolean> hc = () -> {
            boolean r = results[Math.min(call[0], results.length - 1)];
            call[0]++;
            return r;
        };

        boolean success = svc.rollingUpdate("web-app:2.0", hc);
        System.out.printf("%nResultado del update: %s%n", success ? "EXITO" : "ROLLBACK");
        svc.print("Estado final del servicio:");

        // Segunda demo con éxito total
        System.out.println("\n\n=== Segunda demo: update exitoso ===");
        Service svc2 = new Service("api", "api:1.0", 3);
        svc2.print("Estado inicial:");

        Supplier<Boolean> alwaysOk = () -> true;
        svc2.rollingUpdate("api:2.0", alwaysOk);
        svc2.print("Estado final:");
    }
}
