import java.util.*;
import java.util.function.Predicate;

public class Ejercicio5 {

    static class Commit {
        int index;   // posición en el historial (0 = más antiguo)
        String hash;
        String message;

        Commit(int index, String hash, String message) {
            this.index   = index;
            this.hash    = hash;
            this.message = message;
        }

        @Override public String toString() {
            return String.format("c%02d [%s] %s", index + 1, hash, message);
        }
    }

    static class GitBisect {
        List<Commit> commits;
        Predicate<Commit> testFn;   // true = tiene el bug
        int testCount = 0;

        GitBisect(List<Commit> commits, Predicate<Commit> testFn) {
            this.commits = commits;
            this.testFn  = testFn;
        }

        // Búsqueda binaria para encontrar el primer commit con el bug
        Commit bisect() {
            int low  = 0;
            int high = commits.size() - 1;
            int step = 0;

            System.out.println("=== git bisect start ===");
            System.out.printf("  good=%s  bad=%s  rango=%d commits%n%n",
                    commits.get(low), commits.get(high), commits.size());

            while (low < high) {
                step++;
                int mid = (low + high) / 2;
                Commit candidate = commits.get(mid);
                testCount++;

                boolean hasBug = testFn.test(candidate);
                System.out.printf("Paso %d | probando %s | rango=[%d,%d]%n",
                        step, candidate, low + 1, high + 1);
                System.out.printf("       → %s%n", hasBug ? "BAD (tiene el bug)" : "GOOD (sin bug)");

                if (hasBug) {
                    // El bug está en mid o antes → buscamos en [low, mid]
                    high = mid;
                } else {
                    // Sin bug en mid → el primer bug está después
                    low = mid + 1;
                }
                System.out.printf("       → nuevo rango: [%d,%d] (%d commits restantes)%n%n",
                        low + 1, high + 1, high - low + 1);
            }

            Commit firstBad = commits.get(low);
            System.out.printf("=== Resultado: primer commit con el bug ===%n");
            System.out.printf("  %s%n", firstBad);
            System.out.printf("  Pasos necesarios: %d (de %d commits — log2(%d)≈%.1f)%n",
                    testCount, commits.size(), commits.size(),
                    Math.log(commits.size()) / Math.log(2));
            return firstBad;
        }
    }

    public static void main(String[] args) {
        // 16 commits: c01..c16. El bug se introduce en el c11
        List<Commit> history = new ArrayList<>();
        String[] messages = {
            "feat: bootstrap del proyecto",
            "feat: modelo de usuario",
            "feat: autenticación básica",
            "feat: endpoints REST",
            "refactor: separar capas",
            "feat: validaciones de entrada",
            "feat: módulo de pagos",
            "feat: integración con proveedor externo",
            "fix: timeout en llamadas externas",
            "feat: caché de sesiones",
            "feat: refactorizar módulo de sesiones",  // <-- introduce el bug (índice 10 = c11)
            "feat: logging estructurado",
            "feat: métricas prometheus",
            "fix: race condition en cache",
            "feat: endpoint de health",
            "chore: actualizar dependencias"
        };

        for (int i = 0; i < messages.length; i++) {
            history.add(new Commit(i, "h" + String.format("%02d", i + 1), messages[i]));
        }

        System.out.println("=== Historial completo (" + history.size() + " commits) ===");
        history.forEach(c -> System.out.println("  " + c));
        System.out.println();

        // El bug existe a partir del commit con índice 10 (c11)
        int bugIntroducedAt = 10;
        Predicate<Commit> hasBug = c -> c.index >= bugIntroducedAt;

        GitBisect bisect = new GitBisect(history, hasBug);
        Commit result = bisect.bisect();

        System.out.printf("%nVerificación: índice esperado=10 (c11), encontrado=%d (%s) → %s%n",
                result.index, result.hash,
                result.index == bugIntroducedAt ? "CORRECTO" : "ERROR");
    }
}
