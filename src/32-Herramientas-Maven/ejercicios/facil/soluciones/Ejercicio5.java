import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio5 {

    static class Goal {
        final String name;
        final Runnable action;

        Goal(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }
    }

    static class Plugin {
        final String groupId;
        final String artifactId;
        final List<Goal> goals;
        final String phase;

        Plugin(String groupId, String artifactId, String phase, Goal... goals) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.phase = phase;
            this.goals = List.of(goals);
        }

        String id() { return groupId + ":" + artifactId; }
    }

    static class LifecycleWithPlugins {
        private final List<String> phaseOrder = List.of(
            "validate", "compile", "test", "package", "verify", "install", "deploy"
        );
        // Fase -> lista de plugins vinculados
        private final Map<String, List<Plugin>> pluginsByPhase = new LinkedHashMap<>();

        void register(Plugin plugin) {
            pluginsByPhase.computeIfAbsent(plugin.phase, k -> new ArrayList<>()).add(plugin);
            System.out.printf("[INFO] Plugin registrado: %s en fase '%s'%n",
                    plugin.id(), plugin.phase);
        }

        void run(String targetPhase) {
            int targetIdx = phaseOrder.indexOf(targetPhase);
            System.out.println();
            System.out.println("[INFO] --- BUILD ---");

            for (int i = 0; i <= targetIdx; i++) {
                String phase = phaseOrder.get(i);
                System.out.println("[INFO] --- " + phase + " ---");
                List<Plugin> plugins = pluginsByPhase.getOrDefault(phase, List.of());
                if (plugins.isEmpty()) {
                    System.out.println("[INFO] (sin plugins vinculados)");
                } else {
                    for (Plugin plugin : plugins) {
                        for (Goal goal : plugin.goals) {
                            System.out.printf("[INFO] %s:%s%n", plugin.id(), goal.name);
                            goal.action.run();
                        }
                    }
                }
                System.out.println();
            }
            System.out.println("[INFO] BUILD SUCCESS");
        }
    }

    public static void main(String[] args) {
        LifecycleWithPlugins lifecycle = new LifecycleWithPlugins();

        // maven-compiler-plugin en fase compile
        Plugin compiler = new Plugin(
            "org.apache.maven.plugins", "maven-compiler-plugin", "compile",
            new Goal("compile", () -> {
                System.out.println("[INFO]   Compilando fuentes Java 21...");
                System.out.println("[INFO]   Compiladas 18 clases en target/classes");
            })
        );

        // maven-surefire-plugin en fase test
        Plugin surefire = new Plugin(
            "org.apache.maven.plugins", "maven-surefire-plugin", "test",
            new Goal("test", () -> {
                System.out.println("[INFO]   Ejecutando tests...");
                System.out.println("[INFO]   Tests run: 32, Failures: 0, Errors: 0, Skipped: 1");
            })
        );

        // maven-jar-plugin en fase package
        Plugin jar = new Plugin(
            "org.apache.maven.plugins", "maven-jar-plugin", "package",
            new Goal("jar", () ->
                System.out.println("[INFO]   Building jar: target/app-1.0.0.jar"))
        );

        System.out.println("=== Registrando plugins ===");
        lifecycle.register(compiler);
        lifecycle.register(surefire);
        lifecycle.register(jar);

        System.out.println();
        System.out.println("=== mvn package ===");
        lifecycle.run("package");
    }
}
