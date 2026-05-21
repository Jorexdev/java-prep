import java.util.*;
import java.util.concurrent.*;

public class Ejercicio6 {

    enum StageStatus { PENDING, RUNNING, DONE }

    static class Stage {
        String name;
        List<String> dependsOn;
        StageStatus status = StageStatus.PENDING;

        Stage(String name, String... dependsOn) {
            this.name      = name;
            this.dependsOn = Arrays.asList(dependsOn);
        }

        void run() {
            status = StageStatus.RUNNING;
            System.out.printf("  [%5d ms] %-14s STARTED%n",
                    System.currentTimeMillis() % 100000, name);
            try {
                Thread.sleep(100); // simular trabajo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            status = StageStatus.DONE;
            System.out.printf("  [%5d ms] %-14s DONE%n",
                    System.currentTimeMillis() % 100000, name);
        }
    }

    static List<List<Stage>> topologicalLayers(List<Stage> stages) {
        Map<String, Stage> byName = new LinkedHashMap<>();
        stages.forEach(s -> byName.put(s.name, s));

        Map<String, Set<String>> deps = new LinkedHashMap<>();
        stages.forEach(s -> deps.put(s.name, new HashSet<>(s.dependsOn)));

        List<List<Stage>> layers = new ArrayList<>();
        Set<String> completed   = new HashSet<>();

        while (completed.size() < stages.size()) {
            List<Stage> layer = new ArrayList<>();
            for (Stage s : stages) {
                if (completed.contains(s.name)) continue;
                if (completed.containsAll(deps.get(s.name))) layer.add(s);
            }
            if (layer.isEmpty()) throw new IllegalStateException("Ciclo detectado");
            layers.add(layer);
            layer.forEach(s -> completed.add(s.name));
        }
        return layers;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Stage Dependencies — Parallel Execution ===\n");

        List<Stage> stages = List.of(
                new Stage("checkout"),
                new Stage("compile",  "checkout"),
                new Stage("lint",     "checkout"),
                new Stage("test",     "compile"),
                new Stage("security", "compile"),
                new Stage("package",  "test", "security", "lint"),
                new Stage("deploy",   "package")
        );

        List<List<Stage>> layers = topologicalLayers(stages);

        System.out.println("Capas topológicas:");
        for (int i = 0; i < layers.size(); i++) {
            System.out.printf("  Capa %d: %s%n", i + 1,
                    layers.get(i).stream().map(s -> s.name).toList());
        }

        System.out.println("\nEjecución (stages en la misma capa corren en paralelo):");

        ExecutorService pool = Executors.newCachedThreadPool();
        for (List<Stage> layer : layers) {
            List<Future<?>> futures = new ArrayList<>();
            for (Stage s : layer) {
                futures.add(pool.submit(s::run));
            }
            for (Future<?> f : futures) {
                try { f.get(); } catch (ExecutionException e) { e.printStackTrace(); }
            }
        }
        pool.shutdown();

        System.out.println("\nTodos los stages completados.");
    }
}
