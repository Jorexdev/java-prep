import java.util.*;

public class ExpInteractiveRebase {

    // -----------------------------------------------------------------------
    // Commit model
    // -----------------------------------------------------------------------
    static class Commit {
        final String hash;
        String message;

        Commit(String hash, String message) {
            this.hash    = hash;
            this.message = message;
        }

        @Override public String toString() { return hash + " " + message; }
    }

    // -----------------------------------------------------------------------
    // Interactive rebase actions
    // -----------------------------------------------------------------------
    enum ActionType { PICK, SQUASH, REWORD, DROP, FIXUP }

    static class RebaseAction {
        final ActionType type;
        final String commitHash;
        final String newMessage; // used by REWORD and SQUASH

        RebaseAction(ActionType type, String commitHash, String newMessage) {
            this.type       = type;
            this.commitHash = commitHash;
            this.newMessage = newMessage;
        }

        static RebaseAction pick(String hash)                         { return new RebaseAction(ActionType.PICK,    hash, null); }
        static RebaseAction squash(String hash, String msg)           { return new RebaseAction(ActionType.SQUASH,  hash, msg);  }
        static RebaseAction reword(String hash, String msg)           { return new RebaseAction(ActionType.REWORD,  hash, msg);  }
        static RebaseAction drop(String hash)                         { return new RebaseAction(ActionType.DROP,    hash, null); }
        static RebaseAction fixup(String hash)                        { return new RebaseAction(ActionType.FIXUP,   hash, null); }
    }

    // -----------------------------------------------------------------------
    // InteractiveRebase engine
    // -----------------------------------------------------------------------
    static class InteractiveRebase {
        private final Map<String, Commit> store;

        InteractiveRebase(List<Commit> commits) {
            store = new LinkedHashMap<>();
            commits.forEach(c -> store.put(c.hash, c));
        }

        List<Commit> execute(List<RebaseAction> actions) {
            List<Commit> result = new ArrayList<>();
            Commit pending = null; // accumulates squash/fixup content

            for (RebaseAction action : actions) {
                Commit c = store.get(action.commitHash);
                if (c == null) { System.out.println("  [warn] unknown hash: " + action.commitHash); continue; }

                switch (action.type) {
                    case PICK -> {
                        if (pending != null) { result.add(pending); pending = null; }
                        pending = new Commit(newHash(c.hash, "pick"), c.message);
                        System.out.println("  PICK    " + c + " → keep as-is");
                    }
                    case REWORD -> {
                        if (pending != null) { result.add(pending); pending = null; }
                        pending = new Commit(newHash(c.hash, "reword"), action.newMessage);
                        System.out.println("  REWORD  " + c + " → \"" + action.newMessage + "\"");
                    }
                    case SQUASH -> {
                        // Merge into previous commit; combined message
                        if (pending == null) { pending = new Commit(newHash(c.hash, "sq"), c.message); }
                        String combined = pending.message + " + " + action.newMessage;
                        pending = new Commit(newHash(pending.hash, "sq"), combined);
                        System.out.println("  SQUASH  " + c + " → merged into previous (combined message)");
                    }
                    case FIXUP -> {
                        // Like squash but discards this commit's message
                        if (pending == null) { pending = new Commit(newHash(c.hash, "fu"), c.message); }
                        pending = new Commit(newHash(pending.hash, "fu"), pending.message);
                        System.out.println("  FIXUP   " + c + " → merged (message discarded)");
                    }
                    case DROP -> {
                        System.out.println("  DROP    " + c + " → removed from history");
                    }
                }
            }
            if (pending != null) result.add(pending);
            return result;
        }

        // Simulate new hash after rebase (real Git would SHA-1 the new content)
        private String newHash(String original, String suffix) {
            return original.substring(0, Math.min(3, original.length())) + suffix.charAt(0) + "'";
        }
    }

    static void printHistory(String label, List<Commit> commits) {
        System.out.println("  " + label + ":");
        for (int i = commits.size() - 1; i >= 0; i--) {
            System.out.println("    * " + commits.get(i));
        }
    }

    public static void main(String[] args) {
        // -----------------------------------------------------------------------
        // Starting history: 5 messy commits on a feature branch
        // -----------------------------------------------------------------------
        List<Commit> original = List.of(
                new Commit("a1b2c3d", "WIP: start login"),
                new Commit("e4f5a6b", "fix typo in login"),
                new Commit("c7d8e9f", "WIP: login almost done"),
                new Commit("1a2b3c4", "feat: complete login feature"),
                new Commit("5d6e7f8", "chore: remove debug logs")
        );

        System.out.println("=== Before Interactive Rebase ===");
        printHistory("git log --oneline", original);

        // -----------------------------------------------------------------------
        // Rebase plan: squash WIPs into the main feature commit, drop typo fix,
        // fixup the chore (keep message of feature commit)
        // -----------------------------------------------------------------------
        List<RebaseAction> plan = List.of(
                RebaseAction.drop(  "a1b2c3d"),                                      // drop WIP
                RebaseAction.drop(  "e4f5a6b"),                                      // drop typo fix
                RebaseAction.drop(  "c7d8e9f"),                                      // drop WIP2
                RebaseAction.reword("1a2b3c4", "feat: add login feature"),           // clean up message
                RebaseAction.fixup( "5d6e7f8")                                       // absorb without message
        );

        System.out.println("\n=== Applying Rebase Plan ===");
        InteractiveRebase rebase = new InteractiveRebase(new ArrayList<>(original));
        List<Commit> rebased = rebase.execute(plan);

        System.out.println("\n=== After Interactive Rebase ===");
        printHistory("git log --oneline", rebased);

        System.out.println();
        System.out.println("--- Action summary ---");
        System.out.println("  PICK    → keep commit exactly as-is");
        System.out.println("  REWORD  → keep changes, edit commit message");
        System.out.println("  SQUASH  → merge into previous, combine both messages");
        System.out.println("  FIXUP   → merge into previous, discard this commit's message");
        System.out.println("  DROP    → remove commit entirely from history");
        System.out.println();
        System.out.println("  Use case: clean up a feature branch before opening a PR");
        System.out.println("  Warning:  rewrites history — only safe on unpushed commits");
        System.out.println("  Command:  git rebase -i HEAD~N  (N = number of commits to edit)");
    }
}
