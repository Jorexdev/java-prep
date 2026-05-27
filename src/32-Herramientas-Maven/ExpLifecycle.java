import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ===== Modelo de fase del ciclo de vida =====

class Phase {
    private final String name;
    private final List<String> goals = new ArrayList<>();

    public Phase(String name, String... boundGoals) {
        this.name = name;
        for (String g : boundGoals) goals.add(g);
    }

    public String getName() { return name; }
    public List<String> getGoals() { return goals; }
}

// ===== Modelo de ciclo de vida =====

class Lifecycle {
    private final String name;
    private final List<Phase> phases;

    public Lifecycle(String name, List<Phase> phases) {
        this.name = name;
        this.phases = phases;
    }

    public String getName() { return name; }

    // Ejecutar hasta una fase objetivo (inclusive), todas las anteriores primero
    public void runUntil(String targetPhase) {
        System.out.println("\n[" + name + "] mvn " + targetPhase);
        boolean found = false;
        for (Phase phase : phases) {
            System.out.print("  → " + phase.getName());
            if (!phase.getGoals().isEmpty()) {
                System.out.print(" [" + String.join(", ", phase.getGoals()) + "]");
            }
            System.out.println(" ✓");
            if (phase.getName().equals(targetPhase)) {
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("  WARN: fase '" + targetPhase + "' no encontrada en el ciclo " + name);
        }
    }

    // Listar todas las fases del ciclo
    public void listPhases() {
        System.out.println("\nCiclo '" + name + "' — fases:");
        for (int i = 0; i < phases.size(); i++) {
            Phase p = phases.get(i);
            String goals = p.getGoals().isEmpty() ? "" : " → " + p.getGoals();
            System.out.printf("  %2d. %s%s%n", i + 1, p.getName(), goals);
        }
    }
}

public class ExpLifecycle {

    // ─── Ciclo default (build principal) ─────────────────────────────────────
    static Lifecycle buildDefaultLifecycle() {
        List<Phase> phases = new ArrayList<>();
        phases.add(new Phase("validate"));
        phases.add(new Phase("initialize"));
        phases.add(new Phase("generate-sources"));
        phases.add(new Phase("process-sources"));
        phases.add(new Phase("compile",  "maven-compiler-plugin:compile"));
        phases.add(new Phase("process-classes"));
        phases.add(new Phase("generate-test-sources"));
        phases.add(new Phase("test-compile", "maven-compiler-plugin:testCompile"));
        phases.add(new Phase("test",     "maven-surefire-plugin:test"));
        phases.add(new Phase("package",  "maven-jar-plugin:jar"));
        phases.add(new Phase("verify",   "maven-failsafe-plugin:verify"));
        phases.add(new Phase("install",  "maven-install-plugin:install"));
        phases.add(new Phase("deploy",   "maven-deploy-plugin:deploy"));
        return new Lifecycle("default", phases);
    }

    // ─── Ciclo clean ─────────────────────────────────────────────────────────
    static Lifecycle buildCleanLifecycle() {
        List<Phase> phases = new ArrayList<>();
        phases.add(new Phase("pre-clean"));
        phases.add(new Phase("clean",    "maven-clean-plugin:clean"));
        phases.add(new Phase("post-clean"));
        return new Lifecycle("clean", phases);
    }

    // ─── Ciclo site ──────────────────────────────────────────────────────────
    static Lifecycle buildSiteLifecycle() {
        List<Phase> phases = new ArrayList<>();
        phases.add(new Phase("pre-site"));
        phases.add(new Phase("site",     "maven-site-plugin:site"));
        phases.add(new Phase("post-site"));
        phases.add(new Phase("site-deploy", "maven-site-plugin:deploy"));
        return new Lifecycle("site", phases);
    }

    public static void main(String[] args) {
        Lifecycle defaultLC = buildDefaultLifecycle();
        Lifecycle cleanLC   = buildCleanLifecycle();
        Lifecycle siteLC    = buildSiteLifecycle();

        // ─── Listar los 3 ciclos de vida ─────────────────────────────────
        System.out.println("════════════════════════════════════════");
        System.out.println(" Los 3 ciclos de vida de Maven");
        System.out.println("════════════════════════════════════════");
        cleanLC.listPhases();
        defaultLC.listPhases();
        siteLC.listPhases();

        // ─── Comparar órdenes de ejecución ───────────────────────────────
        System.out.println("\n════════════════════════════════════════");
        System.out.println(" Órdenes de ejecución por comando");
        System.out.println("════════════════════════════════════════");

        defaultLC.runUntil("test");      // mvn test
        defaultLC.runUntil("package");   // mvn package
        defaultLC.runUntil("verify");    // mvn verify

        // ─── Explicación clave ────────────────────────────────────────────
        System.out.println("\n════════════════════════════════════════");
        System.out.println(" Concepto clave");
        System.out.println("════════════════════════════════════════");
        System.out.println("  • Ejecutar una fase activa TODAS las anteriores en el mismo ciclo.");
        System.out.println("  • 'mvn clean package' ejecuta clean lifecycle + default hasta package.");
        System.out.println("  • Los plugins se unen a fases mediante <executions> en el pom.xml.");
        System.out.println("  • Las fases sin goals enlazados se ejecutan pero no hacen nada visible.");
    }
}
