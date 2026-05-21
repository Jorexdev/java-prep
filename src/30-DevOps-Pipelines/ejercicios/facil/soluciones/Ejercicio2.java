import java.util.*;

public class Ejercicio2 {

    static class Artifact {
        String name;
        String version;
        String checksum;

        Artifact(String name, String version) {
            this.name     = name;
            this.version  = version;
            this.checksum = computeChecksum(name, version);
        }

        // Checksum simulado: suma de char codes → hex
        static String computeChecksum(String name, String version) {
            int sum = 0;
            for (char c : (name + version).toCharArray()) sum += c;
            return Integer.toHexString(sum);
        }

        @Override
        public String toString() {
            return name + ":" + version + " (checksum=" + checksum + ")";
        }
    }

    static class BuildStage {
        Artifact run(String name, String version) {
            System.out.println("[build] Compilando " + name + ":" + version);
            Artifact artifact = new Artifact(name, version);
            System.out.println("[build] Artifact producido: " + artifact);
            return artifact;
        }
    }

    static class DeployStage {
        void run(Artifact artifact, String expectedChecksum) {
            System.out.printf("[deploy] Verificando checksum de '%s'...%n", artifact.name);
            System.out.printf("  esperado=%s  actual=%s%n",
                    expectedChecksum, artifact.checksum);

            if (!artifact.checksum.equals(expectedChecksum)) {
                System.out.println("[deploy] ERROR: checksum mismatch. ABORTANDO deploy.");
                return;
            }
            System.out.println("[deploy] Checksum OK. Desplegando...");
            System.out.println("[deploy] " + artifact + " desplegado exitosamente.");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Artifact Build & Deploy Demo ===\n");

        BuildStage  build  = new BuildStage();
        DeployStage deploy = new DeployStage();

        System.out.println("--- Caso 1: deploy exitoso ---");
        Artifact artifact1 = build.run("my-app", "2.3.1");
        deploy.run(artifact1, artifact1.checksum);

        System.out.println("\n--- Caso 2: checksum alterado (artefacto corrupto) ---");
        Artifact artifact2 = build.run("my-app", "2.3.2");
        String tamperedChecksum = "deadbeef";
        deploy.run(artifact2, tamperedChecksum);

        System.out.println("\n--- Verificación del algoritmo de checksum ---");
        String cs1 = Artifact.computeChecksum("my-app", "2.3.1");
        String cs2 = Artifact.computeChecksum("my-app", "2.3.1");
        System.out.printf("  Mismo input → mismo checksum: %s == %s → %b%n",
                cs1, cs2, cs1.equals(cs2));
    }
}
