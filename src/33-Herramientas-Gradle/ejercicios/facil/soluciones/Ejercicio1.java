import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;

public class Ejercicio1 {

    static class Task {
        final String name;
        final List<String> dependsOn;
        final Runnable action;
        boolean failed = false;

        Task(String name, List<String> dependsOn, Runnable action) {
            this.name = name;
            this.dependsOn = dependsOn;
            this.action = action;
        }

        Task(String name, Runnable action, String... deps) {
            this(name, List.of(deps), action);
        }
    }

    static class TaskGraph {
        private final Map<String, Task> tasks = new HashMap<>();

        void register(Task task) { tasks.put(task.name, task); }

        // Ordenación topológica con Kahn
        List<Task> topologicalOrder() {
            Map<String, Integer> inDegree = new HashMap<>();
            Map<String, List<String>> dependents = new HashMap<>();

            tasks.forEach((name, t) -> {
                inDegree.put(name, 0);
                dependents.put(name, new ArrayList<>());
            });

            tasks.forEach((name, t) ->
                t.dependsOn.forEach(dep -> {
                    dependents.get(dep).add(name);
                    inDegree.merge(name, 1, Integer::sum);
                })
            );

            Queue<String> ready = new LinkedList<>();
            inDegree.forEach((name, d) -> { if (d == 0) ready.add(name); });

            List<Task> ordered = new ArrayList<>();
            while (!ready.isEmpty()) {
                String current = ready.poll();
                ordered.add(tasks.get(current));
                dependents.get(current).forEach(dep -> {
                    int newDeg = inDegree.get(dep) - 1;
                    inDegree.put(dep, newDeg);
                    if (newDeg == 0) ready.add(dep);
                });
            }
            return ordered;
        }

        void execute() {
            List<Task> order = topologicalOrder();
            Set<String> failedTasks = new HashSet<>();

            System.out.println("Orden de ejecución: " +
                    order.stream().map(t -> t.name).toList());
            System.out.println();

            for (Task task : order) {
                // Verificar si alguna dependencia falló
                boolean depFailed = task.dependsOn.stream().anyMatch(failedTasks::contains);
                if (depFailed) {
                    System.out.printf("> Task :%s SKIPPED (dependencia falló)%n", task.name);
                    failedTasks.add(task.name);
                    continue;
                }
                System.out.printf("> Task :%s%n", task.name);
                try {
                    task.action.run();
                    System.out.printf("  SUCCESS%n%n");
                } catch (Exception e) {
                    System.out.printf("  FAILED: %s%n%n", e.getMessage());
                    task.failed = true;
                    failedTasks.add(task.name);
                }
            }

            long failures = order.stream().filter(t -> t.failed).count();
            System.out.println(failures == 0 ? "BUILD SUCCESSFUL" : "BUILD FAILED (" + failures + " task(s) failed)");
        }
    }

    public static void main(String[] args) {
        TaskGraph graph = new TaskGraph();

        graph.register(new Task("compileJava", () ->
            System.out.println("  Compilando fuentes Java...")));
        graph.register(new Task("processResources", () ->
            System.out.println("  Procesando recursos...")));
        graph.register(new Task("classes", () ->
            System.out.println("  Preparando clases..."),
            "compileJava", "processResources"));
        graph.register(new Task("test", () ->
            System.out.println("  Ejecutando tests..."),
            "classes"));
        graph.register(new Task("jar", () ->
            System.out.println("  Generando JAR..."),
            "classes"));
        graph.register(new Task("assemble", () ->
            System.out.println("  Ensamblando artefactos..."),
            "jar"));

        System.out.println("=== Gradle build (normal) ===");
        System.out.println();
        graph.execute();

        // Demo con fallo
        System.out.println();
        System.out.println("=== Gradle build (con fallo en 'classes') ===");
        System.out.println();
        TaskGraph graph2 = new TaskGraph();
        graph2.register(new Task("compileJava", () -> System.out.println("  Compilando...")));
        graph2.register(new Task("classes", () -> {
            System.out.println("  Preparando clases...");
            throw new RuntimeException("Error de compilación: sintaxis inválida");
        }, "compileJava"));
        graph2.register(new Task("test", () -> System.out.println("  Tests..."), "classes"));
        graph2.register(new Task("jar",  () -> System.out.println("  JAR..."), "classes"));
        graph2.execute();
    }
}
