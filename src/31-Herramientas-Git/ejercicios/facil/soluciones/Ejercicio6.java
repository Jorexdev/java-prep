import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio6 {

    static class Commit {
        final String hash;
        final String message;
        final String author;
        final Instant timestamp;

        Commit(String hash, String message, String author, Instant timestamp) {
            this.hash = hash;
            this.message = message;
            this.author = author;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("commit %s | autor: %-8s | %s", hash.substring(0, 7), author, message);
        }
    }

    // Filtra commits por autor y por texto en mensaje (ambos opcionales si son null)
    static List<Commit> gitLog(List<Commit> commits, String author, String grepMessage) {
        return commits.stream()
                .filter(c -> author == null || c.author.equalsIgnoreCase(author))
                .filter(c -> grepMessage == null || c.message.toLowerCase().contains(grepMessage.toLowerCase()))
                .collect(Collectors.toList());
    }

    static void printLog(List<Commit> commits, String title) {
        System.out.println("=== " + title + " (" + commits.size() + " commits) ===");
        commits.forEach(System.out::println);
        System.out.println();
    }

    public static void main(String[] args) {
        Instant base = Instant.parse("2024-03-01T09:00:00Z");

        List<Commit> allCommits = List.of(
            new Commit("a1b2c3d", "Initial project setup",       "bob",   base),
            new Commit("b2c3d4e", "fix: login null pointer",     "alice", base.plusSeconds(3600)),
            new Commit("c3d4e5f", "Add user registration",       "alice", base.plusSeconds(7200)),
            new Commit("d4e5f6a", "fix: password validation",    "alice", base.plusSeconds(10800)),
            new Commit("e5f6a7b", "Add dashboard view",          "bob",   base.plusSeconds(14400)),
            new Commit("f6a7b8c", "fix: dashboard crash on load","bob",   base.plusSeconds(18000)),
            new Commit("a7b8c9d", "Add API endpoints",           "alice", base.plusSeconds(21600)),
            new Commit("b8c9d0e", "fix: token expiry handling",  "alice", base.plusSeconds(25200)),
            new Commit("c9d0e1f", "Improve performance",         "bob",   base.plusSeconds(28800)),
            new Commit("d0e1f2a", "fix: memory leak in cache",   "alice", base.plusSeconds(32400))
        );

        printLog(allCommits, "Historial completo");

        // Filtrar solo commits de alice
        List<Commit> byAlice = gitLog(allCommits, "alice", null);
        printLog(byAlice, "Commits de 'alice'");

        // Filtrar solo commits con "fix" en el mensaje
        List<Commit> fixes = gitLog(allCommits, null, "fix");
        printLog(fixes, "Commits con 'fix' en el mensaje");

        // Filtrar: alice + contienen "fix"
        List<Commit> aliceFixes = gitLog(allCommits, "alice", "fix");
        printLog(aliceFixes, "Commits de 'alice' con 'fix'");
    }
}
