import java.util.HashMap;
import java.util.Map;

public class Ejercicio5 {

    static class GradleDistribution {
        final String version;
        final byte[] binary; // simulado con bytes de relleno

        GradleDistribution(String version) {
            this.version = version;
            // Simular un binario con tamaño basado en la versión
            this.binary = ("gradle-" + version + "-bin").getBytes();
        }

        @Override
        public String toString() {
            return "gradle-" + version + "-bin (" + binary.length + " bytes)";
        }
    }

    static class RemoteDistributionService {
        private int downloadCount = 0;

        GradleDistribution download(String version, String url) {
            downloadCount++;
            System.out.printf("  [DOWNLOAD] Descargando gradle-%s desde %s...%n", version, url);
            return new GradleDistribution(version);
        }

        int downloadCount() { return downloadCount; }
    }

    static class GradleWrapper {
        private final String distributionUrl;
        private final String version;
        private final Map<String, byte[]> cache; // version -> binary
        private final RemoteDistributionService remote = new RemoteDistributionService();

        GradleWrapper(String distributionUrl, String version, Map<String, byte[]> sharedCache) {
            this.distributionUrl = distributionUrl;
            this.version = version;
            this.cache = sharedCache;
        }

        // Obtiene la distribución: del cache si existe, del remoto si no
        GradleDistribution getDistribution() {
            if (cache.containsKey(version)) {
                byte[] cached = cache.get(version);
                System.out.printf("  [CACHED   ] gradle-%s (usando caché local, %d bytes)%n",
                        version, cached.length);
                return new GradleDistribution(version);
            } else {
                GradleDistribution dist = remote.download(version, distributionUrl);
                cache.put(version, dist.binary);
                System.out.printf("  [STORED   ] gradle-%s guardado en caché%n", version);
                return dist;
            }
        }

        void execute(String command) {
            System.out.printf("Ejecutando: './gradlew %s' con gradle-%s%n", command, version);
            GradleDistribution dist = getDistribution();
            System.out.printf("  Ejecutando build con %s%n", dist);
        }
    }

    public static void main(String[] args) {
        // Cache compartida entre todos los wrappers (simula ~/.gradle/wrapper/dists)
        Map<String, byte[]> wrapperCache = new HashMap<>();

        System.out.println("=== Caché inicial vacía ===");
        System.out.println();

        // Wrapper 1: Gradle 8.5
        GradleWrapper w1 = new GradleWrapper(
            "https://services.gradle.org/distributions/gradle-8.5-bin.zip", "8.5", wrapperCache);
        w1.execute("build");
        System.out.println();

        // Wrapper 2: Gradle 8.6 (nueva versión)
        GradleWrapper w2 = new GradleWrapper(
            "https://services.gradle.org/distributions/gradle-8.6-bin.zip", "8.6", wrapperCache);
        w2.execute("test");
        System.out.println();

        // Wrapper 3: Gradle 8.5 de nuevo (ya en caché)
        GradleWrapper w3 = new GradleWrapper(
            "https://services.gradle.org/distributions/gradle-8.5-bin.zip", "8.5", wrapperCache);
        w3.execute("assemble");
        System.out.println();

        // Wrapper 4: Gradle 7.6 (versión legacy)
        GradleWrapper w4 = new GradleWrapper(
            "https://services.gradle.org/distributions/gradle-7.6-bin.zip", "7.6", wrapperCache);
        w4.execute("jar");
        System.out.println();

        // Acceso repetido a versiones ya cacheadas
        System.out.println("=== Segundo acceso a versiones ya descargadas ===");
        new GradleWrapper("...", "8.5", wrapperCache).execute("check");
        new GradleWrapper("...", "8.6", wrapperCache).execute("check");
        System.out.println();

        System.out.println("=== Resumen ===");
        System.out.println("Versiones en caché: " + wrapperCache.keySet());
        System.out.println("Total descargas (todos los wrappers comparten caché): " +
                wrapperCache.size() + " versiones distintas descargadas");
    }
}
