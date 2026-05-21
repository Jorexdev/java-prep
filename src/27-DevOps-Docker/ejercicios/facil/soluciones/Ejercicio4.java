import java.util.*;

public class Ejercicio4 {

    static class DockerImage {
        String name;
        Map<String, String> defaultEnv;

        DockerImage(String name, Map<String, String> defaultEnv) {
            this.name       = name;
            this.defaultEnv = new LinkedHashMap<>(defaultEnv);
        }
    }

    static class Container {
        String name;
        DockerImage image;
        Map<String, String> envOverrides;

        Container(String name, DockerImage image, Map<String, String> envOverrides) {
            this.name         = name;
            this.image        = image;
            this.envOverrides = new LinkedHashMap<>(envOverrides);
        }

        Map<String, String> resolvedEnv() {
            Map<String, String> resolved = new LinkedHashMap<>(image.defaultEnv);
            resolved.putAll(envOverrides);
            return resolved;
        }

        void printEnv() {
            System.out.println("\nContainer: " + name + " (image: " + image.name + ")");
            System.out.println("  Env vars de la imagen (defaults):");
            image.defaultEnv.forEach((k, v) -> System.out.printf("    %s=%s%n", k, v));
            System.out.println("  Overrides del container:");
            if (envOverrides.isEmpty()) {
                System.out.println("    (ninguno)");
            } else {
                envOverrides.forEach((k, v) -> System.out.printf("    %s=%s%n", k, v));
            }
            System.out.println("  Entorno resuelto final:");
            resolvedEnv().forEach((k, v) -> {
                boolean overridden = envOverrides.containsKey(k);
                System.out.printf("    %s=%s%s%n", k, v, overridden ? "  [override]" : "");
            });
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Docker Environment Variables — Herencia imagen→container ===");

        Map<String, String> imageEnv = new LinkedHashMap<>();
        imageEnv.put("JAVA_OPTS",    "-Xmx512m");
        imageEnv.put("APP_ENV",      "production");
        imageEnv.put("LOG_LEVEL",    "INFO");
        imageEnv.put("DB_HOST",      "localhost");
        imageEnv.put("DB_PORT",      "5432");

        DockerImage image = new DockerImage("my-app:2.0", imageEnv);

        Map<String, String> devOverrides = new LinkedHashMap<>();
        devOverrides.put("APP_ENV",   "development");
        devOverrides.put("LOG_LEVEL", "DEBUG");
        devOverrides.put("DB_HOST",   "dev-db.internal");

        Container devContainer = new Container("app-dev", image, devOverrides);

        Map<String, String> prodOverrides = new LinkedHashMap<>();
        prodOverrides.put("DB_HOST", "prod-db.internal");
        prodOverrides.put("JAVA_OPTS", "-Xmx2g -XX:+UseG1GC");

        Container prodContainer = new Container("app-prod", image, prodOverrides);

        devContainer.printEnv();
        prodContainer.printEnv();
    }
}
