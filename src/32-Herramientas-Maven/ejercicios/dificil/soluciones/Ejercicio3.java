import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    static class Goal {
        final String name;
        final Runnable action;
        Goal(String name, Runnable action) { this.name = name; this.action = action; }
    }

    static class Plugin {
        final String id;
        final String phase;
        final List<Goal> goals;

        Plugin(String id, String phase, Goal... goals) {
            this.id = id;
            this.phase = phase;
            this.goals = List.of(goals);
        }
    }

    static class CustomLifecycle {
        private final String name;
        private final List<String> phases;
        private final Map<String, List<Plugin>> pluginsByPhase = new LinkedHashMap<>();

        CustomLifecycle(String name, String... phases) {
            this.name = name;
            this.phases = List.of(phases);
        }

        void bindPlugin(Plugin plugin) {
            if (!phases.contains(plugin.phase)) {
                throw new IllegalArgumentException("Fase desconocida en lifecycle '" + name + "': " + plugin.phase);
            }
            pluginsByPhase.computeIfAbsent(plugin.phase, k -> new ArrayList<>()).add(plugin);
            System.out.printf("[INFO] Plugin vinculado: %s -> fase '%s'%n", plugin.id, plugin.phase);
        }

        void run(String targetPhase) {
            if (!phases.contains(targetPhase)) {
                throw new IllegalArgumentException("Fase objetivo desconocida: " + targetPhase);
            }
            int targetIdx = phases.indexOf(targetPhase);

            System.out.println();
            System.out.println("[INFO] Lifecycle: " + name);
            System.out.println("[INFO] Objetivo: " + targetPhase);
            System.out.println("[INFO]");

            for (int i = 0; i <= targetIdx; i++) {
                String phase = phases.get(i);
                System.out.println("[INFO] --- fase: " + phase + " ---");
                List<Plugin> plugins = pluginsByPhase.getOrDefault(phase, List.of());
                if (plugins.isEmpty()) {
                    System.out.println("[INFO] (sin plugins)");
                } else {
                    for (Plugin plugin : plugins) {
                        for (Goal goal : plugin.goals) {
                            System.out.printf("[INFO] Ejecutando %s:%s%n", plugin.id, goal.name);
                            goal.action.run();
                        }
                    }
                }
                System.out.println("[INFO]");
            }
            System.out.println("[INFO] BUILD SUCCESS");
        }
    }

    public static void main(String[] args) {
        // Definir lifecycle custom "integration"
        CustomLifecycle integration = new CustomLifecycle("integration",
            "pre-integration", "integration-test", "post-integration"
        );

        System.out.println("=== Registrando plugins en lifecycle 'integration' ===");

        // Plugin 1: pre-integration — levantar servicios/contenedores
        integration.bindPlugin(new Plugin(
            "docker-compose-plugin", "pre-integration",
            new Goal("start", () -> {
                System.out.println("[INFO]   Iniciando contenedores Docker...");
                System.out.println("[INFO]   postgres:15 - started on port 5432");
                System.out.println("[INFO]   redis:7    - started on port 6379");
            })
        ));

        // Plugin 2: integration-test — ejecutar tests de integración
        integration.bindPlugin(new Plugin(
            "maven-failsafe-plugin", "integration-test",
            new Goal("integration-test", () -> {
                System.out.println("[INFO]   Ejecutando integration tests...");
                System.out.println("[INFO]   Tests run: 8, Failures: 0, Errors: 0");
            }),
            new Goal("verify", () ->
                System.out.println("[INFO]   Verificando resultados de integration tests..."))
        ));

        // Plugin 3: post-integration — limpiar servicios
        integration.bindPlugin(new Plugin(
            "docker-compose-plugin", "post-integration",
            new Goal("stop", () -> {
                System.out.println("[INFO]   Deteniendo contenedores Docker...");
                System.out.println("[INFO]   Contenedores eliminados correctamente");
            })
        ));

        System.out.println();
        System.out.println("=== mvn integration-test (lifecycle: integration) ===");
        integration.run("integration-test");

        System.out.println();
        System.out.println("Nota: post-integration no se ejecutó porque el objetivo fue 'integration-test'");
        System.out.println("Para ejecutar todo: mvn post-integration");
    }
}
