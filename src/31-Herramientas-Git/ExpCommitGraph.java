import java.util.*;

public class ExpCommitGraph {

    // -----------------------------------------------------------------------
    // Git Object Model — simplified DAG
    // -----------------------------------------------------------------------
    static class Commit {
        final String hash;       // short 7-char hash
        final String message;
        final List<String> parents; // 0=root, 1=linear, 2=merge commit

        Commit(String hash, String message, String... parents) {
            this.hash    = hash;
            this.message = message;
            this.parents = Arrays.asList(parents);
        }

        boolean isMerge() { return parents.size() == 2; }

        @Override public String toString() {
            return hash + " " + message + (isMerge() ? " [merge]" : "");
        }
    }

    // -----------------------------------------------------------------------
    // Repository: stores commits + named refs (HEAD, branches)
    // -----------------------------------------------------------------------
    static class Repository {
        private final Map<String, Commit> store = new LinkedHashMap<>();
        private final Map<String, String> refs  = new LinkedHashMap<>(); // name → hash

        void add(Commit c) { store.put(c.hash, c); }

        void setRef(String name, String hash) { refs.put(name, hash); }

        Commit get(String hash) { return store.get(hash); }

        String resolve(String ref) {
            // ref can be a branch name or direct hash
            return refs.getOrDefault(ref, ref);
        }

        // Walk history from a ref, depth-first via first parent
        List<Commit> log(String ref) {
            List<Commit> result = new ArrayList<>();
            Set<String> visited = new LinkedHashSet<>();
            Queue<String> queue = new ArrayDeque<>();
            queue.add(resolve(ref));

            while (!queue.isEmpty()) {
                String hash = queue.poll();
                if (hash == null || visited.contains(hash)) continue;
                visited.add(hash);
                Commit c = store.get(hash);
                if (c != null) {
                    result.add(c);
                    c.parents.forEach(queue::add);
                }
            }
            return result;
        }

        // Find lowest common ancestor via BFS from both commits
        String lca(String hashA, String hashB) {
            Set<String> ancestorsA = new LinkedHashSet<>();
            Queue<String> q = new ArrayDeque<>();
            q.add(hashA);
            while (!q.isEmpty()) {
                String h = q.poll();
                if (h == null || !store.containsKey(h) || !ancestorsA.add(h)) continue;
                store.get(h).parents.forEach(q::add);
            }

            q.add(hashB);
            Set<String> visited = new HashSet<>();
            while (!q.isEmpty()) {
                String h = q.poll();
                if (h == null || !store.containsKey(h) || !visited.add(h)) continue;
                if (ancestorsA.contains(h)) return h;
                store.get(h).parents.forEach(q::add);
            }
            return null;
        }

        void printGraph() {
            System.out.println("  Refs: " + refs);
            System.out.println("  Commits (insertion order):");
            store.values().forEach(c -> {
                String parents = c.parents.isEmpty() ? "(root)"
                        : "← " + String.join(", ", c.parents);
                System.out.printf("    %s  %s%n", c, parents);
            });
        }
    }

    // -----------------------------------------------------------------------
    // Build demo: linear → branch → merge
    // -----------------------------------------------------------------------
    static Repository buildRepo() {
        Repository repo = new Repository();

        Commit c1 = new Commit("a1b2c3d", "Initial commit");
        Commit c2 = new Commit("e4f5a6b", "Add README",      "a1b2c3d");
        Commit c3 = new Commit("c7d8e9f", "Add pom.xml",     "e4f5a6b");
        // feature branch diverges from c2
        Commit f1 = new Commit("1a2b3c4", "feat: add login", "e4f5a6b");
        Commit f2 = new Commit("5d6e7f8", "feat: add logout","1a2b3c4");
        // merge commit has two parents: main tip (c3) and feature tip (f2)
        Commit m1 = new Commit("9a0b1c2", "Merge feature/auth into main", "c7d8e9f", "5d6e7f8");

        for (Commit c : List.of(c1, c2, c3, f1, f2, m1)) repo.add(c);

        repo.setRef("main",         "9a0b1c2");
        repo.setRef("feature/auth", "5d6e7f8");
        repo.setRef("HEAD",         "9a0b1c2");

        return repo;
    }

    static void printLog(Repository repo, String ref) {
        System.out.println("  git log " + ref + ":");
        repo.log(ref).forEach(c -> System.out.println("    " + c));
    }

    static void printAsciiGraph() {
        System.out.println("  ASCII graph (git log --oneline --graph):");
        System.out.println("    *   9a0b1c2 Merge feature/auth into main");
        System.out.println("    |\\");
        System.out.println("    | * 5d6e7f8 feat: add logout");
        System.out.println("    | * 1a2b3c4 feat: add login");
        System.out.println("    * | c7d8e9f Add pom.xml");
        System.out.println("    |/");
        System.out.println("    * e4f5a6b Add README");
        System.out.println("    * a1b2c3d Initial commit");
    }

    public static void main(String[] args) {
        Repository repo = buildRepo();

        System.out.println("=== Commit Graph ===");
        repo.printGraph();

        System.out.println();
        printAsciiGraph();

        System.out.println();
        printLog(repo, "main");

        System.out.println();
        System.out.println("  LCA (lowest common ancestor) of main..feature/auth:");
        String ancestor = repo.lca("9a0b1c2", "5d6e7f8");
        System.out.println("    → " + repo.get(ancestor));

        System.out.println();
        System.out.println("--- Concepts ---");
        System.out.println("  Commit   → snapshot + parent refs (DAG node)");
        System.out.println("  Branch   → movable pointer to a commit hash");
        System.out.println("  HEAD     → pointer to current branch (or detached commit)");
        System.out.println("  Merge    → commit with 2 parents, preserves both histories");
        System.out.println("  git diff A...B → changes reachable from B not from A (common ancestor)");
    }
}
