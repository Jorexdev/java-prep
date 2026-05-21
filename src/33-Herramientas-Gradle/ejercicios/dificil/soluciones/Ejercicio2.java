import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ejercicio2 {

    record TaskMetric(String name, long durationMs, boolean cacheHit) {}
    record TestResult(String suite, int passed, int failed) {}

    static class BuildScan {
        private final List<TaskMetric> taskMetrics = new ArrayList<>();
        private final List<TestResult> testResults = new ArrayList<>();
        private int dependencyCount = 0;
        private long buildStart;

        void start() { buildStart = System.currentTimeMillis(); }

        void recordTask(String name, long durationMs, boolean cacheHit) {
            taskMetrics.add(new TaskMetric(name, durationMs, cacheHit));
        }

        void recordTestResult(String suite, int passed, int failed) {
            testResults.add(new TestResult(suite, passed, failed));
        }

        void setDependencyCount(int count) { this.dependencyCount = count; }

        void print() {
            long totalMs = System.currentTimeMillis() - buildStart;
            int cacheHits   = (int) taskMetrics.stream().filter(TaskMetric::cacheHit).count();
            int cacheMisses = taskMetrics.size() - cacheHits;
            int totalPassed = testResults.stream().mapToInt(TestResult::passed).sum();
            int totalFailed = testResults.stream().mapToInt(TestResult::failed).sum();

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║             BUILD SCAN REPORT            ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.printf("║  Build time:    %6d ms%s║%n", totalMs, " ".repeat(17));
            System.out.printf("║  Tasks:         %6d total%s║%n", taskMetrics.size(), " ".repeat(14));
            System.out.printf("║  Cache hits:    %6d  misses: %3d       ║%n", cacheHits, cacheMisses);
            System.out.printf("║  Dependencies:  %6d%s║%n", dependencyCount, " ".repeat(18));
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  TASK DETAILS                            ║");
            for (TaskMetric m : taskMetrics) {
                String status = m.cacheHit() ? "UP-TO-DATE" : "executed  ";
                System.out.printf("║  %-22s %s %4dms ║%n", ":" + m.name(), status, m.durationMs());
            }
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.printf("║  TESTS: %d passed, %d failed%s║%n",
                totalPassed, totalFailed, " ".repeat(22 - String.valueOf(totalPassed).length() - String.valueOf(totalFailed).length()));
            for (TestResult r : testResults) {
                System.out.printf("║    %-20s +%3d -%3d           ║%n", r.suite(), r.passed(), r.failed());
            }
            System.out.println("╚══════════════════════════════════════════╝");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BuildScan scan = new BuildScan();
        scan.start();
        scan.setDependencyCount(42);

        Random rng = new Random(42);
        String[][] tasks = {
            {"compileJava",        "false"},
            {"processResources",   "true"},
            {"classes",            "true"},
            {"compileTestJava",    "false"},
            {"processTestRes",     "true"},
            {"testClasses",        "true"},
            {"test",               "false"},
            {"jar",                "false"},
        };

        System.out.println("=== Ejecutando build... ===\n");
        for (String[] t : tasks) {
            boolean cached = Boolean.parseBoolean(t[1]);
            long duration = cached ? 0 : 20 + rng.nextInt(80);
            if (!cached) Thread.sleep(duration / 10); // simulate (scaled down)
            System.out.printf("  %-24s %s%n", ":" + t[0], cached ? "UP-TO-DATE" : duration + "ms");
            scan.recordTask(t[0], duration, cached);
        }

        scan.recordTestResult("UnitTests",       47, 0);
        scan.recordTestResult("IntegrationTests", 12, 1);

        scan.print();
    }
}
