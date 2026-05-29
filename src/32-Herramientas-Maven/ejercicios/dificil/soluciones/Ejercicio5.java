import java.util.*;

public class Ejercicio5 {

    static class ParentPom {
        String groupId;
        String artifactId;
        String version;
        Map<String, String> properties;           // java.version, etc.
        Map<String, String> dependencyManagement; // artifactId → version

        ParentPom(String groupId, String artifactId, String version,
                  Map<String, String> properties,
                  Map<String, String> dependencyManagement) {
            this.groupId             = groupId;
            this.artifactId          = artifactId;
            this.version             = version;
            this.properties          = new LinkedHashMap<>(properties);
            this.dependencyManagement = new LinkedHashMap<>(dependencyManagement);
        }
    }

    static class ChildModule {
        String name;
        ParentPom parent;
        List<String> ownDeps;        // deps declaradas (sin versión explícita)
        List<String> dependsOnModules; // otros módulos del reactor de los que depende

        ChildModule(String name, ParentPom parent, List<String> ownDeps,
                    List<String> dependsOnModules) {
            this.name              = name;
            this.parent            = parent;
            this.ownDeps           = ownDeps;
            this.dependsOnModules  = dependsOnModules;
        }

        // Resuelve la versión de cada dep usando dependencyManagement del parent
        Map<String, String> resolvedDeps() {
            Map<String, String> resolved = new LinkedHashMap<>();
            for (String dep : ownDeps) {
                String version = parent.dependencyManagement.getOrDefault(dep, "?? (no gestionada)");
                resolved.put(dep, version);
            }
            return resolved;
        }
    }

    static class ReactorBuild {
        Map<String, ChildModule> modules = new LinkedHashMap<>();

        void addModule(ChildModule module) {
            modules.put(module.name, module);
        }

        // Ordenamiento topológico respetando dependencias inter-módulo
        List<ChildModule> topologicalOrder() {
            Map<String, Integer> inDegree = new LinkedHashMap<>();
            Map<String, List<String>> dependents = new LinkedHashMap<>();

            modules.keySet().forEach(n -> {
                inDegree.put(n, 0);
                dependents.put(n, new ArrayList<>());
            });

            for (ChildModule m : modules.values()) {
                for (String dep : m.dependsOnModules) {
                    if (modules.containsKey(dep)) {
                        inDegree.merge(m.name, 1, Integer::sum);
                        dependents.get(dep).add(m.name);
                    }
                }
            }

            Queue<String> queue = new ArrayDeque<>();
            inDegree.forEach((name, deg) -> { if (deg == 0) queue.add(name); });

            List<ChildModule> order = new ArrayList<>();
            while (!queue.isEmpty()) {
                String current = queue.poll();
                order.add(modules.get(current));
                for (String dep : dependents.get(current)) {
                    inDegree.merge(dep, -1, Integer::sum);
                    if (inDegree.get(dep) == 0) queue.add(dep);
                }
            }

            if (order.size() != modules.size()) {
                throw new RuntimeException("Dependencia circular en el reactor");
            }
            return order;
        }

        void build() {
            System.out.println("=== Maven Multi-Module Reactor Build ===\n");
            List<ChildModule> order = topologicalOrder();

            System.out.print("Orden de construcción: ");
            order.forEach(m -> System.out.print(m.name + " → "));
            System.out.println("(fin)\n");

            for (ChildModule module : order) {
                System.out.printf("[BUILD] %s (parent=%s:%s)%n",
                        module.name,
                        module.parent.artifactId,
                        module.parent.version);

                if (!module.dependsOnModules.isEmpty()) {
                    System.out.printf("  Depende de módulos: %s%n", module.dependsOnModules);
                }

                Map<String, String> deps = module.resolvedDeps();
                if (!deps.isEmpty()) {
                    System.out.println("  Dependencias resueltas:");
                    deps.forEach((dep, ver) ->
                            System.out.printf("    %-25s → %s%n", dep, ver));
                }

                // Herencia de propiedades del parent
                System.out.printf("  Propiedades heredadas: java.version=%s%n",
                        module.parent.properties.getOrDefault("java.version", "N/A"));
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        // Parent POM
        ParentPom parent = new ParentPom(
                "com.example",
                "parent-pom",
                "1.0.0",
                Map.of("java.version", "21"),
                new LinkedHashMap<>(Map.of(
                        "spring-context", "6.1.2",
                        "jackson-databind", "2.15.0",
                        "slf4j-api", "2.0.7"
                ))
        );

        // Módulos del reactor
        ChildModule common = new ChildModule("common", parent,
                List.of("slf4j-api"), List.of());

        ChildModule domain = new ChildModule("domain", parent,
                List.of("jackson-databind"), List.of());

        ChildModule service = new ChildModule("service", parent,
                List.of("spring-context", "slf4j-api"),
                List.of("domain", "common"));

        ChildModule web = new ChildModule("web", parent,
                List.of("spring-context"),
                List.of("service"));

        ReactorBuild reactor = new ReactorBuild();
        reactor.addModule(web);     // registrado en cualquier orden
        reactor.addModule(service);
        reactor.addModule(domain);
        reactor.addModule(common);

        reactor.build();

        System.out.println("=== Parent POM dependencyManagement ===");
        parent.dependencyManagement.forEach((k, v) ->
                System.out.printf("  %-25s = %s%n", k, v));
    }
}
