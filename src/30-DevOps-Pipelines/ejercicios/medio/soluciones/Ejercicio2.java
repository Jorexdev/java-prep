import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Ejercicio2 {

    enum StageStatus { PENDING, RUNNING, PASSED, FAILED }

    static class Stage {
        String name;
        Runnable task;
        StageStatus status = StageStatus.PENDING;

        Stage(String name, Runnable task) {
            this.name = name;
            this.task = task;
        }

        void run() {
            status = StageStatus.RUNNING;
            System.out.printf("  [%-16s] iniciado  (thread=%s)%n",
                    name, Thread.currentThread().getName());
            try {
                task.run();
                status = StageStatus.PASSED;
                System.out.printf("  [%-16s] PASSED%n", name);
            } catch (Exception e) {
                status = StageStatus.FAILED;
                System.out.printf("  [%-16s] FAILED: %s%n", name, e.getMessage());
            }
        }
    }

    static class ParallelGroup {
        String name;
        List<Stage> stages;

        ParallelGroup(String name, List<Stage> stages) {
            this.name   = name;
            this.stages = new ArrayList<>(stages);
        }

        boolean run() throws InterruptedException {
            System.out.printf("Grupo paralelo '%s' iniciado (%d stages):%n",
                    name, stages.size());

            AtomicBoolean anyFailed = new AtomicBoolean(false);
            ExecutorService pool    = Executors.newFixedThreadPool(stages.size());
            List<Future<?>> futures = new ArrayList<>();

            for (Stage s : stages) {
                futures.add(pool.submit(() -> {
                    s.run();
                    if (s.status == StageStatus.FAILED) anyFailed.set(true);
                }));
            }

            for (Future<?> f : futures) {
                try { f.get(); } catch (ExecutionException e) { anyFailed.set(true); }
            }
            pool.shutdown();

            boolean ok = !anyFailed.get();
            System.out.printf("Grupo '%s': %s%n", name, ok ? "PASSED" : "FAILED");
            return ok;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Parallel Stages Demo ===\n");

        List<Stage> stages = List.of(
                new Stage("lint",        () -> { try { Thread.sleep(80); } catch (InterruptedException e) {} }),
                new Stage("unit-tests",  () -> { try { Thread.sleep(120); } catch (InterruptedException e) {} }),
                new Stage("sec-scan",    () -> {
                    try { Thread.sleep(60); } catch (InterruptedException e) {}
                    throw new RuntimeException("vulnerability found: CVE-2024-1234");
                }),
                new Stage("sast",        () -> { try { Thread.sleep(90); } catch (InterruptedException e) {} })
        );

        ParallelGroup group = new ParallelGroup("quality-checks", stages);
        boolean passed = group.run();

        System.out.println("\nResumen de stages:");
        stages.forEach(s ->
                System.out.printf("  %-16s %s%n", s.name, s.status));

        System.out.printf("%nPipeline: %s%n", passed ? "continúa" : "ABORTADO (fallo en grupo paralelo)");
    }
}
