import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ExpGitInternals {

    // -----------------------------------------------------------------------
    // SHA-1 helper (Git uses SHA-1 for object addressing)
    // -----------------------------------------------------------------------
    static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.substring(0, 7); // short hash (7 chars like git log --oneline)
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // Git Object Types
    // -----------------------------------------------------------------------
    interface GitObject { String hash(); String type(); }

    // Blob: raw file content — hash = sha1("blob " + content)
    static class GitBlob implements GitObject {
        final String content;
        private final String hash;

        GitBlob(String content) {
            this.content = content;
            this.hash    = sha1("blob " + content);
        }

        @Override public String hash() { return hash; }
        @Override public String type() { return "blob"; }
        @Override public String toString() {
            return String.format("blob  %s  \"%s\"", hash, content.replace("\n", "\\n"));
        }
    }

    // Tree: directory listing — maps filenames to object hashes
    static class GitTree implements GitObject {
        final String name;
        final Map<String, String> entries = new LinkedHashMap<>(); // filename → hash
        private final String hash;

        GitTree(String name, Map<String, String> entries) {
            this.name = name;
            this.entries.putAll(entries);
            this.hash = sha1("tree " + entries.toString());
        }

        @Override public String hash() { return hash; }
        @Override public String type() { return "tree"; }
        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("tree  %s  (%s)%n", hash, name));
            entries.forEach((file, h) ->
                    sb.append(String.format("  ├── %-20s → %s%n", file, h)));
            return sb.toString().stripTrailing();
        }
    }

    // Commit: points to a tree + optional parent + metadata
    static class GitCommit implements GitObject {
        final String message;
        final String treeHash;
        final String parentHash; // null for root commit
        final String author;
        private final String hash;

        GitCommit(String message, String treeHash, String parentHash, String author) {
            this.message    = message;
            this.treeHash   = treeHash;
            this.parentHash = parentHash;
            this.author     = author;
            this.hash       = sha1("commit " + treeHash + parentHash + message);
        }

        @Override public String hash() { return hash; }
        @Override public String type() { return "commit"; }
        @Override public String toString() {
            return String.format("commit %s  tree=%s  parent=%s  author=%s  msg=\"%s\"",
                    hash, treeHash, parentHash == null ? "(root)" : parentHash, author, message);
        }
    }

    // Tag: named pointer to a commit (annotated tag)
    static class GitTag implements GitObject {
        final String tagName;
        final String commitHash;
        final String tagger;
        final String message;
        private final String hash;

        GitTag(String tagName, String commitHash, String tagger, String message) {
            this.tagName    = tagName;
            this.commitHash = commitHash;
            this.tagger     = tagger;
            this.message    = message;
            this.hash       = sha1("tag " + tagName + commitHash);
        }

        @Override public String hash() { return hash; }
        @Override public String type() { return "tag"; }
        @Override public String toString() {
            return String.format("tag   %s  name=%s  commit=%s  \"%s\"",
                    hash, tagName, commitHash, message);
        }
    }

    // -----------------------------------------------------------------------
    // Object Store — content-addressable storage
    // -----------------------------------------------------------------------
    static class ObjectStore {
        private final Map<String, GitObject> objects = new LinkedHashMap<>();

        void store(GitObject obj) { objects.put(obj.hash(), obj); }

        GitObject get(String hash) { return objects.get(hash); }

        void printAll() {
            System.out.println("  Object store (" + objects.size() + " objects):");
            objects.values().forEach(o -> System.out.println("    " + o));
        }

        long countByType(String type) {
            return objects.values().stream().filter(o -> o.type().equals(type)).count();
        }
    }

    public static void main(String[] args) {
        ObjectStore store = new ObjectStore();

        // ----------------------------------------------------------------
        // Commit 1: two files
        // ----------------------------------------------------------------
        GitBlob mainJava  = new GitBlob("public class Main {}");
        GitBlob readmeV1  = new GitBlob("# My Project");
        store.store(mainJava);
        store.store(readmeV1);

        GitTree tree1 = new GitTree("root",
                new LinkedHashMap<>(Map.of("Main.java", mainJava.hash(), "README.md", readmeV1.hash())));
        store.store(tree1);

        GitCommit commit1 = new GitCommit("Initial commit", tree1.hash(), null, "jorex");
        store.store(commit1);

        // ----------------------------------------------------------------
        // Commit 2: README unchanged (same blob!), Main.java updated
        // ----------------------------------------------------------------
        GitBlob mainJavaV2 = new GitBlob("public class Main { /* v2 */ }");
        store.store(mainJavaV2);
        // README blob is REUSED — same content → same hash → no duplicate stored

        GitTree tree2 = new GitTree("root-v2",
                new LinkedHashMap<>(Map.of("Main.java", mainJavaV2.hash(), "README.md", readmeV1.hash())));
        store.store(tree2);

        GitCommit commit2 = new GitCommit("Update Main.java", tree2.hash(), commit1.hash(), "jorex");
        store.store(commit2);

        // ----------------------------------------------------------------
        // Annotated Tag on commit2
        // ----------------------------------------------------------------
        GitTag v1Tag = new GitTag("v1.0.0", commit2.hash(), "jorex", "First stable release");
        store.store(v1Tag);

        // ----------------------------------------------------------------
        // Print full object graph
        // ----------------------------------------------------------------
        System.out.println("=== Git Object Store ===");
        store.printAll();

        System.out.println();
        System.out.println("=== Deduplication demo ===");
        System.out.println("  README.md hash in commit1 tree: " + readmeV1.hash());
        System.out.println("  README.md hash in commit2 tree: " + readmeV1.hash() + "  ← same blob reused");
        System.out.printf("  Blobs: %d  Trees: %d  Commits: %d  Tags: %d  (total: %d)%n",
                store.countByType("blob"),
                store.countByType("tree"),
                store.countByType("commit"),
                store.countByType("tag"),
                store.objects.size());

        System.out.println();
        System.out.println("--- Key concepts ---");
        System.out.println("  blob    → file content (hash based on content, not filename)");
        System.out.println("  tree    → directory (maps names to blob/tree hashes)");
        System.out.println("  commit  → snapshot (tree) + parent + metadata");
        System.out.println("  tag     → named pointer to a commit + annotation");
        System.out.println("  Identical file content → identical hash → stored once (dedup)");
        System.out.println("  .git/objects/ stores all objects; refs/ stores branch/tag pointers");
    }
}
