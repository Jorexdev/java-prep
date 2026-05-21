import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio4 {

    static class Artifact {
        final String coordinates;
        final long sizeKb;

        Artifact(String coordinates, long sizeKb) {
            this.coordinates = coordinates;
            this.sizeKb = sizeKb;
        }

        @Override public String toString() {
            return String.format("%-50s (%d KB)", coordinates, sizeKb);
        }
    }

    static class LocalRepo {
        private final Map<String, Artifact> cache;

        LocalRepo(Map<String, Artifact> prePopulated) {
            this.cache = new HashMap<>(prePopulated);
        }

        boolean contains(String coordinates) { return cache.containsKey(coordinates); }

        Artifact get(String coordinates) { return cache.get(coordinates); }

        void store(Artifact artifact) {
            cache.put(artifact.coordinates, artifact);
        }

        int size() { return cache.size(); }
    }

    static class RemoteRepo {
        private final Map<String, Artifact> available;
        private int downloadCount = 0;

        RemoteRepo(Map<String, Artifact> available) {
            this.available = available;
        }

        // Simula descarga: devuelve el artifact o lanza excepción si no existe
        Artifact download(String coordinates) {
            Artifact artifact = available.get(coordinates);
            if (artifact == null) {
                throw new IllegalStateException("Artifact no encontrado en repositorio remoto: " + coordinates);
            }
            downloadCount++;
            System.out.printf("  [DOWNLOAD] %s%n", artifact);
            return artifact;
        }

        int downloadCount() { return downloadCount; }
    }

    static class DependencyResolver {
        private final LocalRepo local;
        private final RemoteRepo remote;
        private int cacheHits = 0;
        private int downloads = 0;

        DependencyResolver(LocalRepo local, RemoteRepo remote) {
            this.local = local;
            this.remote = remote;
        }

        Artifact resolve(String coordinates) {
            if (local.contains(coordinates)) {
                cacheHits++;
                Artifact cached = local.get(coordinates);
                System.out.printf("  [LOCAL]    %s%n", cached);
                return cached;
            } else {
                Artifact downloaded = remote.download(coordinates);
                local.store(downloaded); // cachear en local
                downloads++;
                return downloaded;
            }
        }

        void printSummary() {
            System.out.println();
            System.out.println("=== Resumen de resolución ===");
            System.out.printf("  Cache hits (local):  %d%n", cacheHits);
            System.out.printf("  Descargas remotas:   %d%n", downloads);
            System.out.printf("  Artifacts en local:  %d%n", local.size());
        }
    }

    public static void main(String[] args) {
        // 10 dependencias a resolver
        List<String> requested = List.of(
            "org.springframework:spring-context:6.1.2",
            "org.springframework:spring-web:6.1.2",
            "com.fasterxml.jackson:jackson-databind:2.16.0",
            "org.slf4j:slf4j-api:2.0.9",
            "ch.qos.logback:logback-classic:1.4.14",
            "org.junit.jupiter:junit-jupiter:5.10.1",
            "org.mockito:mockito-core:5.7.0",
            "org.postgresql:postgresql:42.7.0",
            "com.google.guava:guava:33.0.0",
            "org.apache.commons:commons-lang3:3.14.0"
        );

        // 6 ya están en caché local
        Map<String, Artifact> localCache = new HashMap<>();
        localCache.put("org.springframework:spring-context:6.1.2",
                new Artifact("org.springframework:spring-context:6.1.2", 1200));
        localCache.put("org.springframework:spring-web:6.1.2",
                new Artifact("org.springframework:spring-web:6.1.2", 890));
        localCache.put("com.fasterxml.jackson:jackson-databind:2.16.0",
                new Artifact("com.fasterxml.jackson:jackson-databind:2.16.0", 1450));
        localCache.put("org.slf4j:slf4j-api:2.0.9",
                new Artifact("org.slf4j:slf4j-api:2.0.9", 60));
        localCache.put("org.junit.jupiter:junit-jupiter:5.10.1",
                new Artifact("org.junit.jupiter:junit-jupiter:5.10.1", 310));
        localCache.put("com.google.guava:guava:33.0.0",
                new Artifact("com.google.guava:guava:33.0.0", 2800));

        // El remoto tiene todo
        Map<String, Artifact> remoteAvailable = new HashMap<>();
        requested.forEach(c -> remoteAvailable.put(c, new Artifact(c, 500)));

        LocalRepo  local    = new LocalRepo(localCache);
        RemoteRepo remote   = new RemoteRepo(remoteAvailable);
        DependencyResolver resolver = new DependencyResolver(local, remote);

        System.out.println("=== Resolviendo " + requested.size() + " dependencias ===");
        System.out.println("(Caché local tiene " + local.size() + " artifacts)");
        System.out.println();

        for (String dep : requested) {
            resolver.resolve(dep);
        }

        resolver.printSummary();

        System.out.println();
        System.out.println("=== Segunda ejecución (todo en caché) ===");
        System.out.println();
        DependencyResolver resolver2 = new DependencyResolver(local, remote);
        for (String dep : requested) {
            resolver2.resolve(dep);
        }
        resolver2.printSummary();
    }
}
