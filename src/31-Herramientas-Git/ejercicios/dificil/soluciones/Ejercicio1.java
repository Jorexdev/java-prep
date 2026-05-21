import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Ejercicio1 {

    static class Blob {
        final String content;
        final int hash; // simula SHA-256 con hashCode()

        Blob(String content) {
            this.content = content;
            this.hash = content.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Blob[hash=%d, content='%s']", hash, content);
        }
    }

    static class Tree {
        final Map<String, Blob> entries; // nombre → blob
        final int hash;

        Tree(Map<String, Blob> entries) {
            this.entries = entries;
            // Hash del árbol: basado en los hashes de sus blobs
            this.hash = entries.entrySet().stream()
                    .mapToInt(e -> Objects.hash(e.getKey(), e.getValue().hash))
                    .sum();
        }

        void print(String prefix) {
            entries.forEach((name, blob) ->
                System.out.printf("  %s%s -> hash=%d%n", prefix, name, blob.hash));
            System.out.printf("  [tree hash: %d]%n", hash);
        }
    }

    static class Commit {
        final String hash;
        final String message;
        final Tree tree;
        final Commit parent;

        Commit(String hash, String message, Tree tree, Commit parent) {
            this.hash = hash;
            this.message = message;
            this.tree = tree;
            this.parent = parent;
        }
    }

    public static void main(String[] args) {
        // Contenido de archivos — mismo para ambas ramas
        Map<String, String> fileContents = Map.of(
            "Main.java",    "public class Main { }",
            "Utils.java",   "public class Utils { }",
            "README.md",    "# My Project"
        );

        // Crear blobs
        Map<String, Blob> blobs = new LinkedHashMap<>();
        fileContents.forEach((name, content) -> blobs.put(name, new Blob(content)));

        // Árbol A — rama feature-a
        Tree treeA = new Tree(blobs);

        // Árbol B — exactamente el mismo contenido, construido de forma independiente
        Map<String, Blob> blobsB = new LinkedHashMap<>();
        fileContents.forEach((name, content) -> blobsB.put(name, new Blob(content)));
        Tree treeB = new Tree(blobsB);

        // Árbol C — contenido diferente
        Map<String, Blob> blobsC = new LinkedHashMap<>(blobs);
        blobsC.put("Extra.java", new Blob("public class Extra { }"));
        Tree treeC = new Tree(blobsC);

        // Historiales completamente distintos
        Commit historialA = new Commit("aaa001", "Historia A: commit 1", treeA, null);
        Commit historialB = new Commit("bbb001", "Historia B: commit diferente", treeB,
                new Commit("bbb000", "Historia B: commit inicial", treeB, null));

        System.out.println("=== Blob hashes (mismo contenido = mismo hash) ===");
        blobs.forEach((name, blob) -> {
            Blob blobFromB = blobsB.get(name);
            System.out.printf("  %-15s hash_A=%d, hash_B=%d, iguales=%b%n",
                    name, blob.hash, blobFromB.hash, blob.hash == blobFromB.hash);
        });

        System.out.println();
        System.out.println("=== Tree de rama feature-a ===");
        treeA.print("");

        System.out.println();
        System.out.println("=== Tree de rama feature-b (mismo contenido) ===");
        treeB.print("");

        System.out.println();
        System.out.println("=== Tree C (contenido diferente) ===");
        treeC.print("");

        System.out.println();
        System.out.println("=== Conclusión ===");
        System.out.printf("treeA.hash == treeB.hash: %b  (mismo contenido -> mismo hash)%n",
                treeA.hash == treeB.hash);
        System.out.printf("treeA.hash == treeC.hash: %b  (contenido diferente -> hash diferente)%n",
                treeA.hash == treeC.hash);
        System.out.printf("commitA.hash != commitB.hash: %b (historiales distintos)%n",
                !historialA.hash.equals(historialB.hash));
        System.out.println();
        System.out.println("Git usa este principio para deduplicar objetos: si el contenido");
        System.out.println("es idéntico, el objeto solo se almacena una vez en .git/objects/");
    }
}
