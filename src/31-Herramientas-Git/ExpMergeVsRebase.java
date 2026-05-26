import java.util.*;

public class ExpMergeVsRebase {

    static class Commit {
        final String hash;
        final String message;
        final List<String> parents;

        Commit(String hash, String message, String... parents) {
            this.hash    = hash;
            this.message = message;
            this.parents = Arrays.asList(parents);
        }

        boolean isMerge() { return parents.size() > 1; }

        @Override public String toString() {
            return hash + " " + message;
        }
    }

    // -----------------------------------------------------------------------
    // Build the diverged starting state:
    //   main:    C1 → C2 → C3
    //   feature: C1 → C2 → F1 → F2
    // -----------------------------------------------------------------------
    static Map<String, Commit> buildBase() {
        Map<String, Commit> store = new LinkedHashMap<>();
        store.put("C1", new Commit("C1", "Initial commit"));
        store.put("C2", new Commit("C2", "Add README",       "C1"));
        store.put("C3", new Commit("C3", "Fix typo in main", "C2"));
        store.put("F1", new Commit("F1", "feat: add login",  "C2"));
        store.put("F2", new Commit("F2", "feat: add logout", "F1"));
        return store;
    }

    // -----------------------------------------------------------------------
    // Strategy 1: MERGE
    //   Creates a merge commit M with two parents: C3 and F2.
    //   Both branch histories are preserved.
    // -----------------------------------------------------------------------
    static void demoMerge(Map<String, Commit> base) {
        Map<String, Commit> store = new LinkedHashMap<>(base);

        // Merge commit: main tip (C3) + feature tip (F2)
        Commit M = new Commit("M1", "Merge feature/auth into main", "C3", "F2");
        store.put("M1", M);

        System.out.println("  After: git merge feature/auth (from main)");
        System.out.println();
        System.out.println("    *   M1 Merge feature/auth into main");
        System.out.println("    |\\");
        System.out.println("    | * F2 feat: add logout");
        System.out.println("    | * F1 feat: add login");
        System.out.println("    * | C3 Fix typo in main");
        System.out.println("    |/");
        System.out.println("    * C2 Add README");
        System.out.println("    * C1 Initial commit");
        System.out.println();
        System.out.println("  main HEAD → M1 (two parents: C3, F2)");
        System.out.println("  History preserved: non-linear, shows exactly when branches diverged");
        System.out.println("  New commits: M1 (1 merge commit, original hashes unchanged)");
    }

    // -----------------------------------------------------------------------
    // Strategy 2: REBASE
    //   Replays F1 and F2 on top of C3 as new commits F1' and F2'.
    //   Produces a linear history but rewrites hashes.
    // -----------------------------------------------------------------------
    static void demoRebase(Map<String, Commit> base) {
        // Rebased commits get new hashes (content is same, parent changes → new hash)
        Commit f1prime = new Commit("F1'", "feat: add login  [rebased]",  "C3");
        Commit f2prime = new Commit("F2'", "feat: add logout [rebased]",  "F1'");

        System.out.println("  After: git rebase main (from feature/auth)");
        System.out.println();
        System.out.println("    * F2' feat: add logout [rebased]");
        System.out.println("    * F1' feat: add login  [rebased]");
        System.out.println("    * C3  Fix typo in main");
        System.out.println("    * C2  Add README");
        System.out.println("    * C1  Initial commit");
        System.out.println();
        System.out.println("  feature/auth HEAD → F2'");
        System.out.println("  History rewritten: linear, no merge commit");
        System.out.println("  New commits: F1' and F2' (different hashes than F1, F2)");
        System.out.println("  Danger: never rebase commits that others have already pulled");
    }

    // -----------------------------------------------------------------------
    // Fast-forward merge (possible only when no divergence)
    // -----------------------------------------------------------------------
    static void demoFastForward() {
        System.out.println("  Fast-forward merge (when main has no new commits after branch point):");
        System.out.println("    Before: main → C2, feature → F2 (linear from C2)");
        System.out.println("    After:  main → F2 (pointer just moves forward, no merge commit)");
        System.out.println("    git merge --no-ff forces a merge commit even when FF is possible");
    }

    public static void main(String[] args) {
        System.out.println("=== Starting State ===");
        System.out.println("  main:    C1 → C2 → C3");
        System.out.println("  feature: C1 → C2 → F1 → F2  (diverged after C2)");
        System.out.println();

        System.out.println("=== Strategy 1: Merge ===");
        demoMerge(buildBase());
        System.out.println();

        System.out.println("=== Strategy 2: Rebase ===");
        demoRebase(buildBase());
        System.out.println();

        System.out.println("=== Bonus: Fast-Forward ===");
        demoFastForward();
        System.out.println();

        System.out.println("--- When to use each ---");
        System.out.println("  Merge:  shared/public branches (main, develop); audit trail matters");
        System.out.println("  Rebase: local feature branches before opening a PR; keeps log clean");
        System.out.println("  Rule:   never rebase pushed commits others may have based work on");
        System.out.println("  Golden: git rebase → git push → PR → merge (with --no-ff for traceability)");
    }
}
