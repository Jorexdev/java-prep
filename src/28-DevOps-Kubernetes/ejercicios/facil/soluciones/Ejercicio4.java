import java.util.*;

public class Ejercicio4 {

    static class ConfigMap {
        String name;
        Map<String, String> data;

        ConfigMap(String name, Map<String, String> data) {
            this.name = name;
            this.data = new LinkedHashMap<>(data);
        }
    }

    static class Pod {
        String name;
        Map<String, String> envVars   = new LinkedHashMap<>();
        Map<String, String> fileMount = new LinkedHashMap<>();

        Pod(String name) {
            this.name = name;
        }

        // Modo (a): montar ConfigMap como variables de entorno
        void mountAsEnv(ConfigMap cm) {
            System.out.printf("  Montando ConfigMap '%s' como env vars en pod '%s'%n",
                    cm.name, name);
            envVars.putAll(cm.data);
        }

        // Modo (b): montar ConfigMap como archivos simulados
        void mountAsFiles(ConfigMap cm, String mountPath) {
            System.out.printf("  Montando ConfigMap '%s' como archivos en '%s' en pod '%s'%n",
                    cm.name, mountPath, name);
            cm.data.forEach((key, value) ->
                    fileMount.put(mountPath + "/" + key, value));
        }

        void printEnv() {
            System.out.println("\n  [" + name + "] Environment variables:");
            if (envVars.isEmpty()) {
                System.out.println("    (vacío)");
            } else {
                envVars.forEach((k, v) -> System.out.printf("    %s=%s%n", k, v));
            }
        }

        void printFiles() {
            System.out.println("\n  [" + name + "] Archivos montados:");
            if (fileMount.isEmpty()) {
                System.out.println("    (vacío)");
            } else {
                fileMount.forEach((path, content) -> {
                    String preview = content.length() > 50
                            ? content.substring(0, 47) + "..."
                            : content;
                    System.out.printf("    %-40s → \"%s\"%n", path, preview);
                });
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes ConfigMap Demo ===\n");

        Map<String, String> appConfig = new LinkedHashMap<>();
        appConfig.put("DB_HOST",     "postgres.default.svc.cluster.local");
        appConfig.put("DB_PORT",     "5432");
        appConfig.put("APP_ENV",     "production");
        appConfig.put("LOG_LEVEL",   "INFO");

        Map<String, String> fileConfig = new LinkedHashMap<>();
        fileConfig.put("application.yml",
                "server:\n  port: 8080\nspring:\n  datasource:\n    url: jdbc:postgresql://postgres:5432/mydb");
        fileConfig.put("logback.xml",
                "<configuration><appender name=\"STDOUT\"><pattern>%msg%n</pattern></appender></configuration>");

        ConfigMap envCm  = new ConfigMap("app-config",  appConfig);
        ConfigMap fileCm = new ConfigMap("file-config", fileConfig);

        System.out.println("--- Modo (a): env vars ---");
        Pod podA = new Pod("app-pod-env");
        podA.mountAsEnv(envCm);
        podA.printEnv();

        System.out.println("\n--- Modo (b): archivos ---");
        Pod podB = new Pod("app-pod-files");
        podB.mountAsFiles(fileCm, "/etc/config");
        podB.printFiles();

        System.out.println("\n--- Pod con ambos modos ---");
        Pod podC = new Pod("app-pod-both");
        podC.mountAsEnv(envCm);
        podC.mountAsFiles(fileCm, "/app/config");
        podC.printEnv();
        podC.printFiles();
    }
}
