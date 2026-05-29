import java.util.*;

public class Ejercicio6 {

    enum TaskResult { EXECUTED, UP_TO_DATE, FAILED }

    static class GradleTask {
        String name;
        Set<String> inputs;
        Set<String> outputs;
        List<String> dependsOn;

        // hash de los inputs de la última ejecución exitosa
        int cachedInputHash = -1;

        GradleTask(String name, Set<String> inputs, Set<String> outputs,
                   List<String> dependsOn) {
            this.name      = name;
            this.inputs    = new LinkedHashSet<>(inputs);
            this.outputs   = new LinkedHashSet<>(outputs);
            this.dependsOn = dependsOn;
        }

        int currentInputHash() {
            return inputs.hashCode();
        }

        boolean isUpToDate() {
            return cachedInputHash == currentInputHash();
        }
    }

    static class TaskRunner {
        Map<String, GradleTask> tasks = new LinkedHashMap<>();

        void register(GradleTask task) {
            tasks.put(task.name, task);
        }

        List<GradleTask> topologicalOrder() {
            Map<String, Integer> inDegree = new LinkedHashMap<>();
            Map<String, List<String>> dependents = new LinkedHashMap<>();

            tasks.keySet().forEach(n -> {
                inDegree.put(n, 0);
                dependents.put(n, new ArrayList<>());
            });

            for (GradleTask t : tasks.values()) {
                for (String dep : t.dependsOn) {
                    if (tasks.containsKey(dep)) {
                        inDegree.merge(t.name, 1, Integer::sum);
                        dependents.get(dep).add(t.name);
                    }
                }
            }

            Queue<String> queue = new ArrayDeque<>();
            inDegree.forEach((name, deg) -> { if (deg == 0) queue.add(name); });

            List<GradleTask> order = new ArrayList<>();
            while (!queue.isEmpty()) {
                String cur = queue.poll();
                order.add(tasks.get(cur));
                for (String dep : dependents.get(cur)) {
                    inDegree.merge(dep, -1, Integer::sum);
                    if (inDegree.get(dep) == 0) queue.add(dep);
                }
            }
            return order;
        }

        void run(String buildLabel) {
            System.out.printf("%n=== Build: %s ===%n", buildLabel);
            List<GradleTask> order = topologicalOrder();

            int executed = 0, upToDate = 0;
            for (GradleTask task : order) {
                if (task.isUpToDate()) {
                    System.out.printf("  %-20s UP-TO-DATE (inputs hash=%d)%n",
                            ":" + task.name, task.cachedInputHash);
                    upToDate++;
                } else {
                    System.out.printf("  %-20s EXECUTED   (inputs hash=%d → %d)%n",
                            ":" + task.name, task.cachedInputHash, task.currentInputHash());
                    task.cachedInputHash = task.currentInputHash();
                    executed++;
                }
            }

            System.out.printf("%n  %d tareas ejecutadas, %d UP-TO-DATE%n", executed, upToDate);
        }
    }

    public static void main(String[] args) {
        // Definición de 5 tareas con sus inputs, outputs y dependencias
        GradleTask compileJava = new GradleTask("compileJava",
                new LinkedHashSet<>(Set.of("src/Main.java", "src/Service.java")),
                Set.of("build/classes/"),
                List.of());

        GradleTask processResources = new GradleTask("processResources",
                new LinkedHashSet<>(Set.of("src/resources/application.properties")),
                Set.of("build/resources/"),
                List.of());

        GradleTask test = new GradleTask("test",
                new LinkedHashSet<>(Set.of("src/test/MainTest.java")),
                Set.of("build/test-results/"),
                List.of("compileJava", "processResources"));

        GradleTask jar = new GradleTask("jar",
                new LinkedHashSet<>(Set.of("build/classes/")),
                Set.of("build/libs/app.jar"),
                List.of("compileJava"));

        GradleTask assemble = new GradleTask("assemble",
                new LinkedHashSet<>(Set.of("build/libs/app.jar")),
                Set.of("build/distributions/"),
                List.of("jar"));

        TaskRunner runner = new TaskRunner();
        runner.register(compileJava);
        runner.register(processResources);
        runner.register(test);
        runner.register(jar);
        runner.register(assemble);

        // Primer build: todos se ejecutan (no hay caché)
        runner.run("Primer build (sin caché)");

        // Segundo build: sin cambios → todos UP-TO-DATE
        runner.run("Segundo build (sin cambios)");

        // Modificar los inputs de compileJava y processResources
        System.out.println("\n--- Simulando cambios en código fuente ---");
        System.out.println("  Modificado: src/Main.java");
        System.out.println("  Modificado: src/resources/application.properties");

        compileJava.inputs.add("src/Main.java:modificado-v2");
        processResources.inputs.add("src/resources/application.properties:modificado-v2");

        // Tercer build: solo se reejecutar las tareas con inputs cambiados
        // (compileJava, processResources y sus dependientes: test, jar, assemble)
        runner.run("Tercer build (inputs de compile y resources cambiados)");
    }
}
