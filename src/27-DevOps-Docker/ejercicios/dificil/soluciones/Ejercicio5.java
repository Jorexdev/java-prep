import java.util.*;

public class Ejercicio5 {

    static class Artifact {
        String name;
        int sizeMB;

        Artifact(String name, int sizeMB) {
            this.name   = name;
            this.sizeMB = sizeMB;
        }

        @Override public String toString() {
            return name + " (" + sizeMB + " MB)";
        }
    }

    static class BuildStage {
        String name;
        String baseImage;
        int baseImageSizeMB;
        List<String> commands = new ArrayList<>();
        Map<String, Artifact> artifacts = new LinkedHashMap<>();   // producidos localmente
        Map<String, Artifact> copiedArtifacts = new LinkedHashMap<>(); // copiados de otro stage

        BuildStage(String name, String baseImage, int baseImageSizeMB) {
            this.name             = name;
            this.baseImage        = baseImage;
            this.baseImageSizeMB  = baseImageSizeMB;
        }

        void addCommand(String command) {
            commands.add(command);
        }

        void produceArtifact(Artifact artifact) {
            artifacts.put(artifact.name, artifact);
        }

        void copyFrom(BuildStage sourceStage, String artifactName) {
            Artifact a = sourceStage.artifacts.get(artifactName);
            if (a == null) throw new IllegalArgumentException(
                    "El stage '" + sourceStage.name + "' no produce el artefacto: " + artifactName);
            copiedArtifacts.put(artifactName, a);
            System.out.printf("  COPY --from=%s %s → stage '%s'%n",
                    sourceStage.name, a, name);
        }

        int totalSizeMB() {
            // El stage final solo lleva la imagen base + artefactos copiados (no los locales)
            int copied = copiedArtifacts.values().stream().mapToInt(a -> a.sizeMB).sum();
            return baseImageSizeMB + copied;
        }

        int fullSizeMB() {
            // Tamaño total si se incluyen todos los artefactos locales (etapa builder)
            int local = artifacts.values().stream().mapToInt(a -> a.sizeMB).sum();
            return baseImageSizeMB + local;
        }

        void printSummary() {
            System.out.printf("%n--- Stage: %s (FROM %s) ---%n", name, baseImage);
            System.out.println("  Comandos:");
            commands.forEach(c -> System.out.println("    RUN " + c));
            if (!artifacts.isEmpty()) {
                System.out.println("  Artefactos producidos:");
                artifacts.values().forEach(a -> System.out.println("    " + a));
            }
            if (!copiedArtifacts.isEmpty()) {
                System.out.println("  Artefactos copiados:");
                copiedArtifacts.values().forEach(a -> System.out.println("    " + a));
            }
            System.out.printf("  Tamaño de esta etapa: %d MB%n", fullSizeMB());
        }
    }

    static class MultiStageBuild {
        List<BuildStage> stages = new ArrayList<>();

        void addStage(BuildStage stage) {
            stages.add(stage);
        }

        BuildStage finalStage() {
            return stages.get(stages.size() - 1);
        }

        void run() {
            System.out.println("=== Multi-Stage Build ===");
            stages.forEach(BuildStage::printSummary);

            BuildStage last = finalStage();
            int builderSize = stages.get(0).fullSizeMB();
            int finalSize   = last.totalSizeMB();
            int saved       = builderSize - finalSize;
            double pct      = (saved * 100.0) / builderSize;

            System.out.println("\n=== Resultado ===");
            System.out.printf("  Etapa builder  : %4d MB%n", builderSize);
            System.out.printf("  Imagen final   : %4d MB  (solo artefactos copiados)%n", finalSize);
            System.out.printf("  Reducción      : %4d MB  (%.1f%% más pequeña)%n", saved, pct);
        }
    }

    public static void main(String[] args) {
        // Etapa builder: compila el proyecto
        BuildStage builder = new BuildStage("builder", "eclipse-temurin:21-jdk", 450);
        builder.addCommand("mvn dependency:resolve");
        builder.addCommand("mvn package -DskipTests");
        builder.produceArtifact(new Artifact("app.jar", 35));
        builder.produceArtifact(new Artifact("test-classes/", 12));
        builder.produceArtifact(new Artifact(".m2/repository/", 180));

        // Etapa runner: imagen ligera con solo el jar
        BuildStage runner = new BuildStage("runner", "eclipse-temurin:21-jre-slim", 85);
        runner.addCommand("adduser --system appuser");

        System.out.println("Copiando artefactos del builder al runner:");
        runner.copyFrom(builder, "app.jar");

        runner.addCommand("java -jar app.jar");

        MultiStageBuild build = new MultiStageBuild();
        build.addStage(builder);
        build.addStage(runner);
        build.run();
    }
}
