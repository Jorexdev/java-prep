import java.util.*;
import java.util.concurrent.*;

public class Ejercicio4 {

    // Nodo del grafo del pipeline
    static class StageNode {
        String name;
        List<StageNode> dependencies = new ArrayList<>();
        List<StageNode> dependents   = new ArrayList<>();
        boolean parallel = false;  // true = pertenece a un grupo paralelo
        String group;              // nombre del grupo paralelo

        StageNode(String name) { this.name = name; }
    }

    // Builder del DSL
    static class PipelineBuilder {
        String pipelineName;
        List<StageNode> nodes      = new ArrayList<>();
        Map<String, StageNode> map = new LinkedHashMap<>();
        StageNode lastNode         = null;

        PipelineBuilder(String name) { this.pipelineName = name; }

        static PipelineBuilder named(String name) { return new PipelineBuilder(name); }

        StageNode getOrCreate(String name) {
            return map.computeIfAbsent(name, n -> {
                StageNode node = new StageNode(n);
                nodes.add(node);
                return node;
            });
        }

        // Añadir un stage secuencial
        PipelineBuilder stage(String name) {
            StageNode node = getOrCreate(name);
            if (lastNode != null) {
                node.dependencies.add(lastNode);
                lastNode.dependents.add(node);
            }
            lastNode = node;
            return this;
        }

        // Alias para stage (claridad en la cadena)
        PipelineBuilder then(String name) { return stage(name); }

        // Añadir un grupo de stages paralelos
        PipelineBuilder parallel(String... names) {
            String groupName = String.join("+", names);
            // Nodo "puerta de entrada" al grupo
            StageNode barrier = new StageNode("__barrier__" + groupName);
            if (lastNode != null) {
                barrier.dependencies.add(lastNode);
                lastNode.dependents.add(barrier);
            }
            nodes.add(barrier);

            // Nodo "puerta de salida" del grupo
            StageNode exit = new StageNode("__exit__" + groupName);
            nodes.add(exit);
            map.put("__exit__" + groupName, exit);

            for (String n : names) {
                StageNode node = getOrCreate(n);
                node.parallel = true;
                node.group    = groupName;
                node.dependencies.add(barrier);
                barrier.dependents.add(node);
                node.dependents.add(exit);
                exit.dependencies.add(node);
            }
            lastNode = exit;
            return this;
        }

        Pipeline build() {
            return new Pipeline(pipelineName, nodes, map);
        }
    }

    static class Pipeline {
        String name;
        List<StageNode> nodes;
        Map<String, StageNode> map;

        Pipeline(String name, List<StageNode> nodes, Map<String, StageNode> map) {
            this.name  = name;
            this.nodes = nodes;
            this.map   = map;
        }

        void printGraph() {
            System.out.printf("Pipeline '%s' — grafo de stages:%n", name);
            for (StageNode n : nodes) {
                if (n.name.startsWith("__")) continue;
                String deps = n.dependencies.stream()
                        .filter(d -> !d.name.startsWith("__"))
                        .map(d -> d.name)
                        .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);
                System.out.printf("  %-14s deps=[%s] parallel=%b%n",
                        n.name, deps, n.parallel);
            }
        }

        void execute() throws InterruptedException, ExecutionException {
            System.out.printf("%n=== Ejecutando pipeline '%s' ===%n%n", name);

            Set<String> done = new HashSet<>();
            ExecutorService pool = Executors.newCachedThreadPool();

            // Ejecutar por capas topológicas
            while (done.size() < nodes.size()) {
                List<StageNode> ready = new ArrayList<>();
                for (StageNode n : nodes) {
                    if (done.contains(n.name)) continue;
                    boolean depsOk = n.dependencies.stream()
                            .allMatch(d -> done.contains(d.name));
                    if (depsOk) ready.add(n);
                }

                if (ready.isEmpty()) break;

                List<Future<?>> futures = new ArrayList<>();
                for (StageNode n : ready) {
                    futures.add(pool.submit(() -> {
                        if (!n.name.startsWith("__")) {
                            System.out.printf("  [%-14s] RUNNING  (thread=%s)%n",
                                    n.name, Thread.currentThread().getName());
                            try { Thread.sleep(80); } catch (InterruptedException e) {}
                            System.out.printf("  [%-14s] DONE%n", n.name);
                        }
                        synchronized (done) { done.add(n.name); }
                    }));
                }
                for (Future<?> f : futures) f.get();
            }
            pool.shutdown();
            System.out.println("\nPipeline completado.");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Pipeline DSL Demo ===\n");

        Pipeline pipeline = PipelineBuilder
                .named("ci")
                .stage("compile")
                .then("test")
                .parallel("lint", "security")
                .then("deploy")
                .build();

        pipeline.printGraph();
        pipeline.execute();
    }
}
