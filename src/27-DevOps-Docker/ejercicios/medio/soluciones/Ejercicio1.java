import java.util.*;

public class Ejercicio1 {

    static class Artifact {
        String name;
        double sizeMB;

        Artifact(String name, double sizeMB) {
            this.name   = name;
            this.sizeMB = sizeMB;
        }
    }

    static class BuildStage {
        String name;
        String baseImage;
        List<String> commands;
        List<Artifact> producedArtifacts;
        List<Artifact> copiedArtifacts = new ArrayList<>();
        double baseSizeMB;

        BuildStage(String name, String baseImage, double baseSizeMB, List<String> commands) {
            this.name               = name;
            this.baseImage          = baseImage;
            this.baseSizeMB         = baseSizeMB;
            this.commands           = new ArrayList<>(commands);
            this.producedArtifacts  = new ArrayList<>();
        }

        void produce(Artifact artifact) {
            producedArtifacts.add(artifact);
            System.out.printf("  [%s] Producido: %s (%.1f MB)%n",
                    name, artifact.name, artifact.sizeMB);
        }

        void copyFrom(BuildStage source, String artifactName) {
            source.producedArtifacts.stream()
                    .filter(a -> a.name.equals(artifactName))
                    .findFirst()
                    .ifPresentOrElse(
                        a -> {
                            copiedArtifacts.add(a);
                            System.out.printf("  [%s] COPY --from=%s %s (%.1f MB)%n",
                                    name, source.name, a.name, a.sizeMB);
                        },
                        () -> System.out.printf("  [%s] ERROR: %s no existe en stage %s%n",
                                name, artifactName, source.name)
                    );
        }

        double totalSize() {
            double artifactSize = copiedArtifacts.stream().mapToDouble(a -> a.sizeMB).sum()
                    + producedArtifacts.stream().mapToDouble(a -> a.sizeMB).sum();
            return baseSizeMB + artifactSize;
        }

        void print() {
            System.out.printf("Stage '%-10s'  base=%-28s comandos=%d  tamaño=%.1f MB%n",
                    name, baseImage, commands.size(), totalSize());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Multi-Stage Build Simulation ===\n");

        // Stage 1: builder — JDK completo + compilación
        BuildStage builder = new BuildStage("builder", "eclipse-temurin:21-jdk", 280.0,
                List.of("COPY pom.xml .", "COPY src ./src", "RUN mvn package -DskipTests"));

        System.out.println("[Stage: builder]");
        for (String cmd : builder.commands) {
            System.out.println("  RUN " + cmd);
        }
        builder.produce(new Artifact("app.jar",   45.0));
        builder.produce(new Artifact("tests.jar",  8.0));
        builder.produce(new Artifact("sources.jar", 4.0));

        System.out.printf("  Tamaño stage builder: %.1f MB%n%n", builder.totalSize());

        // Stage 2: runtime — solo JRE + el jar
        BuildStage runtime = new BuildStage("runtime", "eclipse-temurin:21-jre-alpine", 60.0,
                List.of("COPY --from=builder app.jar .", "EXPOSE 8080", "CMD java -jar app.jar"));

        System.out.println("[Stage: runtime (final)]");
        runtime.copyFrom(builder, "app.jar");
        System.out.printf("  Tamaño stage runtime: %.1f MB%n%n", runtime.totalSize());

        // Resumen
        System.out.println("=== Resumen de la build ===");
        builder.print();
        runtime.print();

        double reduction = builder.totalSize() - runtime.totalSize();
        double pct       = reduction / builder.totalSize() * 100;
        System.out.printf("%nReducción de tamaño: %.1f MB → %.1f MB  (ahorro: %.1f MB, %.0f%%)%n",
                builder.totalSize(), runtime.totalSize(), reduction, pct);
    }
}
