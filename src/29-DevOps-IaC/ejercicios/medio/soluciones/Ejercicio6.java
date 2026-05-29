import java.util.*;

public class Ejercicio6 {

    static class TerraformModule {
        String name;
        Map<String, String> inputVars;   // varName → valor (puede venir de otro módulo)
        List<String> resources;
        Map<String, String> outputVars;  // varName → valor producido
        List<String> dependsOn;          // nombres de módulos de los que recibe inputs

        TerraformModule(String name, Map<String, String> inputVars,
                        List<String> resources, Map<String, String> outputVars) {
            this.name       = name;
            this.inputVars  = new LinkedHashMap<>(inputVars);
            this.resources  = resources;
            this.outputVars = new LinkedHashMap<>(outputVars);
            this.dependsOn  = new ArrayList<>();
        }

        // Marca que este módulo recibe outputs de otro módulo como sus inputs
        void addDependency(String moduleName) {
            dependsOn.add(moduleName);
        }

        // Inyecta valores reales de inputs (resueltos de outputs de dependencias)
        void resolveInputs(Map<String, String> resolvedValues) {
            resolvedValues.forEach((k, v) -> {
                if (inputVars.containsKey(k)) {
                    inputVars.put(k, v);
                }
            });
        }

        Map<String, String> apply() {
            System.out.printf("%n  [APPLY] módulo '%s'%n", name);
            System.out.printf("    Inputs  : %s%n", inputVars);
            System.out.printf("    Recursos: %s%n", resources);
            System.out.printf("    Outputs : %s%n", outputVars);
            return Collections.unmodifiableMap(outputVars);
        }
    }

    static class ModuleComposer {
        Map<String, TerraformModule> modules = new LinkedHashMap<>();

        void register(TerraformModule module) {
            modules.put(module.name, module);
        }

        // Ordenamiento topológico de módulos
        List<TerraformModule> topologicalOrder() {
            Map<String, Integer> inDegree = new LinkedHashMap<>();
            Map<String, List<String>> dependents = new LinkedHashMap<>();

            modules.keySet().forEach(n -> {
                inDegree.put(n, 0);
                dependents.put(n, new ArrayList<>());
            });

            for (TerraformModule m : modules.values()) {
                for (String dep : m.dependsOn) {
                    inDegree.merge(m.name, 1, Integer::sum);
                    dependents.get(dep).add(m.name);
                }
            }

            Queue<String> queue = new ArrayDeque<>();
            inDegree.forEach((name, degree) -> { if (degree == 0) queue.add(name); });

            List<TerraformModule> order = new ArrayList<>();
            while (!queue.isEmpty()) {
                String current = queue.poll();
                order.add(modules.get(current));
                for (String dependent : dependents.get(current)) {
                    inDegree.merge(dependent, -1, Integer::sum);
                    if (inDegree.get(dependent) == 0) queue.add(dependent);
                }
            }

            if (order.size() != modules.size()) {
                throw new RuntimeException("Dependencia circular detectada entre módulos");
            }
            return order;
        }

        void apply() {
            System.out.println("=== Terraform Module Apply ===");
            List<TerraformModule> order = topologicalOrder();
            System.out.print("\nOrden de apply: ");
            order.forEach(m -> System.out.print(m.name + " "));
            System.out.println();

            // Acumula todos los outputs para propagarlos
            Map<String, String> globalOutputs = new LinkedHashMap<>();

            for (TerraformModule module : order) {
                // Inyecta outputs anteriores como inputs del módulo actual
                module.resolveInputs(globalOutputs);
                Map<String, String> outputs = module.apply();
                globalOutputs.putAll(outputs);
            }

            System.out.println("\n=== Outputs finales acumulados ===");
            globalOutputs.forEach((k, v) -> System.out.printf("  %-20s = %s%n", k, v));
        }
    }

    public static void main(String[] args) {
        // Módulo vpc: sin inputs, produce vpc-id y subnet-id
        TerraformModule vpc = new TerraformModule(
                "vpc",
                Map.of(),
                List.of("aws_vpc.main", "aws_subnet.public"),
                new LinkedHashMap<>(Map.of("vpc-id", "vpc-0a1b2c3d", "subnet-id", "subnet-0x1y2z"))
        );

        // Módulo database: necesita vpc-id y subnet-id de vpc
        TerraformModule database = new TerraformModule(
                "database",
                new LinkedHashMap<>(Map.of("vpc-id", "", "subnet-id", "")),
                List.of("aws_db_instance.main", "aws_security_group.db"),
                new LinkedHashMap<>(Map.of("db-endpoint", "mydb.cluster.rds.amazonaws.com:5432"))
        );
        database.addDependency("vpc");

        // Módulo app: necesita db-endpoint y vpc-id de database/vpc
        TerraformModule app = new TerraformModule(
                "app",
                new LinkedHashMap<>(Map.of("db-endpoint", "", "vpc-id", "")),
                List.of("aws_ecs_service.app", "aws_lb.app", "aws_route53_record.app"),
                new LinkedHashMap<>(Map.of("app-url", "https://app.example.com"))
        );
        app.addDependency("database");
        app.addDependency("vpc");

        ModuleComposer composer = new ModuleComposer();
        composer.register(vpc);
        composer.register(database);
        composer.register(app);

        composer.apply();
    }
}
