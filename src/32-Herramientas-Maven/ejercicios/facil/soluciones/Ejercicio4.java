import java.util.ArrayList;
import java.util.List;

public class Ejercicio4 {

    enum Scope { COMPILE, TEST, RUNTIME, PROVIDED }

    static class Dependency {
        final String name;
        final Scope scope;

        Dependency(String name, Scope scope) {
            this.name = name;
            this.scope = scope;
        }

        @Override
        public String toString() {
            return String.format("%-35s (%s)", name, scope.name().toLowerCase());
        }
    }

    static class ClasspathResolver {
        private final List<Dependency> dependencies;

        ClasspathResolver(List<Dependency> dependencies) {
            this.dependencies = dependencies;
        }

        // compile classpath: compile + provided
        List<Dependency> compileClasspath() {
            return dependencies.stream()
                    .filter(d -> d.scope == Scope.COMPILE || d.scope == Scope.PROVIDED)
                    .toList();
        }

        // runtime classpath: compile + runtime (sin provided)
        List<Dependency> runtimeClasspath() {
            return dependencies.stream()
                    .filter(d -> d.scope == Scope.COMPILE || d.scope == Scope.RUNTIME)
                    .toList();
        }

        // test classpath: compile + test + runtime (todo excepto provided)
        List<Dependency> testClasspath() {
            return dependencies.stream()
                    .filter(d -> d.scope != Scope.PROVIDED)
                    .toList();
        }

        void printClasspath(String mode, List<Dependency> classpath) {
            System.out.println("=== " + mode + " classpath (" + classpath.size() + " entries) ===");
            classpath.forEach(d -> System.out.println("  " + d));
            System.out.println();
        }
    }

    public static void main(String[] args) {
        List<Dependency> pom = List.of(
            new Dependency("com.google.guava:guava:33.0",       Scope.COMPILE),
            new Dependency("org.slf4j:slf4j-api:2.0.9",         Scope.COMPILE),
            new Dependency("ch.qos.logback:logback-classic:1.4",Scope.RUNTIME),
            new Dependency("javax.servlet:servlet-api:4.0",     Scope.PROVIDED),
            new Dependency("org.junit.jupiter:junit-5.10",      Scope.TEST),
            new Dependency("org.mockito:mockito-core:5.7",       Scope.TEST)
        );

        System.out.println("=== POM declarations ===");
        pom.forEach(System.out::println);
        System.out.println();

        ClasspathResolver resolver = new ClasspathResolver(pom);

        resolver.printClasspath("compile", resolver.compileClasspath());
        resolver.printClasspath("runtime", resolver.runtimeClasspath());
        resolver.printClasspath("test",    resolver.testClasspath());

        System.out.println("=== Reglas de scope ===");
        System.out.println("  compile  -> compile + runtime classpath");
        System.out.println("  test     -> solo test classpath");
        System.out.println("  runtime  -> solo runtime classpath");
        System.out.println("  provided -> solo compile classpath (contenedor lo provee en runtime)");
    }
}
