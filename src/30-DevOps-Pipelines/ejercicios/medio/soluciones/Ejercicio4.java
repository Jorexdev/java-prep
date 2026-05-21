import java.util.*;
import java.util.function.Predicate;

public class Ejercicio4 {

    enum StageStatus { SKIPPED, PASSED }

    static class PipelineContext {
        Map<String, String> vars = new LinkedHashMap<>();

        PipelineContext(Map<String, String> initial) { vars.putAll(initial); }

        String get(String key)              { return vars.getOrDefault(key, ""); }
        void   set(String key, String val)  { vars.put(key, val); }
    }

    static class Stage {
        String name;
        Predicate<PipelineContext> when;
        Runnable task;
        StageStatus status;

        Stage(String name, Predicate<PipelineContext> when, Runnable task) {
            this.name = name;
            this.when = when;
            this.task = task;
        }

        void run(PipelineContext ctx) {
            boolean shouldRun = (when == null) || when.test(ctx);
            System.out.printf("  [%-18s] condition=%s → ", name, shouldRun ? "true " : "false");
            if (shouldRun) {
                task.run();
                status = StageStatus.PASSED;
                System.out.println("EJECUTADO");
            } else {
                status = StageStatus.SKIPPED;
                System.out.println("SKIPPED");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Conditional Stage Execution Demo ===\n");

        // Scenario 1: rama feature → deploy-staging ejecuta, deploy-prod skipped
        System.out.println("--- Scenario 1: branch=feature/login ---");
        PipelineContext ctx1 = new PipelineContext(Map.of(
                "branch", "feature/login",
                "tests",  "passed"
        ));
        runPipeline(ctx1);

        // Scenario 2: rama main, tests pasados → deploy-prod ejecuta
        System.out.println("\n--- Scenario 2: branch=main, tests=passed ---");
        PipelineContext ctx2 = new PipelineContext(Map.of(
                "branch", "main",
                "tests",  "passed"
        ));
        runPipeline(ctx2);

        // Scenario 3: rama main pero tests fallaron → deploy-prod skipped
        System.out.println("\n--- Scenario 3: branch=main, tests=failed ---");
        PipelineContext ctx3 = new PipelineContext(Map.of(
                "branch", "main",
                "tests",  "failed"
        ));
        runPipeline(ctx3);
    }

    static void runPipeline(PipelineContext ctx) {
        List<Stage> stages = List.of(
                new Stage("checkout",
                        null,
                        () -> {}),
                new Stage("test",
                        null,
                        () -> {}),
                new Stage("deploy-staging",
                        c -> !c.get("branch").equals("main"),
                        () -> System.out.print("  → desplegando en staging... ")),
                new Stage("deploy-prod",
                        c -> c.get("branch").equals("main") && c.get("tests").equals("passed"),
                        () -> System.out.print("  → desplegando en producción... "))
        );

        stages.forEach(s -> s.run(ctx));
    }
}
