import java.util.*;
import java.util.concurrent.*;

public class Ejercicio1 {

    enum BuildStatus { PENDING, RUNNING, SUCCESS, FAILED }

    static class BuildJob {
        String javaVersion;
        String os;
        BuildStatus status = BuildStatus.PENDING;
        long durationMs;

        BuildJob(String javaVersion, String os) {
            this.javaVersion = javaVersion;
            this.os          = os;
        }

        String key() { return javaVersion + " / " + os; }

        void run() {
            status = BuildStatus.RUNNING;
            try {
                long sleep = 50 + new Random().nextInt(150);
                Thread.sleep(sleep);
                durationMs = sleep;
                // Simular fallo ocasional (java17/windows)
                if (javaVersion.equals("java17") && os.equals("windows")) {
                    throw new RuntimeException("Build error: unsupported config");
                }
                status = BuildStatus.SUCCESS;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                status = BuildStatus.FAILED;
            } catch (Exception e) {
                status = BuildStatus.FAILED;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Matrix Build Demo ===\n");

        List<String> javaVersions = List.of("java21", "java17");
        List<String> os           = List.of("linux", "windows");

        List<BuildJob> jobs = new ArrayList<>();
        for (String java : javaVersions) {
            for (String platform : os) {
                jobs.add(new BuildJob(java, platform));
            }
        }

        System.out.println("Combinaciones:");
        jobs.forEach(j -> System.out.printf("  %s%n", j.key()));
        System.out.println("\nEjecutando en paralelo...");

        ExecutorService pool = Executors.newFixedThreadPool(jobs.size());
        List<Future<?>> futures = new ArrayList<>();

        for (BuildJob job : jobs) {
            futures.add(pool.submit(job::run));
        }
        for (Future<?> f : futures) {
            try { f.get(); } catch (ExecutionException e) { /* status set in job */ }
        }
        pool.shutdown();

        // Tabla de resultados
        System.out.println("\n=== Resultados ===");
        System.out.printf("%-10s %-10s %-10s %s%n",
                "Java", "OS", "Status", "Duration");
        System.out.println("-".repeat(45));
        for (BuildJob j : jobs) {
            System.out.printf("%-10s %-10s %-10s %d ms%n",
                    j.javaVersion, j.os, j.status, j.durationMs);
        }

        long success = jobs.stream().filter(j -> j.status == BuildStatus.SUCCESS).count();
        long failed  = jobs.stream().filter(j -> j.status == BuildStatus.FAILED).count();
        System.out.printf("%nTotal: %d SUCCESS, %d FAILED%n", success, failed);
    }
}
