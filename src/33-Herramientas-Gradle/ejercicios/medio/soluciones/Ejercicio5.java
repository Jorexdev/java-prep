import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Ejercicio5 {

    static class Task {
        final String name;
        final String inputHash; // hash de los inputs de configuración
        final Runnable action;

        Task(String name, String inputHash, Runnable action) {
            this.name = name;
            this.inputHash = inputHash;
            this.action = action;
        }
    }

    // Snapshot del grafo de tareas serializado
    static class CachedGraph {
        final List<String> taskNames;
        final String configHash;

        CachedGraph(List<String> taskNames, String configHash) {
            this.taskNames = taskNames;
            this.configHash = configHash;
        }
    }

    static class ConfigCache {
        private CachedGraph cachedGraph = null;

        boolean isCacheValid(String currentConfigHash) {
            return cachedGraph != null && Objects.equals(cachedGraph.configHash, currentConfigHash);
        }

        void store(List<Task> tasks, String configHash) {
            List<String> names = tasks.stream().map(t -> t.name).toList();
            cachedGraph = new CachedGraph(names, configHash);
            System.out.println("[cache] Grafo de configuración guardado en cache");
        }

        CachedGraph get() { return cachedGraph; }
    }

    static class GradleBuild {
        private final List<Task> tasks;
        private final String configHash; // hash del build.gradle + settings.gradle
        private final ConfigCache cache;

        GradleBuild(List<Task> tasks, String configHash, ConfigCache cache) {
            this.tasks = tasks;
            this.configHash = configHash;
            this.cache = cache;
        }

        void execute() {
            long startTotal = System.nanoTime();

            if (cache.isCacheValid(configHash)) {
                // Configuration cache hit
                System.out.println("[INFO] Configuration cache hit.");
                System.out.println("[INFO] Reusing configuration cache.");
                long configTime = 0; // no hay fase de configuración
                System.out.printf("[INFO] Configuration phase: SKIPPED (%d ms)%n", configTime);
            } else {
                // Fase de configuración (lenta)
                long configStart = System.nanoTime();
                System.out.println("[INFO] Calculando task graph...");
                System.out.println("[INFO]   Evaluando build.gradle...");
                System.out.println("[INFO]   Resolviendo dependencias de configuración...");
                System.out.println("[INFO]   Configurando " + tasks.size() + " tareas...");

                // Simular tiempo de configuración
                simulateWork(20);

                long configTime = (System.nanoTime() - configStart) / 1_000_000;
                System.out.printf("[INFO] Configuration phase: %d ms%n", configTime);

                // Guardar en cache
                cache.store(tasks, configHash);
            }

            System.out.println();
            // Fase de ejecución
            long execStart = System.nanoTime();
            System.out.println("[INFO] Executing tasks...");
            for (Task task : tasks) {
                System.out.printf("> Task :%s%n", task.name);
                task.action.run();
            }
            long execTime = (System.nanoTime() - execStart) / 1_000_000;
            System.out.printf("[INFO] Execution phase: %d ms%n", execTime);

            long totalTime = (System.nanoTime() - startTotal) / 1_000_000;
            System.out.printf("[INFO] Total build time: %d ms%n%n", totalTime);
        }

        private void simulateWork(long millis) {
            long end = System.nanoTime() + millis * 1_000_000L;
            while (System.nanoTime() < end) { /* busy wait */ }
        }
    }

    public static void main(String[] args) {
        ConfigCache cache = new ConfigCache();

        // Task graph del proyecto
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("compileJava",  "src-hash-abc", () -> System.out.println("  Compilando...")));
        tasks.add(new Task("test",         "test-hash-def", () -> System.out.println("  Testeando...")));
        tasks.add(new Task("jar",          "jar-hash-ghi",  () -> System.out.println("  Empaquetando...")));

        String configHash = "build-gradle-hash-12345"; // hash del build.gradle actual

        System.out.println("=== PRIMER BUILD (sin cache) ===");
        new GradleBuild(tasks, configHash, cache).execute();

        System.out.println("=== SEGUNDO BUILD (misma configuración -> cache hit) ===");
        new GradleBuild(tasks, configHash, cache).execute();

        System.out.println("=== TERCER BUILD (build.gradle modificado -> cache miss) ===");
        String newConfigHash = "build-gradle-hash-99999"; // cambió el build.gradle
        new GradleBuild(tasks, newConfigHash, cache).execute();

        System.out.println("=== CUARTO BUILD (sin cambios -> cache hit de nuevo) ===");
        new GradleBuild(tasks, newConfigHash, cache).execute();
    }
}
