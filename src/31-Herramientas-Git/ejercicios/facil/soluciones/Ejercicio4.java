import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ejercicio4 {

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

    // Genera un hash corto pseudo-aleatorio
    static String newHash() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 7);
    }

    // Cherry-pick: crea un nuevo commit con el mismo mensaje + "(cherry-pick)"
    static Commit cherryPick(Commit source, Branch target) {
        String newHash = newHash();
        String newMessage = source.message + " (cherry-pick)";
        Commit newCommit = new Commit(newHash, newMessage, target.head);
        target.head = newCommit;
        System.out.printf("cherry-pick %s -> nuevo commit %s en '%s'%n",
                source.hash.substring(0, 7), newHash.substring(0, 7), target.name);
        return newCommit;
    }

    static void printLog(Branch branch) {
        System.out.println("Log de '" + branch.name + "':");
        Commit c = branch.head;
        while (c != null) {
            System.out.println("  " + c);
            c = c.parent;
        }
    }

    public static void main(String[] args) {
        // Historial base compartido
        Commit base = new Commit("a1b2c3d", "Initial commit", null);

        // Rama main: 2 commits normales
        Commit m1 = new Commit("b2c3d4e", "Add feature A", base);
        Commit m2 = new Commit("c3d4e5f", "Add feature B", m1);
        Branch main = new Branch("main", m2);

        // Rama hotfix: 1 bugfix crítico
        Commit h1 = new Commit("d4e5f6a", "Fix: null pointer in UserService", base);
        Commit h2 = new Commit("e5f6a7b", "Fix: add input validation", h1);
        Branch hotfix = new Branch("hotfix", h2);

        System.out.println("=== ANTES del cherry-pick ===");
        printLog(main);
        System.out.println();
        printLog(hotfix);
        System.out.println();

        // Cherry-pick del bugfix crítico (h1) a main
        System.out.println("=== Ejecutando cherry-pick ===");
        cherryPick(h1, main);
        cherryPick(h2, main);
        System.out.println();

        System.out.println("=== DESPUÉS del cherry-pick ===");
        printLog(main);
        System.out.println();
        System.out.println("Nota: main tiene los fixes sin tener el historial completo de hotfix");
        System.out.println("Los hashes son distintos (nuevo commit) pero el mensaje es el mismo + (cherry-pick)");
    }
}
