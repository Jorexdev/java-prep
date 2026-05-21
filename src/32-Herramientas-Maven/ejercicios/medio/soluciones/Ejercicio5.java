import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ejercicio5 {

    static class PluginConfig {
        private final Map<String, String> rawConfig;  // config tal como está en el POM
        private final Map<String, String> pomProperties; // propiedades del POM para resolver ${...}

        PluginConfig(Map<String, String> rawConfig, Map<String, String> pomProperties) {
            this.rawConfig = rawConfig;
            this.pomProperties = pomProperties;
        }

        // Resuelve ${property} usando las propiedades del POM
        String resolve(String value) {
            Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(value);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                String propName = matcher.group(1);
                String replacement = pomProperties.getOrDefault(propName, "${" + propName + "}");
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);
            return result.toString();
        }

        // Devuelve la configuración con todas las propiedades resueltas
        Map<String, String> resolved() {
            Map<String, String> result = new LinkedHashMap<>();
            rawConfig.forEach((key, value) -> result.put(key, resolve(value)));
            return result;
        }

        void print(String pluginId) {
            System.out.println("=== Plugin config: " + pluginId + " ===");
            System.out.println("Raw (en el POM):");
            rawConfig.forEach((k, v) -> System.out.printf("  %-20s = %s%n", k, v));
            System.out.println("Resuelta:");
            resolved().forEach((k, v) -> System.out.printf("  %-20s = %s%n", k, v));
            System.out.println();
        }
    }

    static class Plugin {
        final String id;
        final PluginConfig config;

        Plugin(String id, PluginConfig config) {
            this.id = id;
            this.config = config;
        }

        void execute(String goal) {
            System.out.printf("[INFO] --- %s:%s ---%n", id, goal);
            Map<String, String> cfg = config.resolved();
            System.out.println("[INFO] Configuración aplicada:");
            cfg.forEach((k, v) -> System.out.printf("[INFO]   %-20s = %s%n", k, v));
            System.out.printf("[INFO] Ejecutando %s:%s con configuración resuelta%n%n", id, goal);
        }
    }

    public static void main(String[] args) {
        // Propiedades del POM
        Map<String, String> pomProperties = Map.of(
            "java.version",        "21",
            "project.build.dir",   "target",
            "project.encoding",    "UTF-8"
        );

        System.out.println("=== Propiedades del POM ===");
        pomProperties.forEach((k, v) -> System.out.printf("  ${%-25s} = %s%n", k, v));
        System.out.println();

        // maven-compiler-plugin con referencias a propiedades
        PluginConfig compilerConfig = new PluginConfig(
            Map.of(
                "source",      "${java.version}",
                "target",      "${java.version}",
                "encoding",    "${project.encoding}",
                "parameters",  "true",
                "outputDir",   "${project.build.dir}/classes"
            ),
            pomProperties
        );
        Plugin compiler = new Plugin("maven-compiler-plugin", compilerConfig);
        compiler.config.print("maven-compiler-plugin");

        // maven-resources-plugin
        PluginConfig resourcesConfig = new PluginConfig(
            Map.of(
                "encoding",    "${project.encoding}",
                "outputDir",   "${project.build.dir}/resources",
                "filtering",   "true"
            ),
            pomProperties
        );
        Plugin resources = new Plugin("maven-resources-plugin", resourcesConfig);
        resources.config.print("maven-resources-plugin");

        // Simular ejecución
        System.out.println("=== Ejecución ===");
        compiler.execute("compile");
        resources.execute("copy-resources");
    }
}
