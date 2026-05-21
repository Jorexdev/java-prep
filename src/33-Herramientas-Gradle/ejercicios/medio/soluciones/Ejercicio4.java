import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ejercicio4 {

    static class Artifact {
        final String coordinates;
        final String source; // "included-build:X" o "remote"

        Artifact(String coordinates, String source) {
            this.coordinates = coordinates;
            this.source = source;
        }

        @Override public String toString() {
            return coordinates + "  [" + source + "]";
        }
    }

    // Un build incluido (otro proyecto Gradle en el filesystem)
    static class IncludedBuild {
        final String name;
        final String rootDir;
        // Los artifacts que este build "publica"
        private final Map<String, Artifact> publishedArtifacts = new HashMap<>();

        IncludedBuild(String name, String rootDir) {
            this.name = name;
            this.rootDir = rootDir;
        }

        void publish(String coordinates) {
            publishedArtifacts.put(key(coordinates), new Artifact(coordinates, "included-build:" + name));
        }

        Optional<Artifact> resolve(String coordinates) {
            return Optional.ofNullable(publishedArtifacts.get(key(coordinates)));
        }

        private String key(String coords) {
            String[] parts = coords.split(":");
            return parts[0] + ":" + parts[1]; // group:artifact (sin versión)
        }
    }

    static class RemoteRepository {
        private final Map<String, Artifact> artifacts = new HashMap<>();

        void add(String coordinates) {
            artifacts.put(coordinates, new Artifact(coordinates, "remote:mavenCentral"));
        }

        Optional<Artifact> resolve(String coordinates) {
            return Optional.ofNullable(artifacts.get(coordinates));
        }
    }

    static class CompositeBuild {
        private final List<IncludedBuild> includedBuilds = new ArrayList<>();
        private final RemoteRepository remote = new RemoteRepository();

        void includeBuild(IncludedBuild build) {
            includedBuilds.add(build);
            System.out.printf("[INFO] includeBuild('%s') registrado%n", build.rootDir);
        }

        void addToRemote(String coordinates) { remote.add(coordinates); }

        // Resolución: included builds primero, luego remoto
        Artifact resolve(String coordinates) {
            // 1. Buscar en included builds (en orden de registro)
            for (IncludedBuild build : includedBuilds) {
                Optional<Artifact> found = build.resolve(coordinates);
                if (found.isPresent()) {
                    System.out.printf("  [included] %s%n", found.get());
                    return found.get();
                }
            }
            // 2. Buscar en repositorio remoto
            Optional<Artifact> remote = this.remote.resolve(coordinates);
            if (remote.isPresent()) {
                System.out.printf("  [remote  ] %s%n", remote.get());
                return remote.get();
            }
            throw new IllegalStateException("Artifact no encontrado: " + coordinates);
        }
    }

    public static void main(String[] args) {
        CompositeBuild composite = new CompositeBuild();

        // Included build 1: shared-libs (proyecto local)
        IncludedBuild sharedLibs = new IncludedBuild("shared-libs", "../shared-libs");
        sharedLibs.publish("com.example:shared-core:2.0-SNAPSHOT");
        sharedLibs.publish("com.example:shared-utils:1.5-SNAPSHOT");
        composite.includeBuild(sharedLibs);

        // Included build 2: auth-module (otro proyecto local)
        IncludedBuild authModule = new IncludedBuild("auth-module", "../auth-module");
        authModule.publish("com.example:auth-client:3.0-SNAPSHOT");
        composite.includeBuild(authModule);

        // Repositorio remoto tiene el resto
        composite.addToRemote("org.springframework:spring-context:6.1.2");
        composite.addToRemote("org.slf4j:slf4j-api:2.0.9");
        composite.addToRemote("org.junit.jupiter:junit-jupiter:5.10.1");

        System.out.println();
        System.out.println("=== Resolviendo dependencias del proyecto principal ===");
        System.out.println();

        List<String> requested = List.of(
            "com.example:shared-core:2.0-SNAPSHOT",  // en included build
            "com.example:auth-client:3.0-SNAPSHOT",  // en included build
            "org.springframework:spring-context:6.1.2", // solo en remoto
            "com.example:shared-utils:1.5-SNAPSHOT", // en included build
            "org.slf4j:slf4j-api:2.0.9"             // solo en remoto
        );

        List<Artifact> resolved = new ArrayList<>();
        for (String dep : requested) {
            System.out.println("Resolviendo: " + dep);
            resolved.add(composite.resolve(dep));
        }

        System.out.println();
        System.out.println("=== Classpath resuelto ===");
        long fromIncluded = resolved.stream().filter(a -> a.source.startsWith("included")).count();
        long fromRemote   = resolved.stream().filter(a -> a.source.startsWith("remote")).count();
        System.out.printf("  Desde included builds: %d%n", fromIncluded);
        System.out.printf("  Desde remoto:          %d%n", fromRemote);
        System.out.println();
        System.out.println("Ventaja del composite build: los cambios en shared-libs y auth-module");
        System.out.println("se reflejan inmediatamente sin necesidad de publicar a Maven.");
    }
}
