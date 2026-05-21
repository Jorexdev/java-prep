import java.util.*;

public class Ejercicio5 {

    static class PipelineContext {
        Map<String, String> vars;

        PipelineContext(Map<String, String> initial) {
            this.vars = new LinkedHashMap<>(initial);
        }

        String get(String key) {
            return vars.getOrDefault(key, "");
        }

        void set(String key, String value) {
            vars.put(key, value);
        }

        void print(String title) {
            System.out.println("  " + title);
            vars.forEach((k, v) -> System.out.printf("    %-20s = %s%n", k, v));
        }
    }

    @FunctionalInterface
    interface PipelineStage {
        void execute(PipelineContext ctx);
    }

    static class NamedStage {
        String name;
        PipelineStage stage;

        NamedStage(String name, PipelineStage stage) {
            this.name  = name;
            this.stage = stage;
        }

        void run(PipelineContext ctx) {
            System.out.printf("%n[Stage: %s]%n", name);
            stage.execute(ctx);
            ctx.print("Contexto tras el stage:");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Pipeline Variables (PipelineContext) Demo ===\n");

        Map<String, String> initial = new LinkedHashMap<>();
        initial.put("BUILD_NUMBER",  "42");
        initial.put("BRANCH",        "feature/new-api");
        initial.put("ARTIFACT_PATH", "");
        initial.put("TEST_RESULT",   "PENDING");
        initial.put("DEPLOY_ENV",    "");

        PipelineContext ctx = new PipelineContext(initial);
        System.out.println("Contexto inicial:");
        ctx.print("");

        List<NamedStage> stages = List.of(
                new NamedStage("compile", c -> {
                    String build = c.get("BUILD_NUMBER");
                    String artifactPath = "target/app-" + build + ".jar";
                    c.set("ARTIFACT_PATH", artifactPath);
                    System.out.printf("  Compilando build #%s → %s%n", build, artifactPath);
                }),
                new NamedStage("test", c -> {
                    String artifact = c.get("ARTIFACT_PATH");
                    System.out.printf("  Ejecutando tests sobre '%s'...%n", artifact);
                    // Simular tests pasados
                    c.set("TEST_RESULT", "PASSED");
                    c.set("TEST_COUNT", "47");
                }),
                new NamedStage("package", c -> {
                    String testResult = c.get("TEST_RESULT");
                    if (!testResult.equals("PASSED")) {
                        System.out.println("  SKIP: tests no pasaron.");
                        return;
                    }
                    String artifact = c.get("ARTIFACT_PATH");
                    c.set("DOCKER_IMAGE", "myrepo/app:" + c.get("BUILD_NUMBER"));
                    System.out.printf("  Empaquetando '%s' → Docker image: %s%n",
                            artifact, c.get("DOCKER_IMAGE"));
                }),
                new NamedStage("deploy", c -> {
                    String branch = c.get("BRANCH");
                    String env    = branch.equals("main") ? "production" : "staging";
                    c.set("DEPLOY_ENV", env);
                    System.out.printf("  Desplegando imagen '%s' en '%s'%n",
                            c.get("DOCKER_IMAGE"), env);
                })
        );

        for (NamedStage s : stages) {
            s.run(ctx);
        }

        System.out.println("\n=== Contexto final ===");
        ctx.print("");
    }
}
