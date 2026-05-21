import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio5 {

    static class WorkingDirectory {
        String branchName;
        String content; // simula el estado del working directory

        WorkingDirectory(String branchName, String content) {
            this.branchName = branchName;
            this.content = content;
        }

        @Override
        public String toString() {
            return "[branch=" + branchName + ", content='" + content + "']";
        }
    }

    static class GitStash {
        private final Deque<WorkingDirectory> stack = new ArrayDeque<>();

        // Guarda el estado actual y devuelve el índice
        int push(WorkingDirectory snapshot) {
            stack.push(snapshot);
            System.out.println("Stash guardado: stash@{" + (stack.size() - 1) + "} -> " + snapshot);
            return stack.size() - 1;
        }

        // Restaura el último estado guardado
        WorkingDirectory pop() {
            if (stack.isEmpty()) throw new IllegalStateException("No hay stash entries");
            WorkingDirectory restored = stack.pop();
            System.out.println("Stash restaurado: " + restored);
            return restored;
        }

        // Muestra todos los estados guardados
        void list() {
            if (stack.isEmpty()) {
                System.out.println("(no stash entries)");
                return;
            }
            int i = 0;
            for (WorkingDirectory wd : stack) {
                System.out.println("stash@{" + i + "}: " + wd);
                i++;
            }
        }

        int size() { return stack.size(); }
    }

    public static void main(String[] args) {
        GitStash stash = new GitStash();

        // Estado inicial: trabajando en feature con cambios sin commitear
        WorkingDirectory feature = new WorkingDirectory("feature/login", "Cambios sin terminar: login form");
        System.out.println("Estado actual: " + feature);
        System.out.println();

        // Guardar cambios en stash
        System.out.println("=== git stash push ===");
        stash.push(feature);
        System.out.println();

        // Guardar otro snapshot (trabajo adicional)
        WorkingDirectory feature2 = new WorkingDirectory("feature/login", "Otro cambio: add validation");
        stash.push(feature2);
        System.out.println();

        // Ver todos los stash entries
        System.out.println("=== git stash list ===");
        stash.list();
        System.out.println();

        // Simular cambio de rama
        WorkingDirectory hotfixBranch = new WorkingDirectory("hotfix/critical", "Working directory limpio");
        System.out.println("=== Cambiando a rama hotfix ===");
        System.out.println("Working dir ahora: " + hotfixBranch);
        System.out.println();

        // ... trabajar en hotfix ...
        System.out.println("(Trabajando en hotfix...)");
        System.out.println();

        // Restaurar el último stash
        System.out.println("=== git stash pop ===");
        WorkingDirectory restored = stash.pop();
        System.out.println("Volvemos a feature con: " + restored);
        System.out.println();

        System.out.println("=== git stash list (después del pop) ===");
        stash.list();
    }
}
