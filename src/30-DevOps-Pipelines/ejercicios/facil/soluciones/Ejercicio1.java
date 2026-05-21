import java.util.*;

public class Ejercicio1 {

    enum StageStatus { PENDING, PASSED, FAILED, SKIPPED }
    enum PipelineStatus { RUNNING, SUCCESS, FAILED }

    static class Stage {
        String name;
        Runnable task;
        StageStatus status = StageStatus.PENDING;

        Stage(String name, Runnable task) {
            this.name = name;
            this.task = task;
        }

        void run() {
            System.out.printf("  [%-12s] ejecutando...", name);
            try {
                task.run();
                status = StageStatus.PASSED;
                System.out.printf(" %s%n", status);
            } catch (Exception e) {
                status = StageStatus.FAILED;
                System.out.printf(" %s  (%s)%n", status, e.getMessage());
            }
        }
    }

    static class Pipeline {
        String name;
        List<Stage> stages;
        PipelineStatus status = PipelineStatus.RUNNING;

        Pipeline(String name, Stage... stages) {
            this.name   = name;
            this.stages = new ArrayList<>(Arrays.asList(stages));
        }

        void run() {
            System.out.println("Pipeline '" + name + "' iniciado\n");
            boolean failed = false;
            for (Stage s : stages) {
                if (failed) {
                    s.status = StageStatus.SKIPPED;
                    System.out.printf("  [%-12s] %s (pipeline fallido)%n", s.name, StageStatus.SKIPPED);
                    continue;
                }
                s.run();
                if (s.status == StageStatus.FAILED) {
                    failed = true;
                    status = PipelineStatus.FAILED;
                }
            }
            if (!failed) status = PipelineStatus.SUCCESS;

            System.out.println("\n--- Resumen ---");
            stages.forEach(s ->
                    System.out.printf("  %-14s %s%n", s.name + ":", s.status));
            System.out.printf("Pipeline status: %s%n", status);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CI/CD Pipeline Stages Demo ===\n");

        Pipeline pipeline = new Pipeline("build-and-deploy",
                new Stage("checkout",  () -> {}),
                new Stage("compile",   () -> {}),
                new Stage("test",      () -> { throw new RuntimeException("3 tests fallaron"); }),
                new Stage("package",   () -> {}),
                new Stage("deploy",    () -> {})
        );

        pipeline.run();

        System.out.println("\n\n=== Pipeline exitoso ===\n");
        Pipeline ok = new Pipeline("full-success",
                new Stage("checkout", () -> {}),
                new Stage("compile",  () -> {}),
                new Stage("test",     () -> {}),
                new Stage("deploy",   () -> {})
        );
        ok.run();
    }
}
