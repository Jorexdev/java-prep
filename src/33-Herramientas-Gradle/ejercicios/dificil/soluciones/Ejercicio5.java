import java.util.*;

public class Ejercicio5 {

    static class CacheEntry {
        int inputHash;
        String outputs;
        long timestamp;
        long executionMs;  // tiempo de ejecución original

        CacheEntry(int inputHash, String outputs, long timestamp, long executionMs) {
            this.inputHash   = inputHash;
            this.outputs     = outputs;
            this.timestamp   = timestamp;
            this.executionMs = executionMs;
        }
    }

    static class BuildTask {
        String name;
        Set<String> inputs;
        long executionMs;  // tiempo simulado de ejecución (ms)

        BuildTask(String name, Set<String> inputs, long executionMs) {
            this.name        = name;
            this.inputs      = new LinkedHashSet<>(inputs);
            this.executionMs = executionMs;
        }

        int inputHash() {
            return inputs.hashCode();
        }

        String execute() {
            // Simula la ejecución y devuelve los outputs como string
            return "output-of-" + name + "-" + Integer.toHexString(inputHash());
        }
    }

    static class BuildCache {
        // taskName → CacheEntry
        private final Map<String, CacheEntry> store = new LinkedHashMap<>();

        boolean isCacheHit(BuildTask task) {
            CacheEntry entry = store.get(task.name);
            return entry != null && entry.inputHash == task.inputHash();
        }

        String getOutput(BuildTask task) {
            return store.get(task.name).outputs;
        }

        void storeResult(BuildTask task, String outputs) {
            store.put(task.name, new CacheEntry(
                    task.inputHash(), outputs,
                    System.currentTimeMillis(), task.executionMs));
        }
    }

    static class BuildRunner {
        BuildCache cache = new BuildCache();
        List<BuildTask> tasks;

        BuildRunner(List<BuildTask> tasks) {
            this.tasks = tasks;
        }

        void run(String buildLabel) {
            System.out.printf("%n=== Build: %s ===%n%n", buildLabel);

            int hits = 0, misses = 0;
            long totalSaved = 0, totalReal = 0;

            for (BuildTask task : tasks) {
                if (cache.isCacheHit(task)) {
                    String output = cache.getOutput(task);
                    System.out.printf("  %-20s CACHE HIT  (hash=%08x) → output='%s'%n",
                            task.name, task.inputHash(), output);
                    totalSaved += task.executionMs;
                    hits++;
                } else {
                    // Simula ejecución (sin Thread.sleep para no ralentizar el test)
                    String output = task.execute();
                    cache.storeResult(task, output);
                    System.out.printf("  %-20s CACHE MISS (hash=%08x) → ejecutado en %dms%n",
                            task.name, task.inputHash(), task.executionMs);
                    totalReal += task.executionMs;
                    misses++;
                }
            }

            long totalIfNoCache = tasks.stream().mapToLong(t -> t.executionMs).sum();
            System.out.printf("%n  HITS: %d | MISSES: %d%n", hits, misses);
            System.out.printf("  Tiempo real de ejecución : %4dms%n", totalReal);
            System.out.printf("  Tiempo ahorrado por cache: %4dms%n", totalSaved);
            System.out.printf("  Sin cache sería           : %4dms%n", totalIfNoCache);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Gradle Build Cache: hash de inputs/outputs ===");

        List<BuildTask> tasks = List.of(
                new BuildTask("compileJava",       Set.of("src/Main.java", "src/Service.java"),         300),
                new BuildTask("compileTestJava",   Set.of("test/MainTest.java"),                         150),
                new BuildTask("processResources",  Set.of("resources/application.properties"),           80),
                new BuildTask("test",              Set.of("build/classes/", "build/test-results/"),      400),
                new BuildTask("jar",               Set.of("build/classes/", "build/resources/"),         120),
                new BuildTask("generateDocs",      Set.of("src/Main.java", "docs/templates/"),           200)
        );

        BuildRunner runner = new BuildRunner(tasks);

        // Primer build: todos MISS
        runner.run("Primer build (caché vacía)");

        // Segundo build: todos los inputs iguales → todos HIT
        runner.run("Segundo build (sin cambios)");

        // Modificar inputs de 2 tareas
        System.out.println("\n--- Cambios antes del tercer build ---");
        System.out.println("  Modificado: src/Service.java (cambia compileJava)");
        System.out.println("  Modificado: resources/application.properties (cambia processResources)");

        // Crear nuevas instancias con inputs modificados para simular el cambio
        List<BuildTask> tasksV2 = new ArrayList<>(tasks);
        tasksV2.set(0, new BuildTask("compileJava",
                Set.of("src/Main.java", "src/Service.java:v2"), 300));
        tasksV2.set(2, new BuildTask("processResources",
                Set.of("resources/application.properties:v2"), 80));

        BuildRunner runner2 = new BuildRunner(tasks);
        // Inyectar la caché existente del primer runner
        runner2.cache = runner.cache;
        runner2.tasks = tasksV2;

        // Tercer build: 4 HITS, 2 MISS
        runner2.run("Tercer build (2 inputs modificados → 2 MISS, 4 HITS)");
    }
}
