import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Ejercicio 2 (Difícil) — Condition override (exclude)
// ExcludeAutoConfiguration: auto-configs en la lista NO se ejecutan
// aunque sus condiciones se cumplan
public class Ejercicio2 {

    record ConditionResult(boolean matched, String reason) {
        static ConditionResult match() { return new ConditionResult(true, ""); }
        static ConditionResult noMatch(String reason) { return new ConditionResult(false, reason); }
    }

    interface AutoConfigCandidate {
        String getName();
        ConditionResult evaluate();
        void configure();
    }

    enum SkipReason { NONE, CONDITION_NOT_MET, EXCLUDED_BY_USER }

    record ReportEntry(String name, boolean applied, SkipReason skipReason, String detail) {}

    static class AutoConfigProcessor {
        private final Set<String> excluded;
        private final List<ReportEntry> report = new ArrayList<>();

        AutoConfigProcessor(List<String> excluded) {
            this.excluded = Set.copyOf(excluded);
        }

        public void process(List<AutoConfigCandidate> candidates) {
            for (AutoConfigCandidate candidate : candidates) {
                String name = candidate.getName();

                // 1. Comprobar exclusión explícita (mayor prioridad)
                if (excluded.contains(name)) {
                    System.out.println("[Processor] " + name + " → SKIPPED (Excluded by user)");
                    report.add(new ReportEntry(name, false,
                            SkipReason.EXCLUDED_BY_USER, "Excluded by user via @SpringBootApplication(exclude)"));
                    continue;
                }

                // 2. Evaluar condición
                ConditionResult result = candidate.evaluate();
                if (!result.matched()) {
                    System.out.println("[Processor] " + name + " → SKIPPED (" + result.reason() + ")");
                    report.add(new ReportEntry(name, false,
                            SkipReason.CONDITION_NOT_MET, result.reason()));
                    continue;
                }

                // 3. Ejecutar
                System.out.println("[Processor] " + name + " → APPLYING");
                candidate.configure();
                report.add(new ReportEntry(name, true, SkipReason.NONE, ""));
            }
        }

        public void printReport() {
            System.out.println();
            System.out.println("=== CONDITIONS EVALUATION REPORT ===");
            System.out.println();

            System.out.println("Positive matches:");
            report.stream()
                  .filter(ReportEntry::applied)
                  .forEach(e -> System.out.println("   [✓] " + e.name()));

            System.out.println();
            System.out.println("Excluded by user:");
            report.stream()
                  .filter(e -> e.skipReason() == SkipReason.EXCLUDED_BY_USER)
                  .forEach(e -> System.out.println("   [✗] " + e.name()
                          + System.lineSeparator() + "        " + e.detail()));

            System.out.println();
            System.out.println("Negative matches (condition failed):");
            report.stream()
                  .filter(e -> e.skipReason() == SkipReason.CONDITION_NOT_MET)
                  .forEach(e -> System.out.println("   [✗] " + e.name()
                          + System.lineSeparator() + "        " + e.detail()));

            System.out.println();
            long applied = report.stream().filter(ReportEntry::applied).count();
            long excluded = report.stream().filter(e -> e.skipReason() == SkipReason.EXCLUDED_BY_USER).count();
            long condFailed = report.stream().filter(e -> e.skipReason() == SkipReason.CONDITION_NOT_MET).count();
            System.out.println("Resumen: " + report.size() + " total | "
                    + applied + " aplicadas | "
                    + excluded + " excluidas | "
                    + condFailed + " condición fallida");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Condition override (ExcludeAutoConfiguration) ===");
        System.out.println();

        // 5 auto-configs candidatas
        List<AutoConfigCandidate> candidates = new ArrayList<>();

        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "WebAutoConfig"; }
            @Override public ConditionResult evaluate() { return ConditionResult.match(); }
            @Override public void configure() {
                System.out.println("  [WebAutoConfig] DispatcherServlet configurado");
            }
        });

        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "DataSourceAutoConfig"; }
            @Override public ConditionResult evaluate() { return ConditionResult.match(); }
            @Override public void configure() {
                System.out.println("  [DataSourceAutoConfig] HikariCP configurado");
            }
        });

        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "SecurityAutoConfig"; }
            @Override public ConditionResult evaluate() { return ConditionResult.match(); }
            @Override public void configure() {
                System.out.println("  [SecurityAutoConfig] SecurityFilterChain configurado");
            }
        });

        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "ActuatorAutoConfig"; }
            @Override public ConditionResult evaluate() { return ConditionResult.match(); }
            @Override public void configure() {
                System.out.println("  [ActuatorAutoConfig] Endpoints configurados");
            }
        });

        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "CacheAutoConfig"; }
            @Override public ConditionResult evaluate() {
                return ConditionResult.noMatch(
                    "@ConditionalOnProperty (spring.cache.type) not set");
            }
            @Override public void configure() {}
        });

        // Excluir 2 auto-configs explícitamente
        // (equivale a @SpringBootApplication(exclude={DataSourceAutoConfig.class, SecurityAutoConfig.class}))
        List<String> excluded = List.of("DataSourceAutoConfig", "SecurityAutoConfig");
        System.out.println("Exclusiones configuradas por el usuario:");
        excluded.forEach(e -> System.out.println("  @exclude: " + e));
        System.out.println();

        System.out.println("--- Procesando auto-configs ---");
        AutoConfigProcessor processor = new AutoConfigProcessor(excluded);
        processor.process(candidates);
        processor.printReport();
    }
}
