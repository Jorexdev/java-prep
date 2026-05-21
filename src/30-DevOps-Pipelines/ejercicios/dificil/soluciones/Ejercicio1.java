import java.util.*;
import java.util.function.Supplier;

public class Ejercicio1 {

    enum Env { BLUE, GREEN }

    static class Router {
        Env active;

        Router(Env initial) {
            this.active = initial;
        }

        void switchTo(Env env) {
            System.out.printf("  Router: %s → %s%n", active, env);
            this.active = env;
        }

        String handle(String request) {
            return "[" + active + "] " + request;
        }
    }

    static class Environment {
        Env name;
        String deployedVersion;
        boolean idle;

        Environment(Env name, String version) {
            this.name            = name;
            this.deployedVersion = version;
            this.idle            = false;
        }

        void deploy(String version) {
            this.deployedVersion = version;
            this.idle            = false;
            System.out.printf("  [%s] Desplegado: %s%n", name, version);
        }

        void setIdle() {
            this.idle = true;
            System.out.printf("  [%s] Marcado como idle (versión anterior: %s)%n",
                    name, deployedVersion);
        }

        @Override
        public String toString() {
            return name + " v=" + deployedVersion + (idle ? " [IDLE]" : " [ACTIVE]");
        }
    }

    static class BlueGreenDeployer {
        Router      router;
        Environment blue;
        Environment green;
        Env         previousActive;

        BlueGreenDeployer(Router router, Environment blue, Environment green) {
            this.router = router;
            this.blue   = blue;
            this.green  = green;
        }

        boolean deploy(String newVersion, Supplier<Boolean> healthCheck) {
            System.out.printf("%n=== Blue-Green Deploy → %s ===%n", newVersion);

            // 1. Desplegar a green (entorno inactivo)
            Environment target = (router.active == Env.BLUE) ? green : blue;
            target.deploy(newVersion);

            // 2. Health check
            System.out.printf("  Health check sobre %s...%n", target.name);
            boolean healthy = healthCheck.get();
            System.out.printf("  Health check: %s%n", healthy ? "PASS" : "FAIL");

            if (!healthy) {
                System.out.println("  Deploy cancelado. Router sin cambios.");
                return false;
            }

            // 3. Switch del router
            previousActive = router.active;
            Environment old = (router.active == Env.BLUE) ? blue : green;
            router.switchTo(target.name);
            old.setIdle();

            System.out.println("  Deploy completado.");
            return true;
        }

        void rollback() {
            if (previousActive == null) {
                System.out.println("  Nada que revertir.");
                return;
            }
            System.out.printf("%n=== Rollback → %s ===%n", previousActive);
            router.switchTo(previousActive);
            System.out.println("  Rollback completado.");
        }

        void printStatus() {
            System.out.println("\n  Estado:");
            System.out.println("  " + blue);
            System.out.println("  " + green);
            System.out.println("  Router activo: " + router.active);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Blue-Green Deployment Demo ===");

        Router      router = new Router(Env.BLUE);
        Environment blue   = new Environment(Env.BLUE,  "v1.0");
        Environment green  = new Environment(Env.GREEN, "(vacío)");

        BlueGreenDeployer deployer = new BlueGreenDeployer(router, blue, green);
        deployer.printStatus();

        // Demo 1: deploy exitoso
        System.out.println("\n--- Demo 1: deploy exitoso de v2.0 ---");
        deployer.deploy("v2.0", () -> true);
        deployer.printStatus();

        // Demo 2: deploy con health check fallido
        System.out.println("\n--- Demo 2: deploy de v3.0 con health check fallido ---");
        deployer.deploy("v3.0", () -> false);
        deployer.printStatus();

        // Demo 3: rollback
        System.out.println("\n--- Demo 3: rollback manual ---");
        deployer.deploy("v4.0", () -> true);
        deployer.printStatus();
        deployer.rollback();
        deployer.printStatus();
    }
}
