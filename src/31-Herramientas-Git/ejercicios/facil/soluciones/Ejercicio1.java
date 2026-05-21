import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    static class Commit {
        final String hash;
        final String message;
        final Commit parent;
        final String author;
        final Instant timestamp;

        Commit(String hash, String message, Commit parent, String author, Instant timestamp) {
            this.hash = hash;
            this.message = message;
            this.parent = parent;
            this.author = author;
            this.timestamp = timestamp;
        }
    }

    static List<Commit> gitLog(Commit head) {
        List<Commit> history = new ArrayList<>();
        Commit current = head;
        while (current != null) {
            history.add(current);
            current = current.parent;
        }
        return history;
    }

    static void printLog(Commit head) {
        System.out.println("=== git log ===");
        for (Commit c : gitLog(head)) {
            System.out.printf("commit %s%n", c.hash);
            System.out.printf("Author: %s%n", c.author);
            System.out.printf("Date:   %s%n", c.timestamp);
            System.out.printf("%n    %s%n%n", c.message);
        }
    }

    public static void main(String[] args) {
        Instant base = Instant.parse("2024-01-01T10:00:00Z");

        Commit c1 = new Commit("a1b2c3d", "Initial commit", null, "alice", base);
        Commit c2 = new Commit("b2c3d4e", "Add README", c1, "alice", base.plusSeconds(3600));
        Commit c3 = new Commit("c3d4e5f", "Add src layout", c2, "bob", base.plusSeconds(7200));
        Commit c4 = new Commit("d4e5f6a", "Implement login", c3, "alice", base.plusSeconds(10800));
        Commit c5 = new Commit("e5f6a7b", "Fix NPE in login", c4, "bob", base.plusSeconds(14400));
        Commit c6 = new Commit("f6a7b8c", "Add unit tests", c5, "alice", base.plusSeconds(18000));

        printLog(c6);

        System.out.println("Total de commits: " + gitLog(c6).size());
    }
}
