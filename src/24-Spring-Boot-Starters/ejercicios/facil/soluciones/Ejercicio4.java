import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Ejercicio 4 (Fácil) — Auto-config order
// 3 auto-configs con dependencias → ordenar y ejecutar correctamente
public class Ejercicio4 {

    static class AutoConfig {
        final String name;
        final List<String> dependsOn; // nombres de otras auto-configs que deben ir antes

        AutoConfig(String name, String... dependsOn) {
            this.name = name;
            this.dependsOn = List.of(dependsOn);
        }

        void configure() {
            System.out.println("[AutoConfig] Ejecutando: " + name);
        }
    }

    /**
     * Ordena la lista de auto-configs de forma que cada una aparezca después
     * de sus dependencias. Usa orden topológico (DFS post-order).
     */
    static List<AutoConfig> topologicalSort(List<AutoConfig> configs) {
        Map<String, AutoConfig> byName = new LinkedHashMap<>();
        for (AutoConfig ac : configs) byName.put(ac.name, ac);

        List<AutoConfig> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>(); // para detectar ciclos

        for (AutoConfig ac : configs) {
            visit(ac.name, byName, visited, inStack, result);
        }

        return result;
    }

    private static void visit(
            String name,
            Map<String, AutoConfig> byName,
            Set<String> visited,
            Set<String> inStack,
            List<AutoConfig> result) {

        if (visited.contains(name)) return;
        if (inStack.contains(name)) throw new IllegalStateException("Ciclo detectado en " + name);

        AutoConfig ac = byName.get(name);
        if (ac == null) return; // dependencia no en la lista → ignorar

        inStack.add(name);

        for (String dep : ac.dependsOn) {
            visit(dep, byName, visited, inStack, result);
        }

        inStack.remove(name);
        visited.add(name);
        result.add(ac);
    }

    public static void main(String[] args) {
        System.out.println("=== Auto-config order ===");
        System.out.println();

        // Definición de dependencias:
        // JacksonConfig       → sin dependencias
        // WebConfig           → depende de JacksonConfig
        // SecurityConfig      → depende de WebConfig
        List<AutoConfig> configs = List.of(
            new AutoConfig("SecurityConfig", "WebConfig"),
            new AutoConfig("WebConfig", "JacksonConfig"),
            new AutoConfig("JacksonConfig")
        );

        System.out.println("Auto-configs definidas (sin ordenar):");
        for (AutoConfig ac : configs) {
            System.out.println("  " + ac.name
                    + (ac.dependsOn.isEmpty() ? "" : " → depende de: " + ac.dependsOn));
        }

        System.out.println();
        System.out.println("Orden topológico calculado:");
        List<AutoConfig> ordered = topologicalSort(configs);
        for (int i = 0; i < ordered.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + ordered.get(i).name);
        }

        System.out.println();
        System.out.println("Ejecutando en orden correcto:");
        ordered.forEach(AutoConfig::configure);

        System.out.println();
        System.out.println("=== Caso con más dependencias ===");
        List<AutoConfig> configs2 = List.of(
            new AutoConfig("MetricsConfig", "WebConfig"),
            new AutoConfig("WebConfig", "JacksonConfig"),
            new AutoConfig("SecurityConfig", "WebConfig"),
            new AutoConfig("JacksonConfig"),
            new AutoConfig("DataSourceConfig")
        );

        List<AutoConfig> ordered2 = topologicalSort(configs2);
        System.out.println("Orden:");
        for (int i = 0; i < ordered2.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + ordered2.get(i).name);
        }
        System.out.println("Ejecutando:");
        ordered2.forEach(AutoConfig::configure);
    }
}
