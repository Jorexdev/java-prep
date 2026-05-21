import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio1 {

    static class GreetingExtension {
        String message = "Hello from plugin!";
    }

    static class Task {
        final String name;
        final Runnable action;
        Task(String name, Runnable action) { this.name = name; this.action = action; }
        void execute() { System.out.println("  > Task :" + name); action.run(); }
    }

    static class Project {
        final String name;
        final Map<String, List<String>> configurations = new HashMap<>();
        final List<Task> tasks = new ArrayList<>();
        final Map<String, Object> extensions = new HashMap<>();

        Project(String name) {
            this.name = name;
            configurations.put("implementation", new ArrayList<>());
            configurations.put("testImplementation", new ArrayList<>());
            configurations.put("runtimeOnly", new ArrayList<>());
        }

        void addDependency(String config, String dep) {
            configurations.computeIfAbsent(config, k -> new ArrayList<>()).add(dep);
        }

        void registerTask(String name, Runnable action) {
            tasks.add(new Task(name, action));
        }

        <T> T extensions(String name, T ext) {
            extensions.put(name, ext);
            return ext;
        }

        void printState() {
            System.out.println("  Project: " + name);
            System.out.println("  Tasks:   " + tasks.stream().map(t -> t.name).toList());
            configurations.forEach((cfg, deps) -> {
                if (!deps.isEmpty()) System.out.println("  " + cfg + ": " + deps);
            });
            extensions.forEach((k, v) -> {
                if (v instanceof GreetingExtension g)
                    System.out.println("  greeting.message: " + g.message);
            });
        }
    }

    interface GradlePlugin {
        void apply(Project project);
    }

    static class GreetingPlugin implements GradlePlugin {
        @Override
        public void apply(Project project) {
            // 1. Registrar tarea "hello"
            project.registerTask("hello", () ->
                System.out.println("  Hello from plugin!"));

            // 2. Añadir dependencia junit a testImplementation
            project.addDependency("testImplementation", "junit:junit:4.13");

            // 3. Configurar extensión greeting
            GreetingExtension ext = project.extensions("greeting", new GreetingExtension());
            ext.message = "Hello from GreetingPlugin!";
        }
    }

    public static void main(String[] args) {
        Project project = new Project("my-app");

        System.out.println("=== Antes de aplicar el plugin ===");
        project.printState();

        System.out.println("\n--- Aplicando GreetingPlugin ---");
        new GreetingPlugin().apply(project);

        System.out.println("\n=== Después de aplicar el plugin ===");
        project.printState();

        System.out.println("\n--- Ejecutando tarea :hello ---");
        project.tasks.stream()
            .filter(t -> t.name.equals("hello"))
            .findFirst()
            .ifPresent(Task::execute);
    }
}
