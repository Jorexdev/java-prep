import java.util.*;
import java.util.concurrent.*;

public class Ejercicio3 {

    static class Task {
        final String name;
        final List<String> dependsOn;
        final long durationMs;

        Task(String name, long durationMs, String... deps) {
            this.name = name;
            this.durationMs = durationMs;
            this.dependsOn = Arrays.asList(deps);
        }
    }

    static long runSequential(List<Task> tasks) {
        long start = System.currentTimeMillis();
        for (Task t : tasks) {
            System.out.printf("  [sequential] :%s%n", t.name);
            try { Thread.sleep(t.durationMs); } catch (InterruptedException e) {}
        }
        return System.currentTimeMillis() - start;
    }

    static long runParallel(List<Task> tasks) throws Exception {
        Map<String, Task> byName = new HashMap<>();
        for (Task t : tasks) byName.put(t.name, t);

        // Topological sort to find independent batches (levels)
        Map<String, Integer> inDegree = new HashMap<>();
        for (Task t : tasks) inDegree.put(t.name, t.dependsOn.size());

        List<List<Task>> levels = new ArrayList<>();
        Set<String> done = new HashSet<>();

        while (done.size() < tasks.size()) {
            List<Task> ready = new ArrayList<>();
            for (Task t : tasks) {
                if (!done.contains(t.name) && done.containsAll(t.dependsOn)) {
                    ready.add(t);
                }
            }
            levels.add(ready);
            for (Task t : ready) done.add(t.name);
        }

        long start = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (List<Task> level : levels) {
            List<Future<?>> futures = new ArrayList<>();
            for (Task t : level) {
                final Task task = t;
                futures.add(pool.submit(() -> {
                    System.out.printf("  [parallel  ] :%s (thread=%s)%n",
                        task.name, Thread.currentThread().getName());
                    try { Thread.sleep(task.durationMs); } catch (InterruptedException e) {}
                }));
            }
            for (Future<?> f : futures) f.get(); // esperar el nivel antes de pasar al siguiente
        }
        pool.shutdown();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws Exception {
        // 8 tareas: 4 independientes (compileJava, processResources, compileTestJava, checkstyle)
        // + 4 que dependen de ellas
        List<Task> tasks = List.of(
            new Task("compileJava",       100),
            new Task("processResources",  100),
            new Task("compileTestJava",   100),
            new Task("checkstyle",        100),
            new Task("classes",            10, "compileJava", "processResources"),
            new Task("test",               50, "classes", "compileTestJava"),
            new Task("spotbugs",           80, "classes"),
            new Task("jar",                20, "classes")
        );

        System.out.println("=== Ejecución SECUENCIAL ===");
        long seqMs = runSequential(tasks);

        System.out.println("\n=== Ejecución PARALELA (--parallel) ===");
        long parMs = runParallel(tasks);

        System.out.println("\n=== Comparación ===");
        System.out.printf("  Secuencial: %4d ms%n", seqMs);
        System.out.printf("  Paralela:   %4d ms%n", parMs);
        System.out.printf("  Speedup:    %.1fx%n", (double) seqMs / parMs);
        System.out.println("\n  Las 4 tareas del primer nivel se ejecutaron en paralelo.");
    }
}
