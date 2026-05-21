import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ejercicio4 {

    enum Operation { PICK, SQUASH, REWORD, DROP }

    static class Commit {
        final String hash;
        final String message;

        Commit(String hash, String message) {
            this.hash = hash;
            this.message = message;
        }

        @Override
        public String toString() {
            return hash.substring(0, 7) + " " + message;
        }
    }

    static class RebaseInstruction {
        final Operation operation;
        final String commitHash;
        final String newMessage; // solo para REWORD

        RebaseInstruction(Operation op, String hash) {
            this.operation = op;
            this.commitHash = hash;
            this.newMessage = null;
        }

        RebaseInstruction(Operation op, String hash, String newMessage) {
            this.operation = op;
            this.commitHash = hash;
            this.newMessage = newMessage;
        }
    }

    static String newHash() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 7);
    }

    static List<Commit> interactiveRebase(List<Commit> original, List<RebaseInstruction> plan) {
        System.out.println("=== Aplicando plan de rebase interactivo ===");
        List<Commit> result = new ArrayList<>();

        for (RebaseInstruction inst : plan) {
            Commit target = original.stream()
                    .filter(c -> c.hash.startsWith(inst.commitHash))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Commit no encontrado: " + inst.commitHash));

            switch (inst.operation) {
                case PICK -> {
                    Commit newCommit = new Commit(newHash(), target.message);
                    result.add(newCommit);
                    System.out.printf("pick   %s -> %s (%s)%n",
                            target.hash.substring(0, 7), newCommit.hash.substring(0, 7), target.message);
                }
                case SQUASH -> {
                    if (result.isEmpty()) throw new IllegalStateException("No hay commit previo para squash");
                    Commit prev = result.remove(result.size() - 1);
                    String squashedMsg = prev.message + "\n\n" + target.message;
                    Commit squashed = new Commit(newHash(), squashedMsg);
                    result.add(squashed);
                    System.out.printf("squash %s + %s -> %s%n",
                            prev.hash.substring(0, 7), target.hash.substring(0, 7), squashed.hash.substring(0, 7));
                }
                case REWORD -> {
                    String msg = inst.newMessage != null ? inst.newMessage : target.message;
                    Commit rewrote = new Commit(newHash(), msg);
                    result.add(rewrote);
                    System.out.printf("reword %s -> %s (nuevo mensaje: '%s')%n",
                            target.hash.substring(0, 7), rewrote.hash.substring(0, 7), msg);
                }
                case DROP -> {
                    System.out.printf("drop   %s '%s'%n",
                            target.hash.substring(0, 7), target.message);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<Commit> commits = new ArrayList<>(List.of(
            new Commit("aaaa001", "WIP: start feature"),
            new Commit("aaaa002", "WIP: continue feature"),
            new Commit("aaaa003", "Add unit tests"),
            new Commit("aaaa004", "debug log (remove me)"),
            new Commit("aaaa005", "Finalize feature implementation")
        ));

        System.out.println("=== Historial ANTES del rebase interactivo ===");
        commits.forEach(System.out::println);
        System.out.println();

        // Plan de rebase:
        // - squash WIP commits 1 y 2
        // - pick los tests
        // - drop el commit de debug
        // - reword el commit final
        List<RebaseInstruction> plan = List.of(
            new RebaseInstruction(Operation.PICK,   "aaaa001"),
            new RebaseInstruction(Operation.SQUASH, "aaaa002"),
            new RebaseInstruction(Operation.PICK,   "aaaa003"),
            new RebaseInstruction(Operation.DROP,   "aaaa004"),
            new RebaseInstruction(Operation.REWORD, "aaaa005", "feat: implement user feature with tests")
        );

        System.out.println();
        List<Commit> rebased = interactiveRebase(commits, plan);

        System.out.println();
        System.out.println("=== Historial DESPUÉS del rebase interactivo ===");
        rebased.forEach(c -> {
            System.out.println(c.hash.substring(0, 7));
            // Si el mensaje tiene saltos de línea, mostrarlos con indentación
            for (String line : c.message.split("\n")) {
                System.out.println("    " + line);
            }
        });
        System.out.println();
        System.out.printf("Antes: %d commits -> Después: %d commits%n", commits.size(), rebased.size());
    }
}
