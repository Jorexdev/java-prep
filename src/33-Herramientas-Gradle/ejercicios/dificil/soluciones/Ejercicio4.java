import java.util.*;
import java.util.function.Function;

public class Ejercicio4 {

    static class ArtifactTransform {
        final String from;
        final String to;
        final Function<byte[], byte[]> transformer;

        ArtifactTransform(String from, String to, Function<byte[], byte[]> transformer) {
            this.from = from;
            this.to = to;
            this.transformer = transformer;
        }
    }

    static class Artifact {
        final String type;
        final byte[] content;

        Artifact(String type, byte[] content) {
            this.type = type;
            this.content = content;
        }

        @Override public String toString() {
            return "Artifact{type='" + type + "', size=" + content.length + "B}";
        }
    }

    static class TransformRegistry {
        private final List<ArtifactTransform> transforms = new ArrayList<>();

        void register(ArtifactTransform transform) {
            transforms.add(transform);
        }

        // BFS para encontrar la cadena de transforms de `from` a `to`
        Optional<List<ArtifactTransform>> findChain(String from, String to) {
            if (from.equals(to)) return Optional.of(List.of());

            Queue<List<String>> queue = new ArrayDeque<>();
            queue.add(List.of(from));
            Set<String> visited = new HashSet<>();
            visited.add(from);

            while (!queue.isEmpty()) {
                List<String> path = queue.poll();
                String current = path.get(path.size() - 1);

                for (ArtifactTransform t : transforms) {
                    if (t.from.equals(current) && !visited.contains(t.to)) {
                        List<String> newPath = new ArrayList<>(path);
                        newPath.add(t.to);

                        if (t.to.equals(to)) {
                            // Reconstruir la cadena de transforms
                            List<ArtifactTransform> chain = new ArrayList<>();
                            for (int i = 0; i < newPath.size() - 1; i++) {
                                final String s = newPath.get(i);
                                final String e = newPath.get(i + 1);
                                transforms.stream()
                                    .filter(tr -> tr.from.equals(s) && tr.to.equals(e))
                                    .findFirst().ifPresent(chain::add);
                            }
                            return Optional.of(chain);
                        }
                        visited.add(t.to);
                        queue.add(newPath);
                    }
                }
            }
            return Optional.empty();
        }

        Artifact transform(Artifact artifact, String targetType) {
            Optional<List<ArtifactTransform>> chain = findChain(artifact.type, targetType);
            if (chain.isEmpty()) throw new RuntimeException("No transform chain: " + artifact.type + " → " + targetType);

            System.out.println("  Transform chain: " + artifact.type + " → " + targetType);
            Artifact current = artifact;
            for (ArtifactTransform t : chain.get()) {
                byte[] result = t.transformer.apply(current.content);
                System.out.printf("    applying: %s → %s (%dB → %dB)%n",
                    t.from, t.to, current.content.length, result.length);
                current = new Artifact(t.to, result);
            }
            return current;
        }
    }

    public static void main(String[] args) {
        TransformRegistry registry = new TransformRegistry();

        // jar → classes: simula extracción de .class files del jar (reducción de tamaño)
        registry.register(new ArtifactTransform("jar", "classes",
            bytes -> Arrays.copyOf(bytes, bytes.length * 3 / 4)));  // 75%

        // classes → filtered-classes: filtrar solo clases públicas (reducción adicional)
        registry.register(new ArtifactTransform("classes", "filtered-classes",
            bytes -> Arrays.copyOf(bytes, bytes.length / 2)));       // 50%

        System.out.println("=== Artifact Transforms ===\n");

        // Producer genera un jar de 1000 bytes
        Artifact jar = new Artifact("jar", new byte[1000]);
        System.out.println("Producer: " + jar);

        // Consumer 1 necesita 'classes'
        System.out.println("\nConsumer 1 pide 'classes':");
        Artifact classes = registry.transform(jar, "classes");
        System.out.println("  Resultado: " + classes);

        // Consumer 2 necesita 'filtered-classes' (chain de 2 transforms)
        System.out.println("\nConsumer 2 pide 'filtered-classes' (cadena de 2 transforms):");
        Artifact filtered = registry.transform(jar, "filtered-classes");
        System.out.println("  Resultado: " + filtered);

        // Si ya tiene classes, solo aplica el último transform
        System.out.println("\nConsumer 3 ya tiene 'classes', pide 'filtered-classes':");
        Artifact filtered2 = registry.transform(classes, "filtered-classes");
        System.out.println("  Resultado: " + filtered2);
    }
}
