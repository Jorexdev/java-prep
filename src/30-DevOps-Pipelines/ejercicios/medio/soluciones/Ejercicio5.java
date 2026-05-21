import java.util.*;

public class Ejercicio5 {

    static class TestResult {
        String suite;
        int passed;
        int failed;
        int skipped;

        TestResult(String suite, int passed, int failed, int skipped) {
            this.suite   = suite;
            this.passed  = passed;
            this.failed  = failed;
            this.skipped = skipped;
        }

        int total() { return passed + failed + skipped; }

        void print() {
            System.out.printf("  %-28s passed=%-4d failed=%-4d skipped=%-4d total=%d%n",
                    suite, passed, failed, skipped, total());
        }
    }

    static class TestReport {
        List<TestResult> results;

        TestReport(List<TestResult> results) {
            this.results = new ArrayList<>(results);
        }

        int totalPassed()  { return results.stream().mapToInt(r -> r.passed).sum(); }
        int totalFailed()  { return results.stream().mapToInt(r -> r.failed).sum(); }
        int totalSkipped() { return results.stream().mapToInt(r -> r.skipped).sum(); }
        int total()        { return totalPassed() + totalFailed() + totalSkipped(); }

        double successRate() {
            return total() == 0 ? 0 : (double) totalPassed() / total() * 100;
        }

        List<TestResult> failures() {
            return results.stream().filter(r -> r.failed > 0).toList();
        }

        void print() {
            System.out.println("=== Test Report ===\n");
            System.out.printf("  %-28s %-10s %-10s %-10s %s%n",
                    "Suite", "Passed", "Failed", "Skipped", "Total");
            System.out.println("  " + "-".repeat(70));
            results.forEach(TestResult::print);
            System.out.println("  " + "-".repeat(70));
            System.out.printf("  %-28s %-10d %-10d %-10d %d%n",
                    "TOTAL", totalPassed(), totalFailed(), totalSkipped(), total());

            System.out.printf("%n  Tasa de éxito: %.1f%%%n", successRate());

            List<TestResult> fails = failures();
            if (fails.isEmpty()) {
                System.out.println("\n  Todos los tests pasaron.");
            } else {
                System.out.println("\n  Suites con fallos:");
                fails.forEach(r ->
                        System.out.printf("    ✗ %s: %d failed%n", r.suite, r.failed));
            }
        }
    }

    public static void main(String[] args) {
        List<TestResult> results = List.of(
                new TestResult("UnitTests — Domain",       145, 0, 3),
                new TestResult("UnitTests — Services",      89, 4, 1),
                new TestResult("IntegrationTests — API",    34, 0, 2),
                new TestResult("IntegrationTests — DB",     28, 2, 0)
        );

        TestReport report = new TestReport(results);
        report.print();
    }
}
