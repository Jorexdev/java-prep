import java.util.List;

public class Ejercicio3 {

    static class Commit {
        final String hash;
        final String message;
        final boolean hasBug;

        Commit(String hash, String message, boolean hasBug) {
            this.hash = hash;
            this.message = message;
            this.hasBug = hasBug;
        }

        @Override
        public String toString() {
            return hash.substring(0, 7) + " [" + (hasBug ? "BUG" : " OK") + "] " + message;
        }
    }

    // Bisect: búsqueda binaria entre índices good (sin bug) y bad (con bug)
    // Devuelve el commit que introdujo el bug (primer bad)
    static Commit bisect(List<Commit> commits, int good, int bad) {
        System.out.printf("Rango inicial: good=%d (%s), bad=%d (%s)%n",
                good, commits.get(good).hash.substring(0, 7),
                bad,  commits.get(bad).hash.substring(0, 7));
        System.out.println();

        int lo = good;
        int hi = bad;
        int step = 1;

        while (hi - lo > 1) {
            int mid = (lo + hi) / 2;
            Commit candidate = commits.get(mid);
            System.out.printf("Paso %d — comprobando commit[%d]: %s%n", step++, mid, candidate);

            if (candidate.hasBug) {
                System.out.println("  -> tiene bug => mover 'bad' a " + mid);
                hi = mid;
            } else {
                System.out.println("  -> sin bug   => mover 'good' a " + mid);
                lo = mid;
            }
        }

        Commit firstBad = commits.get(hi);
        System.out.println();
        System.out.println("=== Primer commit con bug encontrado ===");
        System.out.println(firstBad);
        return firstBad;
    }

    public static void main(String[] args) {
        // 16 commits: los primeros 5 están OK, a partir del 6 tienen bug
        List<Commit> commits = List.of(
            new Commit("a000001", "Initial commit",          false),
            new Commit("a000002", "Add login module",        false),
            new Commit("a000003", "Add logout",              false),
            new Commit("a000004", "Refactor services",       false),
            new Commit("a000005", "Add caching layer",       false),
            new Commit("a000006", "Optimize DB queries",     true),  // <- INTRODUCE el bug
            new Commit("a000007", "Add rate limiting",       true),
            new Commit("a000008", "Update dependencies",     true),
            new Commit("a000009", "Add metrics endpoint",    true),
            new Commit("a000010", "Fix typo in README",      true),
            new Commit("a000011", "Add integration tests",   true),
            new Commit("a000012", "Bump version to 2.0",     true),
            new Commit("a000013", "Add Swagger docs",        true),
            new Commit("a000014", "Performance improvements",true),
            new Commit("a000015", "Security hardening",      true),
            new Commit("a000016", "Release candidate",       true)
        );

        System.out.println("=== Historial de commits ===");
        for (int i = 0; i < commits.size(); i++) {
            System.out.printf("[%2d] %s%n", i, commits.get(i));
        }
        System.out.println();

        System.out.println("=== git bisect ===");
        System.out.println("Sabemos: commit[0] es bueno, commit[15] es malo");
        System.out.println();

        Commit result = bisect(commits, 0, commits.size() - 1);

        System.out.println();
        System.out.println("Comando equivalente en git:");
        System.out.println("  git bisect start");
        System.out.println("  git bisect bad  " + commits.get(commits.size() - 1).hash);
        System.out.println("  git bisect good " + commits.get(0).hash);
        System.out.println("  => " + result.hash + " is the first bad commit");
    }
}
