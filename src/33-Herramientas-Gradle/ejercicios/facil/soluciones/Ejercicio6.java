import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Ejercicio6 {

    enum Source {
        CLI_PARAM,      // -P flag (mayor precedencia)
        ENV_VAR,        // variables de entorno
        GRADLE_PROPS,   // gradle.properties
        DEFAULT         // valor por defecto (menor precedencia)
    }

    record PropertyEntry(String value, Source source) {}

    static class ProjectProperties {
        private final Map<String, PropertyEntry> cli;
        private final Map<String, PropertyEntry> env;
        private final Map<String, PropertyEntry> gradleProps;
        private final Map<String, PropertyEntry> defaults;

        ProjectProperties(
            Map<String, String> cli,
            Map<String, String> env,
            Map<String, String> gradleProps,
            Map<String, String> defaults
        ) {
            this.cli         = toEntryMap(cli, Source.CLI_PARAM);
            this.env         = toEntryMap(env, Source.ENV_VAR);
            this.gradleProps = toEntryMap(gradleProps, Source.GRADLE_PROPS);
            this.defaults    = toEntryMap(defaults, Source.DEFAULT);
        }

        private Map<String, PropertyEntry> toEntryMap(Map<String, String> raw, Source source) {
            Map<String, PropertyEntry> result = new LinkedHashMap<>();
            raw.forEach((k, v) -> result.put(k, new PropertyEntry(v, source)));
            return result;
        }

        // Resolver en orden de precedencia: CLI > ENV > gradle.properties > default
        Optional<PropertyEntry> get(String key) {
            if (cli.containsKey(key))         return Optional.of(cli.get(key));
            if (env.containsKey(key))         return Optional.of(env.get(key));
            if (gradleProps.containsKey(key)) return Optional.of(gradleProps.get(key));
            if (defaults.containsKey(key))    return Optional.of(defaults.get(key));
            return Optional.empty();
        }

        void printResolution(String key) {
            System.out.println("Resolviendo property: '" + key + "'");
            System.out.printf("  CLI (-P):          %s%n", cli.containsKey(key)         ? cli.get(key).value()         : "(no definida)");
            System.out.printf("  ENV var:           %s%n", env.containsKey(key)         ? env.get(key).value()         : "(no definida)");
            System.out.printf("  gradle.properties: %s%n", gradleProps.containsKey(key) ? gradleProps.get(key).value() : "(no definida)");
            System.out.printf("  default:           %s%n", defaults.containsKey(key)    ? defaults.get(key).value()    : "(no definida)");
            get(key).ifPresentOrElse(
                e -> System.out.printf("  => GANADOR: '%s' (fuente: %s)%n%n", e.value(), e.source()),
                () -> System.out.println("  => PROPERTY NO ENCONTRADA%n")
            );
        }
    }

    public static void main(String[] args) {
        // Simular distintas fuentes para las mismas properties
        Map<String, String> cliParams = Map.of(
            "env",       "prod"         // sobreescribe todo
        );

        Map<String, String> envVars = Map.of(
            "env",       "staging",     // sobreescrito por CLI
            "db.host",   "env-host"     // sobreescrito por CLI (no presente en CLI aquí)
        );

        Map<String, String> gradleProperties = Map.of(
            "env",       "dev",         // sobreescrito por ENV y CLI
            "db.host",   "localhost",   // sobreescrito por ENV
            "db.port",   "5432",        // solo está aquí y en default
            "log.level", "INFO"         // solo en gradle.properties
        );

        Map<String, String> defaults = Map.of(
            "env",       "local",       // menor precedencia
            "db.host",   "127.0.0.1",
            "db.port",   "3306",        // sobreescrito por gradle.properties
            "log.level", "WARN",        // sobreescrito por gradle.properties
            "timeout",   "30000"        // solo disponible en defaults
        );

        ProjectProperties props = new ProjectProperties(cliParams, envVars, gradleProperties, defaults);

        System.out.println("=== Resolución de propiedades de Gradle ===");
        System.out.println("(Precedencia: CLI > ENV > gradle.properties > default)");
        System.out.println();

        props.printResolution("env");
        props.printResolution("db.host");
        props.printResolution("db.port");
        props.printResolution("log.level");
        props.printResolution("timeout");
    }
}
