import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Ejercicio6 {

    enum JobStatus { PENDING, RUNNING, SUCCESS, FAILED }

    static class MatrixJob {
        String os;
        String javaVersion;
        JobStatus status = JobStatus.PENDING;
        long durationMs  = 0;

        MatrixJob(String os, String javaVersion) {
            this.os          = os;
            this.javaVersion = javaVersion;
        }

        String label() {
            return os + " / java-" + javaVersion;
        }
    }

    static class MatrixPipeline {
        List<String> osList;
        List<String> javaVersions;
        Random random = new Random(42); // seed fijo para reproducibilidad

        MatrixPipeline(List<String> osList, List<String> javaVersions) {
            this.osList       = osList;
            this.javaVersions = javaVersions;
        }

        List<MatrixJob> generateMatrix() {
            List<MatrixJob> jobs = new ArrayList<>();
            for (String os : osList) {
                for (String java : javaVersions) {
                    jobs.add(new MatrixJob(os, java));
                }
            }
            return jobs;
        }

        void run(List<MatrixJob> jobs) throws InterruptedException {
            System.out.printf("=== Matrix Strategy: %d OS × %d Java = %d jobs ===%n%n",
                    osList.size(), javaVersions.size(), jobs.size());

            ExecutorService pool = Executors.newFixedThreadPool(jobs.size());
            CountDownLatch latch = new CountDownLatch(jobs.size());

            long startAll = System.currentTimeMillis();

            for (MatrixJob job : jobs) {
                pool.submit(() -> {
                    job.status = JobStatus.RUNNING;
                    long t0 = System.currentTimeMillis();
                    try {
                        // Simula duración aleatoria 100–300 ms
                        int sleep = 100 + random.nextInt(201);
                        Thread.sleep(sleep);
                        // 10% probabilidad de fallo
                        if (random.nextInt(10) == 0) {
                            throw new RuntimeException("Build failed: compilación rota");
                        }
                        job.status = JobStatus.SUCCESS;
                    } catch (Exception e) {
                        job.status = JobStatus.FAILED;
                    } finally {
                        job.durationMs = System.currentTimeMillis() - t0;
                        latch.countDown();
                    }
                });
            }

            latch.await();
            pool.shutdown();

            long totalMs = System.currentTimeMillis() - startAll;

            printTable(jobs, totalMs);
        }

        void printTable(List<MatrixJob> jobs, long totalMs) {
            System.out.println("=== Resultados de la Matrix ===");
            System.out.printf("  %-26s | %-8s | %s%n", "Job", "Duración", "Estado");
            System.out.println("  " + "-".repeat(52));

            AtomicInteger success = new AtomicInteger();
            AtomicInteger failed  = new AtomicInteger();

            for (MatrixJob job : jobs) {
                String icon = job.status == JobStatus.SUCCESS ? "OK" : "FAIL";
                System.out.printf("  %-26s | %5dms  | %s%n",
                        job.label(), job.durationMs, icon);
                if (job.status == JobStatus.SUCCESS) success.incrementAndGet();
                else failed.incrementAndGet();
            }

            System.out.println("  " + "-".repeat(52));
            System.out.printf("  Total: %d SUCCESS, %d FAILED | Tiempo paralelo: %dms%n",
                    success.get(), failed.get(), totalMs);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MatrixPipeline pipeline = new MatrixPipeline(
                List.of("linux", "windows", "macos"),
                List.of("17", "21")
        );

        List<MatrixJob> matrix = pipeline.generateMatrix();
        System.out.println("Combinaciones generadas:");
        matrix.forEach(j -> System.out.println("  - " + j.label()));
        System.out.println();

        pipeline.run(matrix);
    }
}
