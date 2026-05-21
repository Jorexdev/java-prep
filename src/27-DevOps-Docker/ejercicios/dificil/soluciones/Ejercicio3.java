import java.util.*;

public class Ejercicio3 {

    static class Layer {
        String hash;
        String description;
        double sizeMB;

        Layer(String hash, String description, double sizeMB) {
            this.hash        = hash;
            this.description = description;
            this.sizeMB      = sizeMB;
        }
    }

    static class DockerImage {
        String name;
        List<String> layerHashes;

        DockerImage(String name, List<String> layerHashes) {
            this.name        = name;
            this.layerHashes = new ArrayList<>(layerHashes);
        }
    }

    static class LayerStore {
        Map<String, Layer> layers    = new LinkedHashMap<>();
        List<DockerImage>  images    = new ArrayList<>();

        void addLayer(Layer layer) {
            layers.put(layer.hash, layer);
        }

        void addImage(DockerImage image) {
            images.add(image);
        }

        double diskUsageWithDedup() {
            // solo contar cada layer una vez
            Set<String> seen = new HashSet<>();
            double total = 0;
            for (DockerImage img : images) {
                for (String hash : img.layerHashes) {
                    if (seen.add(hash)) {
                        Layer l = layers.get(hash);
                        if (l != null) total += l.sizeMB;
                    }
                }
            }
            return total;
        }

        double diskUsageWithoutDedup() {
            // contar cada layer por cada imagen que lo usa
            double total = 0;
            for (DockerImage img : images) {
                for (String hash : img.layerHashes) {
                    Layer l = layers.get(hash);
                    if (l != null) total += l.sizeMB;
                }
            }
            return total;
        }

        void printAnalysis() {
            System.out.println("=== Layer Deduplication Analysis ===\n");

            System.out.println("Layers disponibles:");
            layers.values().forEach(l ->
                    System.out.printf("  [%-8s] %-35s %.1f MB%n",
                            l.hash, l.description, l.sizeMB));

            System.out.println("\nImágenes:");
            for (DockerImage img : images) {
                double imgSize = img.layerHashes.stream()
                        .mapToDouble(h -> layers.getOrDefault(h, new Layer("", "", 0)).sizeMB)
                        .sum();
                System.out.printf("  %-20s layers=%s  tamaño nominal=%.1f MB%n",
                        img.name, img.layerHashes, imgSize);
            }

            double withDedup    = diskUsageWithDedup();
            double withoutDedup = diskUsageWithoutDedup();
            double savings      = withoutDedup - withDedup;

            System.out.printf("%n%-35s %8.1f MB%n", "Disco sin deduplicación:",   withoutDedup);
            System.out.printf("%-35s %8.1f MB%n",   "Disco real (con dedup):",    withDedup);
            System.out.printf("%-35s %8.1f MB  (%.0f%% ahorro)%n",
                    "Espacio ahorrado:", savings, savings / withoutDedup * 100);
        }
    }

    public static void main(String[] args) {
        LayerStore store = new LayerStore();

        // Layers compartidos
        store.addLayer(new Layer("sha256:aaa", "FROM ubuntu:22.04",          72.0));
        store.addLayer(new Layer("sha256:bbb", "RUN apt-get update",         12.0));
        // Layers específicos por imagen
        store.addLayer(new Layer("sha256:ccc", "RUN install java",          280.0));
        store.addLayer(new Layer("sha256:ddd", "COPY app.jar /app/",         20.0));
        store.addLayer(new Layer("sha256:eee", "RUN install python3",        55.0));
        store.addLayer(new Layer("sha256:fff", "COPY script.py /app/",        3.0));
        store.addLayer(new Layer("sha256:ggg", "RUN install nodejs",         95.0));
        store.addLayer(new Layer("sha256:hhh", "COPY package.json server.js", 5.0));

        // Las tres imágenes comparten sha256:aaa y sha256:bbb
        store.addImage(new DockerImage("java-app:1.0",
                List.of("sha256:aaa", "sha256:bbb", "sha256:ccc", "sha256:ddd")));
        store.addImage(new DockerImage("python-app:1.0",
                List.of("sha256:aaa", "sha256:bbb", "sha256:eee", "sha256:fff")));
        store.addImage(new DockerImage("node-app:1.0",
                List.of("sha256:aaa", "sha256:bbb", "sha256:ggg", "sha256:hhh")));

        store.printAnalysis();
    }
}
