import java.util.*;

public class ExpPipelineStages {

    enum StageStatus { PENDING, RUNNING, PASSED, FAILED, SKIPPED }
    enum FailurePolicy { FAIL, CONTINUE, ROLLBACK }

    // A stage runs a list of commands; any throwing RuntimeException means failure
    static class Stage {
        private final String name;
        private final FailurePolicy onFailure;
        private final Runnable commands;
        private StageStatus status = StageStatus.PENDING;

        Stage(String name, FailurePolicy onFailure, Runnable commands) {
            this.name = name;
            this.onFailure = onFailure;
            this.commands = commands;
        }

        void run() {
            status = StageStatus.RUNNING;
            System.out.printf("  [%-10s] RUNNING...%n", name);
            try {
                commands.run();
                status = StageStatus.PASSED;
                System.out.printf("  [%-10s] PASSED%n", name);
            } catch (RuntimeException e) {
                status = StageStatus.FAILED;
                System.out.printf("  [%-10s] FAILED — %s%n", name, e.getMessage());
            }
        }

        void skip() {
            status = StageStatus.SKIPPED;
            System.out.printf("  [%-10s] SKIPPED%n", name);
        }

        StageStatus getStatus()     { return status; }
        FailurePolicy getOnFailure(){ return onFailure; }
        String getName()            { return name; }
    }

    static class Pipeline {
        private final String name;
        private final List<Stage> stages;

        Pipeline(String name, List<Stage> stages) {
            this.name = name;
            this.stages = stages;
        }

        void run() {
            System.out.printf("%n[Pipeline: %s]%n", name);
            System.out.println("─".repeat(55));

            boolean aborted = false;
            for (Stage stage : stages) {
                if (aborted) {
                    stage.skip();
                    continue;
                }
                stage.run();
                if (stage.getStatus() == StageStatus.FAILED) {
                    switch (stage.getOnFailure()) {
                        case FAIL     -> { aborted = true; System.out.println("  !! Pipeline abortado."); }
                        case ROLLBACK -> { aborted = true; System.out.println("  !! Iniciando rollback..."); }
                        case CONTINUE -> System.out.println("  (continúa a pesar del fallo)");
                    }
                }
            }

            System.out.println("\n  Resumen:");
            for (Stage s : stages) {
                System.out.printf("    %-10s → %s%n", s.getName(), s.getStatus());
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(55));
        System.out.println("  CI/CD PIPELINE STAGES — simulación");
        System.out.println("═".repeat(55));

        // ── Escenario: test falla → pipeline se aborta ────────────
        List<Stage> stages = List.of(
                new Stage("build",   FailurePolicy.FAIL,     () -> System.out.println("    mvn compile OK")),
                new Stage("test",    FailurePolicy.FAIL,     () -> {
                    System.out.println("    mvn test...");
                    throw new RuntimeException("2 tests fallaron: UserServiceTest, OrderServiceTest");
                }),
                new Stage("package", FailurePolicy.FAIL,     () -> System.out.println("    mvn package OK")),
                new Stage("deploy",  FailurePolicy.ROLLBACK,  () -> System.out.println("    kubectl apply OK"))
        );

        Pipeline pipeline = new Pipeline("myapp-ci", stages);
        pipeline.run();

        System.out.println("\n── Conclusión ──");
        System.out.println("  onFailure=FAIL aborta el pipeline inmediatamente.");
        System.out.println("  Las etapas posteriores quedan en SKIPPED.");
    }
}
