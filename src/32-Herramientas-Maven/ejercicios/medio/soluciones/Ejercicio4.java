import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Ejercicio4 {

    static class Module {
        final String name;
        final List<String> dependsOn; // nombres de otros módulos

        Module(String name, String... deps) {
            this.name = name;
            this.dependsOn = List.of(deps);
        }

        @Override public String toString() { return name; }
    }

    static class ReactorProject {
        private final List<Module> modules;

        ReactorProject(List<Module> modules) {
            this.modules = modules;
        }

        // Ordenación topológica con algoritmo de Kahn (BFS)
        List<Module> buildOrder() {
            Map<String, Module> byName = new HashMap<>();
            Map<String, Integer> inDegree = new HashMap<>();
            Map<String, List<String>> dependents = new HashMap<>(); // quién depende de mí

            for (Module m : modules) {
                byName.put(m.name, m);
                inDegree.put(m.name, 0);
                dependents.put(m.name, new ArrayList<>());
            }

            // Calcular in-degree (cuántos módulos me bloquean)
            for (Module m : modules) {
                for (String dep : m.dependsOn) {
                    dependents.get(dep).add(m.name);
                    inDegree.merge(m.name, 1, Integer::sum);
                }
            }

            // Iniciar con módulos sin dependencias
            Queue<String> ready = new LinkedList<>();
            inDegree.forEach((name, degree) -> {
                if (degree == 0) ready.add(name);
            });

            List<Module> ordered = new ArrayList<>();
            while (!ready.isEmpty()) {
                String current = ready.poll();
                ordered.add(byName.get(current));
                // Reducir in-degree de los que dependían de este
                for (String dependent : dependents.get(current)) {
                    int newDegree = inDegree.get(dependent) - 1;
                    inDegree.put(dependent, newDegree);
                    if (newDegree == 0) ready.add(dependent);
                }
            }

            if (ordered.size() != modules.size()) {
                throw new IllegalStateException("Ciclo de dependencias detectado en el reactor");
            }
            return ordered;
        }

        void build() {
            System.out.println("[INFO] Reactor build order:");
            List<Module> order = buildOrder();
            order.forEach(m -> System.out.printf("  [INFO]   %s%n", m.name));
            System.out.println();

            System.out.println("[INFO] Building reactor...");
            for (int i = 0; i < order.size(); i++) {
                Module m = order.get(i);
                System.out.printf("[INFO] --- %d/%d: Building %s ---%n",
                        i + 1, order.size(), m.name);
                if (!m.dependsOn.isEmpty()) {
                    System.out.println("[INFO]   Dependencias: " + m.dependsOn);
                }
                System.out.println("[INFO]   BUILD SUCCESS");
            }
        }
    }

    public static void main(String[] args) {
        // 5 módulos con dependencias entre sí
        // common <- core <- service <- api
        // common <- security
        // service depende de security
        List<Module> modules = List.of(
            new Module("common"),                          // base, sin deps
            new Module("security",  "common"),            // depende de common
            new Module("core",      "common"),            // depende de common
            new Module("service",   "core", "security"),  // depende de core y security
            new Module("api",       "service")            // depende de service
        );

        System.out.println("=== Declaración de módulos ===");
        modules.forEach(m -> System.out.println("  " + m.name + " -> " + m.dependsOn));
        System.out.println();

        ReactorProject reactor = new ReactorProject(modules);
        reactor.build();

        System.out.println();
        System.out.println("[INFO] Reactor Summary:");
        System.out.println("[INFO] SUCCESS");
    }
}
