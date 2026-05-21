import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// Ejercicio 1 (Difícil) — Topological auto-config
// 5 auto-configs con dependencias → Kahn's algorithm (BFS topológico)
public class Ejercicio1 {

    static class AutoConfig {
        final String name;
        final List<String> dependsOn;

        AutoConfig(String name, String... deps) {
            this.name = name;
            this.dependsOn = List.of(deps);
        }

        void configure(int order) {
            System.out.println("  #" + order + " [AutoConfig] Ejecutando: " + name
                    + (dependsOn.isEmpty() ? "" : " (después de: " + dependsOn + ")"));
        }
    }

    /**
     * Kahn's algorithm para ordenamiento topológico.
     * Requiere que no haya ciclos.
     *
     * 1. Calcular in-degree (número de dependencias) de cada nodo.
     * 2. Encolar todos los nodos con in-degree = 0.
     * 3. Desencolar, añadir al resultado, decrementar in-degree de sucesores.
     * 4. Si algún sucesor llega a 0, encolar.
     * 5. Repetir hasta vaciar la cola.
     */
    static List<AutoConfig> kahnsSort(List<AutoConfig> configs) {
        Map<String, AutoConfig> byName = new LinkedHashMap<>();
        for (AutoConfig ac : configs) byName.put(ac.name, ac);

        // Calcular in-degree para cada nodo
        Map<String, Integer> inDegree = new HashMap<>();
        for (AutoConfig ac : configs) inDegree.put(ac.name, 0);

        for (AutoConfig ac : configs) {
            for (String dep : ac.dependsOn) {
                // dep debe ir antes de ac → ac tiene +1 in-degree
                inDegree.merge(ac.name, 1, Integer::sum);
            }
        }

        // Construir grafo inverso: quién depende de quién
        // dependsOn[B] = A significa: A debe ir antes de B
        // Invertido: B → [lista de quienes esperan a B para poder ejecutarse]
        Map<String, List<String>> dependents = new HashMap<>();
        for (AutoConfig ac : configs) dependents.put(ac.name, new ArrayList<>());

        for (AutoConfig ac : configs) {
            for (String dep : ac.dependsOn) {
                dependents.computeIfAbsent(dep, k -> new ArrayList<>()).add(ac.name);
            }
        }

        // Cola inicial: nodos sin dependencias (in-degree = 0)
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.offer(e.getKey());
        }

        List<AutoConfig> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(byName.get(current));

            for (String dependent : dependents.getOrDefault(current, List.of())) {
                int newDegree = inDegree.merge(dependent, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.offer(dependent);
                }
            }
        }

        if (result.size() != configs.size()) {
            throw new IllegalStateException("Ciclo detectado en las dependencias de auto-config");
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println("=== Topological auto-config (Kahn's algorithm) ===");
        System.out.println();

        // Dependencias definidas en el enunciado:
        // A → B (A depende de B)
        // B → C (B depende de C)
        // D → B (D depende de B)
        // E → C (E depende de C)
        List<AutoConfig> configs = new ArrayList<>();
        configs.add(new AutoConfig("A", "B"));
        configs.add(new AutoConfig("B", "C"));
        configs.add(new AutoConfig("C"));          // sin dependencias
        configs.add(new AutoConfig("D", "B"));
        configs.add(new AutoConfig("E", "C"));

        System.out.println("Auto-configs y sus dependencias:");
        for (AutoConfig ac : configs) {
            System.out.println("  " + ac.name
                    + (ac.dependsOn.isEmpty() ? " (sin dependencias)"
                       : " depende de → " + ac.dependsOn));
        }

        System.out.println();
        System.out.println("Árbol de dependencias:");
        System.out.println("  C (raíz)");
        System.out.println("  ├── B (depende de C)");
        System.out.println("  │   ├── A (depende de B)");
        System.out.println("  │   └── D (depende de B)");
        System.out.println("  └── E (depende de C)");

        System.out.println();
        System.out.println("Orden topológico calculado (Kahn's BFS):");
        List<AutoConfig> ordered = kahnsSort(configs);
        for (int i = 0; i < ordered.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + ordered.get(i).name);
        }

        System.out.println();
        System.out.println("Ejecutando en orden:");
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).configure(i + 1);
        }

        System.out.println();
        System.out.println("Verificaciones:");
        int posC = -1, posB = -1, posA = -1, posD = -1, posE = -1;
        for (int i = 0; i < ordered.size(); i++) {
            switch (ordered.get(i).name) {
                case "C" -> posC = i;
                case "B" -> posB = i;
                case "A" -> posA = i;
                case "D" -> posD = i;
                case "E" -> posE = i;
            }
        }
        System.out.println("  C antes de B: " + (posC < posB) + " (requerido)");
        System.out.println("  C antes de E: " + (posC < posE) + " (requerido)");
        System.out.println("  B antes de A: " + (posB < posA) + " (requerido)");
        System.out.println("  B antes de D: " + (posB < posD) + " (requerido)");
    }
}
