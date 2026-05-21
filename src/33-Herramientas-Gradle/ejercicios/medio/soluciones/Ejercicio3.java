import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Ejercicio3 {

    // Dependencia externa (coordinadas de Maven/Gradle)
    record ExternalDependency(String group, String name, String version) {
        String coordinates() { return group + ":" + name + ":" + version; }
        String key() { return group + ":" + name; }
    }

    // Referencia a un proyecto local del build
    record ProjectReference(String projectPath) {
        @Override public String toString() { return "project(\"" + projectPath + "\")"; }
    }

    // Resultado de la resolución: puede ser externa o local
    sealed interface ResolvedDep permits ResolvedDep.External, ResolvedDep.Local {
        record External(ExternalDependency dep) implements ResolvedDep {}
        record Local(ProjectReference ref, String originalCoordinates) implements ResolvedDep {}
    }

    static class DependencySubstitution {
        private final List<SubstitutionRule> rules = new ArrayList<>();

        record SubstitutionRule(String targetKey, ProjectReference replacement, String reason) {}

        void substitute(String coordinates, ProjectReference with, String reason) {
            String[] parts = coordinates.split(":");
            String key = parts[0] + ":" + parts[1];
            rules.add(new SubstitutionRule(key, with, reason));
            System.out.printf("[config] Sustitución registrada: %s -> %s (%s)%n",
                    coordinates, with, reason);
        }

        ResolvedDep resolve(ExternalDependency dep) {
            Optional<SubstitutionRule> rule = rules.stream()
                    .filter(r -> r.targetKey().equals(dep.key()))
                    .findFirst();
            if (rule.isPresent()) {
                return new ResolvedDep.Local(rule.get().replacement(), dep.coordinates());
            }
            return new ResolvedDep.External(dep);
        }
    }

    static class GradleProject {
        final String name;
        final List<ExternalDependency> declared = new ArrayList<>();
        final DependencySubstitution substitution = new DependencySubstitution();

        GradleProject(String name) { this.name = name; }

        void addDep(String coordinates) {
            String[] p = coordinates.split(":");
            declared.add(new ExternalDependency(p[0], p[1], p[2]));
        }

        List<ResolvedDep> resolveAll() {
            List<ResolvedDep> resolved = new ArrayList<>();
            for (ExternalDependency dep : declared) {
                resolved.add(substitution.resolve(dep));
            }
            return resolved;
        }
    }

    public static void main(String[] args) {
        GradleProject project = new GradleProject("my-app");

        // Dependencias declaradas en build.gradle
        project.addDep("com.example:shared-lib:1.0");
        project.addDep("com.example:utils:2.3");
        project.addDep("org.springframework:spring-context:6.1.2");
        project.addDep("com.example:data-access:1.5");
        project.addDep("org.slf4j:slf4j-api:2.0.9");

        System.out.println("=== Dependencias declaradas ===");
        project.declared.forEach(d -> System.out.println("  " + d.coordinates()));
        System.out.println();

        // Configurar sustituciones: módulos locales en desarrollo
        System.out.println("=== Configurando sustituciones ===");
        project.substitution.substitute(
            "com.example:shared-lib:1.0",
            new ProjectReference(":shared-lib"),
            "usando versión local en desarrollo"
        );
        project.substitution.substitute(
            "com.example:data-access:1.5",
            new ProjectReference(":data-access"),
            "trabajando en cambios locales de data-access"
        );

        System.out.println();
        System.out.println("=== Grafo de dependencias ANTES de la sustitución ===");
        project.declared.forEach(d -> System.out.println("  external: " + d.coordinates()));
        System.out.println();

        System.out.println("=== Grafo de dependencias DESPUÉS de la sustitución ===");
        List<ResolvedDep> resolved = project.resolveAll();
        for (ResolvedDep dep : resolved) {
            switch (dep) {
                case ResolvedDep.External e ->
                    System.out.println("  [external] " + e.dep().coordinates());
                case ResolvedDep.Local l ->
                    System.out.printf("  [local   ] %s  <- sustituye '%s'%n",
                            l.ref(), l.originalCoordinates());
            }
        }

        System.out.println();
        long localCount    = resolved.stream().filter(d -> d instanceof ResolvedDep.Local).count();
        long externalCount = resolved.stream().filter(d -> d instanceof ResolvedDep.External).count();
        System.out.printf("Total: %d externa(s), %d local(es)%n", externalCount, localCount);
    }
}
