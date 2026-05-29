import java.util.*;

public class Ejercicio6 {

    static class Commit {
        String hash;
        String message;
        String content;

        Commit(String hash, String message, String content) {
            this.hash    = hash;
            this.message = message;
            this.content = content;
        }

        @Override public String toString() {
            return String.format("  %s  %s", hash, message);
        }
    }

    enum RebaseOp { PICK, SQUASH, REWORD, DROP }

    static class RebasePlan {
        record Step(RebaseOp op, String hash, String newMessage) {}

        List<Step> steps = new ArrayList<>();

        void pick(String hash) {
            steps.add(new Step(RebaseOp.PICK, hash, null));
        }

        void squash(String hash) {
            steps.add(new Step(RebaseOp.SQUASH, hash, null));
        }

        void reword(String hash, String newMessage) {
            steps.add(new Step(RebaseOp.REWORD, hash, newMessage));
        }

        void drop(String hash) {
            steps.add(new Step(RebaseOp.DROP, hash, null));
        }
    }

    static class InteractiveRebase {

        List<Commit> apply(List<Commit> commits, RebasePlan plan) {
            Map<String, Commit> byHash = new LinkedHashMap<>();
            commits.forEach(c -> byHash.put(c.hash, c));

            List<Commit> result  = new ArrayList<>();
            int newIndex = 1;

            System.out.println("=== Aplicando plan de rebase interactivo ===");

            for (RebasePlan.Step step : plan.steps) {
                Commit original = byHash.get(step.hash);
                if (original == null) {
                    System.out.printf("  WARN: commit '%s' no encontrado, ignorando%n", step.hash);
                    continue;
                }

                switch (step.op) {
                    case PICK -> {
                        String newHash = "a" + newIndex++;
                        Commit picked = new Commit(newHash, original.message, original.content);
                        result.add(picked);
                        System.out.printf("  pick   %s → %s  '%s'%n",
                                original.hash, newHash, original.message);
                    }
                    case SQUASH -> {
                        if (result.isEmpty()) {
                            throw new IllegalStateException("squash no puede ser el primer commit");
                        }
                        Commit prev = result.get(result.size() - 1);
                        String combinedMsg     = prev.message + " / " + original.message;
                        String combinedContent = prev.content + "\n" + original.content;
                        Commit squashed = new Commit(prev.hash, combinedMsg, combinedContent);
                        result.set(result.size() - 1, squashed);
                        System.out.printf("  squash %s → fusionado con %s  '%s'%n",
                                original.hash, prev.hash, combinedMsg);
                    }
                    case REWORD -> {
                        String newHash = "a" + newIndex++;
                        Commit reworded = new Commit(newHash, step.newMessage, original.content);
                        result.add(reworded);
                        System.out.printf("  reword %s → %s  '%s' → '%s'%n",
                                original.hash, newHash, original.message, step.newMessage);
                    }
                    case DROP -> {
                        System.out.printf("  drop   %s  (eliminado: '%s')%n",
                                original.hash, original.message);
                    }
                }
            }
            return result;
        }
    }

    static void printHistory(String title, List<Commit> commits) {
        System.out.println("\n" + title);
        commits.forEach(System.out::println);
    }

    public static void main(String[] args) {
        List<Commit> original = List.of(
                new Commit("abc1", "fix: corregir NPE en login",          "content-1"),
                new Commit("abc2", "wip: módulo login en progreso",        "content-2"),
                new Commit("abc3", "add: pantalla de inicio",              "content-3"),
                new Commit("abc4", "typo fix",                             "content-4"),
                new Commit("abc5", "debug: logs temporales",               "content-5"),
                new Commit("abc6", "chore: limpiar imports",               "content-6")
        );

        printHistory("Historial original (6 commits):", original);

        // Plan de rebase interactivo:
        // pick   abc1  → mantener
        // squash abc2  → fusionar con abc1
        // reword abc3  → cambiar mensaje
        // pick   abc4  → mantener
        // drop   abc5  → eliminar
        // pick   abc6  → mantener
        RebasePlan plan = new RebasePlan();
        plan.pick("abc1");
        plan.squash("abc2");
        plan.reword("abc3", "feat: módulo login refactorizado");
        plan.pick("abc4");
        plan.drop("abc5");
        plan.pick("abc6");

        System.out.println("\n--- Plan de rebase ---");
        System.out.println("  pick   abc1  fix: corregir NPE en login");
        System.out.println("  squash abc2  wip: módulo login en progreso");
        System.out.println("  reword abc3  → 'feat: módulo login refactorizado'");
        System.out.println("  pick   abc4  typo fix");
        System.out.println("  drop   abc5  debug: logs temporales");
        System.out.println("  pick   abc6  chore: limpiar imports");

        System.out.println();
        InteractiveRebase rebase = new InteractiveRebase();
        List<Commit> rebased = rebase.apply(new ArrayList<>(original), plan);

        printHistory("\nHistorial tras rebase (" + rebased.size() + " commits):", rebased);

        System.out.printf("%n%d commits originales → %d commits finales%n",
                original.size(), rebased.size());
    }
}
