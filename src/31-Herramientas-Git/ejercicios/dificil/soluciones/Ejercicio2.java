import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ejercicio2 {

    static class ReflogEntry {
        final String action;
        final String hash;
        final Instant timestamp;
        final String description;

        ReflogEntry(String action, String hash, String description) {
            this.action = action;
            this.hash = hash;
            this.timestamp = Instant.now();
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("%s@{%s}: %s: %s",
                    hash.substring(0, 7), timestamp, action, description);
        }
    }

    static class Reflog {
        // Más reciente al inicio (índice 0 = más reciente)
        private final List<ReflogEntry> entries = new ArrayList<>();

        void record(String action, String hash, String description) {
            entries.add(0, new ReflogEntry(action, hash, description));
            System.out.printf("[reflog] %s %s - %s%n", action, hash.substring(0, 7), description);
        }

        // Muestra las últimas 10 entradas
        void gitReflog() {
            System.out.println("=== git reflog ===");
            int limit = Math.min(10, entries.size());
            for (int i = 0; i < limit; i++) {
                System.out.printf("HEAD@{%d} %s%n", i, entries.get(i));
            }
        }

        // Restaura el estado de n operaciones atrás
        ReflogEntry reset(int n) {
            if (n >= entries.size()) {
                throw new IllegalArgumentException("Solo hay " + entries.size() + " entradas en el reflog");
            }
            ReflogEntry target = entries.get(n);
            System.out.printf("%nRestaurando HEAD@{%d}: %s%n", n, target);
            // Registrar el propio reset en el reflog
            record("reset", target.hash, "HEAD@{" + n + "}");
            return target;
        }

        int size() { return entries.size(); }
    }

    public static void main(String[] args) throws InterruptedException {
        Reflog reflog = new Reflog();

        System.out.println("=== Simulando operaciones Git ===");
        System.out.println();

        // Secuencia de operaciones
        reflog.record("commit", "a1b2c3d", "Initial commit");
        reflog.record("commit", "b2c3d4e", "Add feature");
        reflog.record("checkout", "c3d4e5f", "moving from main to feature/login");
        reflog.record("commit", "d4e5f6a", "feat: login form");
        reflog.record("commit", "e5f6a7b", "feat: login validation");
        reflog.record("merge",  "f6a7b8c", "Merge branch 'feature/login' into main");
        reflog.record("reset",  "e5f6a7b", "moving to HEAD~1");
        reflog.record("checkout","a7b8c9d", "moving from main to hotfix/null-ptr");
        reflog.record("commit", "b8c9d0e", "fix: null pointer in UserService");
        reflog.record("checkout","c9d0e1f", "moving from hotfix to main");
        reflog.record("merge",  "d0e1f2a", "Merge branch 'hotfix/null-ptr'");
        reflog.record("commit", "e1f2a3b", "chore: bump version");

        System.out.println();
        reflog.gitReflog();

        System.out.println();
        System.out.println("=== Recuperación: volver 3 operaciones atrás ===");
        System.out.println("(Útil si hiciste un reset --hard accidental)");
        ReflogEntry restored = reflog.reset(3);
        System.out.println("HEAD ahora apunta a: " + restored.hash.substring(0, 7));

        System.out.println();
        System.out.println("=== Reflog actualizado (después del reset) ===");
        reflog.gitReflog();
    }
}
