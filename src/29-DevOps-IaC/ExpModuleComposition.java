import java.util.*;

public class ExpModuleComposition {

    static class TfModule {
        private final String name;
        private final Map<String, String> inputs;
        private final Map<String, String> outputs;   // populated after apply
        private final List<String> dependsOn;        // module names this one needs first

        TfModule(String name, Map<String, String> inputs, List<String> dependsOn) {
            this.name = name;
            this.inputs = new LinkedHashMap<>(inputs);
            this.outputs = new LinkedHashMap<>();
            this.dependsOn = dependsOn;
        }

        void apply(Map<String, TfModule> moduleMap) {
            System.out.printf("%n  [MODULE: %s]%n", name);
            System.out.printf("    inputs:    %s%n", inputs);

            // Simulate applying the module and producing outputs
            switch (name) {
                case "vpc" -> {
                    outputs.put("vpc_id",      "vpc-" + Integer.toHexString(name.hashCode()));
                    outputs.put("vpc_cidr",    inputs.getOrDefault("cidr", "10.0.0.0/16"));
                }
                case "subnet" -> {
                    String vpcId = resolve("vpc_id", dependsOn, moduleMap);
                    outputs.put("subnet_id",   "subnet-" + Integer.toHexString(name.hashCode()));
                    outputs.put("vpc_id",      vpcId);
                }
                case "ec2" -> {
                    String subnetId = resolve("subnet_id", dependsOn, moduleMap);
                    outputs.put("instance_id", "i-" + Integer.toHexString(name.hashCode()));
                    outputs.put("subnet_id",   subnetId);
                    outputs.put("public_ip",   "54.72." + (name.hashCode() % 256) + ".1");
                }
            }
            System.out.printf("    outputs:   %s%n", outputs);
        }

        // Find an output value from any of the declared dependencies
        private String resolve(String key, List<String> deps, Map<String, TfModule> moduleMap) {
            for (String dep : deps) {
                TfModule m = moduleMap.get(dep);
                if (m != null && m.outputs.containsKey(key)) {
                    return m.outputs.get(key);
                }
            }
            return "<unresolved:" + key + ">";
        }

        String getName()                  { return name; }
        Map<String, String> getOutputs()  { return outputs; }
        List<String> getDependsOn()       { return dependsOn; }
    }

    // Resolves apply order with topological sort
    static class ModuleGraph {
        private final Map<String, TfModule> modules = new LinkedHashMap<>();

        void add(TfModule m) { modules.put(m.getName(), m); }

        List<String> resolveOrder() {
            Map<String, Integer> inDegree = new HashMap<>();
            Map<String, List<String>> dependents = new HashMap<>();

            for (String name : modules.keySet()) {
                inDegree.put(name, 0);
                dependents.put(name, new ArrayList<>());
            }
            for (TfModule m : modules.values()) {
                for (String dep : m.getDependsOn()) {
                    inDegree.merge(m.getName(), 1, Integer::sum);
                    dependents.computeIfAbsent(dep, k -> new ArrayList<>()).add(m.getName());
                }
            }

            Queue<String> queue = new ArrayDeque<>();
            for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
                if (e.getValue() == 0) queue.add(e.getKey());
            }

            List<String> order = new ArrayList<>();
            while (!queue.isEmpty()) {
                String cur = queue.poll();
                order.add(cur);
                for (String dep : dependents.getOrDefault(cur, Collections.emptyList())) {
                    if (inDegree.merge(dep, -1, Integer::sum) == 0) queue.add(dep);
                }
            }
            return order;
        }

        void apply() {
            List<String> order = resolveOrder();
            System.out.println("  Orden de apply: " + order);
            System.out.println("─".repeat(55));
            for (String name : order) {
                modules.get(name).apply(modules);
            }
        }

        Map<String, TfModule> all() { return modules; }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(55));
        System.out.println("  TERRAFORM MODULE COMPOSITION — simulación");
        System.out.println("═".repeat(55));

        TfModule vpc = new TfModule("vpc",
                Map.of("cidr", "10.0.0.0/16", "region", "eu-west-1"),
                Collections.emptyList());

        // subnet receives vpc_id as implicit input via depends_on
        TfModule subnet = new TfModule("subnet",
                Map.of("az", "eu-west-1a"),
                List.of("vpc"));

        // ec2 receives subnet_id as implicit input via depends_on
        TfModule ec2 = new TfModule("ec2",
                Map.of("type", "t3.micro", "ami", "ami-abc123"),
                List.of("subnet"));

        ModuleGraph graph = new ModuleGraph();
        graph.add(vpc);
        graph.add(subnet);
        graph.add(ec2);

        System.out.println("\n[Aplicando módulos en orden resuelto]");
        graph.apply();

        System.out.println("\n[Outputs por módulo]");
        System.out.println("─".repeat(55));
        for (TfModule m : graph.all().values()) {
            System.out.printf("  module.%-8s → %s%n", m.getName(), m.getOutputs());
        }

        System.out.println("\n── Conclusión ──");
        System.out.println("  Los outputs de un módulo son los inputs del siguiente.");
        System.out.println("  El grafo de dependencias determina el orden de ejecución.");
    }
}
