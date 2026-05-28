import java.util.*;
import java.util.concurrent.*;

// Simula pipelines CI/CD con stages paralelos y fan-out/fan-in.
// En GitHub Actions: jobs con needs:[] corren en paralelo; con needs:[job-a] esperan.
public class ExpParallelStages {

    // ── Modelo de pipeline ────────────────────────────────────────────────────
    enum StageResult { SUCCESS, FAILURE, SKIPPED }

    static class Stage {
        final String name;
        final long durationMs;
        final boolean failing;

        Stage(String name, long durationMs, boolean failing) {
            this.name = name; this.durationMs = durationMs; this.failing = failing;
        }

        StageResult run() {
            try { Thread.sleep(durationMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            StageResult r = failing ? StageResult.FAILURE : StageResult.SUCCESS;
            System.out.printf("  [%4dms] %-25s → %s%n", durationMs, name, r);
            return r;
        }
    }

    // ── 1. PIPELINE SECUENCIAL — tiempo total = suma de todos ────────────────
    // GitHub Actions: jobs sin 'needs' en el mismo workflow son paralelos por defecto.
    // Hacerlos secuenciales requiere needs: [anterior].
    static void sequentialPipeline() throws Exception {
        System.out.println("── 1. Pipeline secuencial ──");

        List<Stage> stages = List.of(
            new Stage("checkout",        50, false),
            new Stage("compile",        200, false),
            new Stage("unit-tests",     300, false),
            new Stage("integration-tests", 400, false),
            new Stage("build-image",    150, false),
            new Stage("deploy-staging",  200, false)
        );

        long inicio = System.currentTimeMillis();
        for (Stage s : stages) {
            if (s.run() == StageResult.FAILURE) {
                System.out.println("  PIPELINE FALLIDO en: " + s.name);
                break;
            }
        }
        System.out.println("  Tiempo total: " + (System.currentTimeMillis() - inicio) + "ms");
        System.out.println("  (= suma de todos los stages)");
    }

    // ── 2. FAN-OUT / FAN-IN — stages independientes en paralelo ─────────────
    // Fan-out: lanzar varios jobs independientes al mismo tiempo.
    // Fan-in:  esperar a que todos terminen antes de continuar.
    //
    // GitHub Actions:
    //   test-unit:     needs: [build]
    //   test-e2e:      needs: [build]
    //   test-security: needs: [build]
    //   deploy:        needs: [test-unit, test-e2e, test-security]  ← fan-in
    static void fanOutFanIn() throws Exception {
        System.out.println("\n── 2. Fan-out / Fan-in (paralelo) ──");

        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

        // Stage 1: checkout (secuencial — necesario antes de todo)
        Stage checkout = new Stage("checkout", 50, false);
        checkout.run();

        // Fan-out: 3 tipos de test independientes en paralelo
        long inicio = System.currentTimeMillis();
        List<Stage> parallelStages = List.of(
            new Stage("unit-tests",      300, false),
            new Stage("e2e-tests",       500, false),
            new Stage("security-scan",   400, false)
        );

        List<Future<StageResult>> futures = parallelStages.stream()
            .map(s -> exec.submit(s::run))
            .toList();

        // Fan-in: esperar todos
        boolean allPassed = true;
        for (Future<StageResult> f : futures) {
            if (f.get() == StageResult.FAILURE) allPassed = false;
        }
        long parallelTime = System.currentTimeMillis() - inicio;

        System.out.println("  Tiempo paralelo: " + parallelTime + "ms (= el stage más lento)");
        System.out.println("  Tiempo secuencial habría sido: " + parallelStages.stream().mapToLong(s -> s.durationMs).sum() + "ms");

        // Stage final (deploy): solo si todos pasaron
        if (allPassed) {
            new Stage("deploy-staging", 150, false).run();
        }

        exec.shutdown();
    }

    // ── 3. FAST-FAIL — abortar en cuanto falla un stage crítico ─────────────
    // En GitHub Actions: if: ${{ failure() }} para steps de cleanup.
    // Si un test falla → no tiene sentido hacer build ni deploy.
    static void fastFail() throws Exception {
        System.out.println("\n── 3. Fast-fail pipeline ──");

        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

        List<Stage> parallelTests = List.of(
            new Stage("unit-tests",     200, false),
            new Stage("integration-tests", 150, true),   // ← este falla
            new Stage("lint",           100, false)
        );

        List<Future<StageResult>> futures = parallelTests.stream()
            .map(s -> exec.submit(s::run))
            .toList();

        String failedStage = null;
        for (int i = 0; i < futures.size(); i++) {
            if (futures.get(i).get() == StageResult.FAILURE) {
                failedStage = parallelTests.get(i).name;
            }
        }

        if (failedStage != null) {
            System.out.println("  FAST-FAIL: pipeline abortado por fallo en '" + failedStage + "'");
            System.out.println("  Stages posteriores (build-image, deploy): SKIPPED");
        }

        exec.shutdown();
    }

    // ── 4. MATRIX BUILDS — mismo job para múltiples configuraciones ───────────
    // GitHub Actions:
    //   strategy:
    //     matrix:
    //       java: [17, 21]
    //       os: [ubuntu, windows]
    //   → 4 jobs en paralelo (2 java × 2 os)
    static void matrixBuilds() throws Exception {
        System.out.println("\n── 4. Matrix builds ──");

        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
        String[] javaVersions = { "17", "21" };
        String[] oses         = { "ubuntu", "windows" };

        List<Future<StageResult>> matrix = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (String java : javaVersions) {
            for (String os : oses) {
                String label = "java-" + java + "/" + os;
                labels.add(label);
                matrix.add(exec.submit(() -> {
                    long duration = 200 + (long)(Math.random() * 100);
                    try { Thread.sleep(duration); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    System.out.printf("  %-20s compilado en %dms%n", label, duration);
                    return StageResult.SUCCESS;
                }));
            }
        }

        CompletableFuture.allOf(matrix.stream()
            .map(f -> CompletableFuture.runAsync(() -> { try { f.get(); } catch (Exception ignored) {} }, exec))
            .toArray(CompletableFuture[]::new)).join();

        System.out.println("  " + matrix.size() + " combinaciones ejecutadas en paralelo");
        exec.shutdown();
    }

    public static void main(String[] args) throws Exception {
        sequentialPipeline();
        fanOutFanIn();
        fastFail();
        matrixBuilds();
    }
}
