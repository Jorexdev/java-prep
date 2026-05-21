import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Ejercicio2 {

    static class TaskSnapshot {
        final String inputsHash;
        final String outputsHash;

        TaskSnapshot(String inputsHash, String outputsHash) {
            this.inputsHash = inputsHash;
            this.outputsHash = outputsHash;
        }
    }

    static class Task {
        final String name;
        final String inputsHash;
        final String outputsHash;
        final Runnable action;

        Task(String name, String inputsHash, String outputsHash, Runnable action) {
            this.name = name;
            this.inputsHash = inputsHash;
            this.outputsHash = outputsHash;
            this.action = action;
        }
    }

    static class IncrementalBuild {
        // Snapshots del último build: taskName -> snapshot
        private final Map<String, TaskSnapshot> snapshots = new HashMap<>();

        boolean isUpToDate(Task task) {
            TaskSnapshot last = snapshots.get(task.name);
            if (last == null) return false;
            return Objects.equals(last.inputsHash, task.inputsHash)
                    && Objects.equals(last.outputsHash, task.outputsHash);
        }

        void execute(List<Task> tasks) {
            System.out.println();
            for (Task task : tasks) {
                if (isUpToDate(task)) {
                    System.out.printf("> Task :%s UP-TO-DATE%n", task.name);
                } else {
                    System.out.printf("> Task :%s%n", task.name);
                    task.action.run();
                    // Guardar snapshot del build actual
                    snapshots.put(task.name, new TaskSnapshot(task.inputsHash, task.outputsHash));
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        List<Task> tasks = List.of(
            new Task("compileJava",  "hash-src-v1",  "hash-classes-v1",
                () -> System.out.println("  Compilando fuentes...")),
            new Task("processResources", "hash-res-v1", "hash-out-res-v1",
                () -> System.out.println("  Procesando recursos...")),
            new Task("test",         "hash-test-v1", "hash-test-results-v1",
                () -> System.out.println("  Ejecutando tests...")),
            new Task("jar",          "hash-classes-v1", "hash-jar-v1",
                () -> System.out.println("  Generando JAR..."))
        );

        IncrementalBuild build = new IncrementalBuild();

        System.out.println("=== PRIMER BUILD (todos ejecutan) ===");
        build.execute(tasks);

        System.out.println("=== SEGUNDO BUILD (mismos inputs/outputs -> UP-TO-DATE) ===");
        build.execute(tasks);

        // Simular un cambio en los sources
        System.out.println("=== TERCER BUILD (cambio en fuentes Java) ===");
        List<Task> tasksAfterChange = List.of(
            new Task("compileJava",  "hash-src-v2",  "hash-classes-v2",  // CAMBIA
                () -> System.out.println("  Recompilando fuentes modificadas...")),
            new Task("processResources", "hash-res-v1", "hash-out-res-v1", // sin cambio
                () -> System.out.println("  Procesando recursos...")),
            new Task("test",         "hash-classes-v2", "hash-test-results-v2", // CAMBIA (dep de compile)
                () -> System.out.println("  Re-ejecutando tests...")),
            new Task("jar",          "hash-classes-v2", "hash-jar-v2",  // CAMBIA
                () -> System.out.println("  Regenerando JAR..."))
        );
        build.execute(tasksAfterChange);
    }
}
