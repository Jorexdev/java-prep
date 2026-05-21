import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ejercicio2 {

    static class Commit {
        final String hash;
        final String message;
        final Commit parent;

        Commit(String hash, String message, Commit parent) {
            this.hash = hash;
            this.message = message;
            this.parent = parent;
        }
    }

    static class Branch {
        String name;
        Commit head;

        Branch(String name, Commit head) {
            this.name = name;
            this.head = head;
        }
    }

    // Devuelve true si `ancestor` es ancestro de `commit`
    static boolean isAncestor(Commit ancestor, Commit commit) {
        Commit current = commit;
        while (current != null) {
            if (current.hash.equals(ancestor.hash)) return true;
            current = current.parent;
        }
        return false;
    }

    // Fast-forward merge: target avanza hasta source si source es descendiente de target
    static String merge(Branch target, Branch source) {
        Commit targetHead = target.head;
        Commit sourceHead = source.head;

        if (isAncestor(targetHead, sourceHead)) {
            // source está por delante de target — fast-forward posible
            target.head = sourceHead;
            return "Fast-forward";
        } else if (isAncestor(sourceHead, targetHead)) {
            return "Already up to date.";
        } else {
            return "Diverged — necesita 3-way merge";
        }
    }

    static void printBranch(Branch b) {
        List<String> commits = new ArrayList<>();
        Commit c = b.head;
        while (c != null) {
            commits.add(c.hash.substring(0, 7) + " " + c.message);
            c = c.parent;
        }
        System.out.printf("Branch '%s' HEAD -> %s%n", b.name, b.head.hash.substring(0, 7));
        commits.forEach(s -> System.out.println("  " + s));
    }

    public static void main(String[] args) {
        // Historial compartido en main
        Commit c1 = new Commit("a1b2c3d", "Initial commit", null);
        Commit c2 = new Commit("b2c3d4e", "Add README", c1);

        // feature avanza 3 commits sobre main
        Commit f1 = new Commit("c3d4e5f", "Feature: add login", c2);
        Commit f2 = new Commit("d4e5f6a", "Feature: add logout", f1);
        Commit f3 = new Commit("e5f6a7b", "Feature: add tests", f2);

        Branch main    = new Branch("main", c2);
        Branch feature = new Branch("feature", f3);

        System.out.println("=== ANTES del merge ===");
        printBranch(main);
        printBranch(feature);

        String result = merge(main, feature);
        System.out.println("\n=== merge result: " + result + " ===");

        System.out.println("\n=== DESPUÉS del merge ===");
        printBranch(main);
        System.out.println("\n(Nota: no se creó commit de merge — main apunta al mismo commit que feature)");
        System.out.println("main HEAD == feature HEAD: " + main.head.hash.equals(feature.head.hash));
    }
}
