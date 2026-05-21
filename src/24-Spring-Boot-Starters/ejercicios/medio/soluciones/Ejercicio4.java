import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Ejercicio 4 (Medio) — Auto-config report
// Conditions Report: [✓] MATCH y [✗] NO_MATCH con razón
public class Ejercicio4 {

    record ConditionResult(boolean matched, String reason) {
        static ConditionResult match() { return new ConditionResult(true, ""); }
        static ConditionResult noMatch(String reason) { return new ConditionResult(false, reason); }
    }

    interface AutoConfigCandidate {
        String getName();
        ConditionResult evaluate();
        void configure(); // solo se llama si matched
    }

    static class ConditionsReport {
        record Entry(String name, boolean applied, String reason) {}

        private final List<Entry> entries = new ArrayList<>();

        public void run(List<AutoConfigCandidate> candidates) {
            for (AutoConfigCandidate candidate : candidates) {
                ConditionResult result = candidate.evaluate();
                if (result.matched()) {
                    System.out.println("[Evaluating] " + candidate.getName() + " → MATCH");
                    candidate.configure();
                    entries.add(new Entry(candidate.getName(), true, ""));
                } else {
                    System.out.println("[Evaluating] " + candidate.getName()
                            + " → NO_MATCH (" + result.reason() + ")");
                    entries.add(new Entry(candidate.getName(), false, result.reason()));
                }
            }
        }

        public void printReport() {
            System.out.println();
            System.out.println("=== CONDITIONS EVALUATION REPORT ===");
            System.out.println();

            System.out.println("Positive matches (auto-configs ejecutadas):");
            entries.stream()
                   .filter(Entry::applied)
                   .forEach(e -> System.out.println("   [✓] " + e.name()));

            System.out.println();
            System.out.println("Negative matches (auto-configs omitidas):");
            entries.stream()
                   .filter(e -> !e.applied())
                   .forEach(e -> System.out.println("   [✗] " + e.name()
                           + System.lineSeparator() + "        Did not match: " + e.reason()));

            System.out.println();
            long applied = entries.stream().filter(Entry::applied).count();
            System.out.println("Total: " + entries.size()
                    + " evaluadas, " + applied + " aplicadas, "
                    + (entries.size() - applied) + " omitidas");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Auto-config report ===");
        System.out.println();

        List<AutoConfigCandidate> candidates = new ArrayList<>();

        // 1. WebAutoConfig → MATCH (condición cumplida)
        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "WebAutoConfig"; }
            @Override public ConditionResult evaluate() { return ConditionResult.match(); }
            @Override public void configure() {
                System.out.println("  [WebAutoConfig] DispatcherServlet, HandlerMapping configurados");
            }
        });

        // 2. DataSourceAutoConfig → NO_MATCH (propiedad ausente)
        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "DataSourceAutoConfig"; }
            @Override public ConditionResult evaluate() {
                return ConditionResult.noMatch(
                    "@ConditionalOnProperty (spring.datasource.url) did not find property");
            }
            @Override public void configure() {}
        });

        // 3. JacksonAutoConfig → MATCH
        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "JacksonAutoConfig"; }
            @Override public ConditionResult evaluate() { return ConditionResult.match(); }
            @Override public void configure() {
                System.out.println("  [JacksonAutoConfig] ObjectMapper configurado");
            }
        });

        // 4. SecurityAutoConfig → NO_MATCH (clase no en classpath)
        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "SecurityAutoConfig"; }
            @Override public ConditionResult evaluate() {
                return ConditionResult.noMatch(
                    "@ConditionalOnClass did not find required class " +
                    "'org.springframework.security.web.SecurityFilterChain'");
            }
            @Override public void configure() {}
        });

        // 5. ActuatorAutoConfig → MATCH
        candidates.add(new AutoConfigCandidate() {
            @Override public String getName() { return "ActuatorAutoConfig"; }
            @Override public ConditionResult evaluate() { return ConditionResult.match(); }
            @Override public void configure() {
                System.out.println("  [ActuatorAutoConfig] HealthEndpoint, InfoEndpoint configurados");
            }
        });

        System.out.println("--- Evaluando condiciones ---");
        ConditionsReport report = new ConditionsReport();
        report.run(candidates);
        report.printReport();
    }
}
