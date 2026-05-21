import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Ejercicio1 {

    static class Commit {
        final String hash;
        final String message;
        final Commit parent;

        Commit(String hash, String message, Commit parent) {
            this.hash = hash;
            this.message = message;
            this.parent = parent;
        }

        @Override
        public String toString() {
            return hash.substring(0, 7) + " " + message;
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

    static String newHash() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 7);
    }

    // Devuelve los commits desde `ancestor` (excl.) hasta `head` (incl.) en orden cronológico
    static List<Commit> commitsSince(Commit ancestor, Commit head) {
        List<Commit> path = new ArrayList<>();
        Commit cur = head;
        while (cur != null && !cur.hash.equals(ancestor.hash)) {
            path.add(cur);
            cur = cur.parent;
        }
        Collections.reverse(path);
        return path;
    }

    // Encontrar ancestro común (recorrido lineal simplificado)
    static Commit findAncestor(Commit a, Commit b) {
        List<String> ancestorsA = new ArrayList<>();
        Commit cur = a;
        while (cur != null) {
            ancestorsA.add(cur.hash);
            cur = cur.parent;
        }
        cur = b;
        while (cur != null) {
            if (ancestorsA.contains(cur.hash)) return cur;
            cur = cur.parent;
        }
        return null;
    }

    // Rebase: re-aplica los commits de feature sobre el HEAD actual de main
    static void rebase(Branch feature, Branch main) {
        Commit ancestor = findAncestor(feature.head, main.head);
        List<Commit> featureCommits = commitsSince(ancestor, feature.head);

        System.out.println("Ancestro común: " + ancestor);
        System.out.println("Commits a re-aplicar (" + featureCommits.size() + "):");
        featureCommits.forEach(c -> System.out.println("  " + c));
        System.out.println();

        // Re-aplicar cada commit sobre el nuevo base (main HEAD)
        Commit newBase = main.head;
        for (Commit original : featureCommits) {
            String newHash = newHash();
            newBase = new Commit(newHash, original.message, newBase);
            System.out.printf("Re-aplicado: %s -> nuevo hash %s (%s)%n",
                    original.hash.substring(0, 7), newHash.substring(0, 7), original.message);
        }
        feature.head = newBase;
    }

    static void printLog(Branch b) {
        System.out.println("Log de '" + b.name + "':");
        Commit c = b.head;
        while (c != null) {
            System.out.println("  " + c);
            c = c.parent;
        }
    }

    public static void main(String[] args) {
        // Historial compartido
        Commit base = new Commit("aaa0001", "Initial commit", null);

        // main avanza 2 commits
        Commit m1 = new Commit("bbb0001", "main: update config", base);
        Commit m2 = new Commit("bbb0002", "main: fix security issue", m1);
        Branch main = new Branch("main", m2);

        // feature bifurca desde base y tiene 3 commits
        Commit f1 = new Commit("ccc0001", "feat: add user model", base);
        Commit f2 = new Commit("ccc0002", "feat: add user service", f1);
        Commit f3 = new Commit("ccc0003", "feat: add user controller", f2);
        Branch feature = new Branch("feature/users", f3);

        System.out.println("=== ANTES del rebase ===");
        printLog(main);
        System.out.println();
        printLog(feature);
        System.out.println();

        System.out.println("=== Ejecutando rebase ===");
        rebase(feature, main);
        System.out.println();

        System.out.println("=== DESPUÉS del rebase ===");
        printLog(feature);
        System.out.println();
        System.out.println("Nota: los commits de feature ahora se apoyan sobre main HEAD");
        System.out.println("Los mensajes son idénticos pero los hashes son nuevos");
    }
}
