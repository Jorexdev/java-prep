import java.util.*;

public class Ejercicio2 {

    static class DockerImage {
        String repo;
        String tag;
        String id;
        double sizeMB;

        DockerImage(String repo, String tag, String id, double sizeMB) {
            this.repo   = repo;
            this.tag    = tag;
            this.id     = id;
            this.sizeMB = sizeMB;
        }

        @Override
        public String toString() {
            return String.format("%-30s  id=%-8s  %.1f MB",
                    repo + ":" + tag, id, sizeMB);
        }
    }

    static class Registry {
        // repo → tag → image
        Map<String, Map<String, DockerImage>> store = new LinkedHashMap<>();
        // imágenes sin tag (dangling): id → image
        Map<String, DockerImage> dangling = new LinkedHashMap<>();

        void push(DockerImage image) {
            System.out.printf("push %s:%s (id=%s)... ", image.repo, image.tag, image.id);
            store.computeIfAbsent(image.repo, k -> new LinkedHashMap<>());
            Map<String, DockerImage> tags = store.get(image.repo);

            if (tags.containsKey(image.tag)) {
                DockerImage old = tags.get(image.tag);
                // la imagen antigua pierde su tag → dangling
                dangling.put(old.id, old);
                System.out.printf("(tag reusado, imagen anterior %s → dangling) ", old.id);
            }
            tags.put(image.tag, image);
            System.out.println("OK");
        }

        DockerImage pull(String repo, String tag) {
            Map<String, DockerImage> tags = store.get(repo);
            if (tags == null || !tags.containsKey(tag)) {
                System.out.printf("pull %s:%s → NOT FOUND%n", repo, tag);
                return null;
            }
            DockerImage img = tags.get(tag);
            System.out.printf("pull %s:%s → %s%n", repo, tag, img.id);
            return img;
        }

        List<String> listTags(String repo) {
            Map<String, DockerImage> tags = store.getOrDefault(repo, Map.of());
            return new ArrayList<>(tags.keySet());
        }

        void delete(String repo, String tag) {
            Map<String, DockerImage> tags = store.get(repo);
            if (tags != null && tags.containsKey(tag)) {
                DockerImage removed = tags.remove(tag);
                System.out.printf("delete %s:%s (id=%s) OK%n", repo, tag, removed.id);
                if (tags.isEmpty()) store.remove(repo);
            } else {
                System.out.printf("delete %s:%s → NOT FOUND%n", repo, tag);
            }
        }

        void printDangling() {
            System.out.println("\n=== Imágenes Dangling ===");
            if (dangling.isEmpty()) {
                System.out.println("  (ninguna)");
            } else {
                dangling.values().forEach(img ->
                        System.out.println("  <none>:<none>  " + img));
            }
        }

        void printAll() {
            System.out.println("\n=== Registry ===");
            store.forEach((repo, tags) ->
                    tags.forEach((tag, img) ->
                            System.out.println("  " + img)));
        }
    }

    public static void main(String[] args) {
        Registry registry = new Registry();

        System.out.println("=== Docker Image Registry Demo ===\n");

        registry.push(new DockerImage("myapp/backend", "1.0.0",  "abc123", 120.0));
        registry.push(new DockerImage("myapp/backend", "latest", "abc123", 120.0));
        registry.push(new DockerImage("myapp/frontend","1.0.0",  "def456",  80.0));
        registry.push(new DockerImage("myapp/frontend","latest", "def456",  80.0));

        // Nueva versión: el tag latest queda dangling para la anterior
        System.out.println();
        registry.push(new DockerImage("myapp/backend", "1.1.0",  "xyz789", 125.0));
        registry.push(new DockerImage("myapp/backend", "latest", "xyz789", 125.0));

        registry.printAll();

        System.out.println("\n=== Tags de myapp/backend ===");
        System.out.println(registry.listTags("myapp/backend"));

        System.out.println("\n=== Pull operations ===");
        registry.pull("myapp/backend",  "latest");
        registry.pull("myapp/frontend", "2.0.0");  // no existe

        System.out.println("\n=== Delete ===");
        registry.delete("myapp/frontend", "1.0.0");

        registry.printDangling();
    }
}
