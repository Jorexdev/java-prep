import java.util.*;

public class Ejercicio3 {

    static class Artifact {
        String name;
        String version;

        Artifact(String name, String version) {
            this.name    = name;
            this.version = version;
        }

        @Override
        public String toString() { return name + ":" + version; }
    }

    static class Environment {
        String name;
        List<Artifact> deployed = new ArrayList<>();

        Environment(String name) { this.name = name; }

        void deploy(Artifact a) {
            deployed.add(a);
            System.out.printf("  [%s] Artifact '%s' desplegado.%n", name, a);
        }

        boolean contains(Artifact a) {
            return deployed.stream().anyMatch(d -> d.name.equals(a.name));
        }
    }

    static class PromotionPipeline {
        Environment dev;
        Environment staging;
        Environment prod;

        PromotionPipeline() {
            this.dev     = new Environment("dev");
            this.staging = new Environment("staging");
            this.prod    = new Environment("prod");
        }

        void promote(Artifact artifact, boolean testsPassed, boolean approved) {
            System.out.printf("%n=== Promoción de '%s' ===%n", artifact);

            // DEV
            dev.deploy(artifact);

            // DEV → STAGING
            System.out.printf("  Promoviendo a staging (testsPassed=%b)... ", testsPassed);
            if (!testsPassed) {
                System.out.println("BLOQUEADO. Los tests no han pasado.");
                return;
            }
            System.out.println("OK");
            staging.deploy(artifact);

            // STAGING → PROD
            System.out.printf("  Promoviendo a prod (approved=%b)... ", approved);
            if (!approved) {
                System.out.println("BLOQUEADO. Sin aprobación manual.");
                return;
            }
            System.out.println("OK");
            prod.deploy(artifact);
            System.out.printf("  '%s' en producción.%n", artifact);
        }

        void printStatus() {
            System.out.println("\n=== Estado de entornos ===");
            for (Environment env : List.of(dev, staging, prod)) {
                System.out.printf("  %-10s → %s%n",
                        env.name,
                        env.deployed.isEmpty() ? "(vacío)" : env.deployed.toString());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Environment Promotion Pipeline ===");

        PromotionPipeline pipeline = new PromotionPipeline();

        // Artifact 1: flujo completo exitoso
        pipeline.promote(new Artifact("api-service", "1.5.0"), true, true);

        // Artifact 2: tests fallaron → bloqueado en dev
        pipeline.promote(new Artifact("feature-x", "0.9.0-beta"), false, true);

        // Artifact 3: tests OK pero sin aprobación → bloqueado en staging
        pipeline.promote(new Artifact("worker", "2.0.0"), true, false);

        pipeline.printStatus();
    }
}
