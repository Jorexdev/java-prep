import java.util.*;

public class ExpMultistageBuilder {

    static class Artifact {
        final String path;
        final long sizeBytes;

        Artifact(String path, long sizeBytes) {
            this.path = path;
            this.sizeBytes = sizeBytes;
        }
    }

    static class BuildStage {
        private final String name;
        private final String baseImage;
        private final List<String> commands;
        private final List<Artifact> producedArtifacts = new ArrayList<>();
        private long layerSize = 0;

        BuildStage(String name, String baseImage, String... commands) {
            this.name = name;
            this.baseImage = baseImage;
            this.commands = List.of(commands);
        }

        void run() {
            System.out.printf("%n  [STAGE: %s]  base=%s%n", name, baseImage);
            for (String cmd : commands) {
                System.out.printf("    RUN %s%n", cmd);
            }
        }

        void produce(String artifactPath, long sizeBytes) {
            producedArtifacts.add(new Artifact(artifactPath, sizeBytes));
            layerSize += sizeBytes;
        }

        List<Artifact> getArtifacts() { return producedArtifacts; }
        String getName()              { return name; }
        long getTotalSize()           { return layerSize; }

        // base image size is a fixed overhead carried into final image size calculation
        long getBaseImageSize() {
            // simulated: JDK full ~320 MB, JRE slim ~80 MB
            return baseImage.contains("jdk") ? 320_000_000L : 80_000_000L;
        }
    }

    static class MultiStageBuild {
        private final List<BuildStage> stages = new ArrayList<>();
        private final Map<String, BuildStage> stagesByName = new LinkedHashMap<>();

        void addStage(BuildStage stage) {
            stages.add(stage);
            stagesByName.put(stage.getName(), stage);
        }

        // Run all stages in order; final stage copies only listed artifacts
        void build(String finalStage, String... artifactsToCopy) {
            System.out.println("─".repeat(55));
            System.out.println("  Ejecutando stages:");

            for (BuildStage stage : stages) {
                stage.run();
            }

            System.out.println("\n  Construyendo imagen final (COPY --from=builder):");
            BuildStage last = stagesByName.get(finalStage);
            long copiedSize = 0;
            Set<String> toCopy = new HashSet<>(Arrays.asList(artifactsToCopy));
            for (BuildStage src : stages) {
                if (src.getName().equals(finalStage)) continue;
                for (Artifact a : src.getArtifacts()) {
                    if (toCopy.contains(a.path)) {
                        System.out.printf("    COPY --from=%s %s → /app/%s  (%s)%n",
                                src.getName(), a.path, a.path, formatSize(a.sizeBytes));
                        copiedSize += a.sizeBytes;
                    }
                }
            }

            long builderTotal = stages.stream()
                    .filter(s -> !s.getName().equals(finalStage))
                    .mapToLong(s -> s.getBaseImageSize() + s.getTotalSize())
                    .sum();

            long runtimeTotal = last.getBaseImageSize() + copiedSize;

            System.out.printf("%n  ── Tamaños ──%n");
            System.out.printf("  stage 'builder' (descartado): %s%n", formatSize(builderTotal));
            System.out.printf("  imagen final  'runtime':      %s%n", formatSize(runtimeTotal));
            System.out.printf("  Ahorro:  %s%n", formatSize(builderTotal - runtimeTotal));
        }

        private String formatSize(long bytes) {
            if (bytes >= 1_000_000) return bytes / 1_000_000 + " MB";
            return bytes / 1_000 + " KB";
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(55));
        System.out.println("  MULTI-STAGE DOCKER BUILD — simulación");
        System.out.println("═".repeat(55));

        // Stage 1: builder — JDK completo, compila el proyecto
        BuildStage builder = new BuildStage("builder", "maven:3.9-jdk-21",
                "mvn dependency:resolve",
                "mvn package -DskipTests");
        builder.produce("target/app.jar", 18_000_000);   // jar compilado
        builder.produce("target/classes/", 45_000_000);  // clases intermedias (no se copian)

        // Stage 2: runtime — JRE slim, solo recibe el jar
        BuildStage runtime = new BuildStage("runtime", "openjdk:21-jre-slim",
                "EXPOSE 8080",
                "ENTRYPOINT [\"java\",\"-jar\",\"/app/app.jar\"]");

        MultiStageBuild build = new MultiStageBuild();
        build.addStage(builder);
        build.addStage(runtime);

        // Solo el jar viaja a la imagen final; las clases y el JDK se descartan
        build.build("runtime", "target/app.jar");

        System.out.println("\n── Conclusión ──");
        System.out.println("  Multi-stage elimina el JDK y artefactos de compilación.");
        System.out.println("  La imagen de producción solo contiene JRE + jar.");
    }
}
