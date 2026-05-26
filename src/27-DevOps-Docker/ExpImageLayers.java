import java.util.*;

public class ExpImageLayers {

    static class ImageLayer {
        private final String id;
        private final String command;
        private final long sizeBytes;
        private boolean cached;

        ImageLayer(String id, String command, long sizeBytes) {
            this.id = id;
            this.command = command;
            this.sizeBytes = sizeBytes;
            this.cached = false;
        }

        void setCached(boolean cached) { this.cached = cached; }
        String getId()       { return id; }
        String getCommand()  { return command; }
        long getSizeBytes()  { return sizeBytes; }
        boolean isCached()   { return cached; }
    }

    // Tracks layers by id so the second build can detect unchanged layers
    static class ImageBuilder {
        private final List<ImageLayer> layers = new ArrayList<>();
        private final Map<String, ImageLayer> previousLayers; // id → layer from last build

        ImageBuilder(Map<String, ImageLayer> previousLayers) {
            this.previousLayers = previousLayers;
        }

        // Cache hit only if layer existed before AND no earlier layer was a miss
        void addLayer(String id, String command, long sizeBytes, boolean cacheInvalidated) {
            ImageLayer layer = new ImageLayer(id, command, sizeBytes);
            boolean hit = !cacheInvalidated && previousLayers.containsKey(id);
            layer.setCached(hit);
            layers.add(layer);
            String status = hit ? "CACHE HIT  (skip)" : "EXECUTING ";
            System.out.printf("  [%s] %-12s %s%n", status, formatSize(sizeBytes), command);
        }

        Map<String, ImageLayer> getLayerMap() {
            Map<String, ImageLayer> map = new LinkedHashMap<>();
            for (ImageLayer l : layers) map.put(l.getId(), l);
            return map;
        }

        long totalSize() {
            // Only executed layers add size; cached layers reuse existing data
            return layers.stream()
                    .filter(l -> !l.isCached())
                    .mapToLong(ImageLayer::getSizeBytes)
                    .sum();
        }

        long fullImageSize() {
            return layers.stream().mapToLong(ImageLayer::getSizeBytes).sum();
        }

        private String formatSize(long bytes) {
            if (bytes >= 1_000_000) return bytes / 1_000_000 + " MB";
            return bytes / 1_000 + " KB";
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(55));
        System.out.println("  DOCKER IMAGE LAYER CACHING — simulación");
        System.out.println("═".repeat(55));

        // ── Build 1: no cache existe aún ──────────────────────────
        System.out.println("\n[Build 1] Imagen: myapp:v1  (sin caché previa)");
        System.out.println("─".repeat(55));

        ImageBuilder build1 = new ImageBuilder(Collections.emptyMap());
        build1.addLayer("L1", "FROM openjdk:21-slim",       80_000_000, false);
        build1.addLayer("L2", "RUN apt-get install curl",   12_000_000, false);
        build1.addLayer("L3", "COPY pom.xml /app/",            150_000, false);
        build1.addLayer("L4", "RUN mvn dependency:resolve", 45_000_000, false);
        build1.addLayer("L5", "COPY src/ /app/src/",           500_000, false);
        build1.addLayer("L6", "RUN mvn package -q",          3_000_000, false);

        Map<String, ImageLayer> afterBuild1 = build1.getLayerMap();
        System.out.printf("%nImagen total: %d MB  |  ejecutado: %d MB%n",
                build1.fullImageSize() / 1_000_000,
                build1.totalSize() / 1_000_000);

        // ── Build 2: solo cambió el código fuente (L5 y arriba invalidan) ──
        System.out.println("\n[Build 2] Imagen: myapp:v2  (solo cambió src/)");
        System.out.println("─".repeat(55));

        // L1–L4 are identical; L5 changed (new source) → invalidates L5 and L6
        ImageBuilder build2 = new ImageBuilder(afterBuild1);
        boolean invalidated = false;
        build2.addLayer("L1", "FROM openjdk:21-slim",       80_000_000, invalidated);
        build2.addLayer("L2", "RUN apt-get install curl",   12_000_000, invalidated);
        build2.addLayer("L3", "COPY pom.xml /app/",            150_000, invalidated);
        build2.addLayer("L4", "RUN mvn dependency:resolve", 45_000_000, invalidated);
        invalidated = true; // src/ changed → cache invalidated from here
        build2.addLayer("L5", "COPY src/ /app/src/",           500_000, invalidated);
        build2.addLayer("L6", "RUN mvn package -q",          3_000_000, invalidated);

        System.out.printf("%nImagen total: %d MB  |  ejecutado esta vez: %d MB%n",
                build2.fullImageSize() / 1_000_000,
                build2.totalSize() / 1_000_000);

        System.out.println("\n── Conclusión ──");
        System.out.println("  Colocar COPY src/ después de RUN mvn dependency:resolve");
        System.out.println("  maximiza cache hits: dependencias no se re-descargan si solo cambia el código.");
    }
}
