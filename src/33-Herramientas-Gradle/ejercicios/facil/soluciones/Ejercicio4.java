import java.util.ArrayList;
import java.util.List;

public class Ejercicio4 {

    enum Configuration { API, IMPLEMENTATION, TEST_IMPLEMENTATION }

    static class Dependency {
        final String name;
        final Configuration config;

        Dependency(String name, Configuration config) {
            this.name = name;
            this.config = config;
        }

        @Override
        public String toString() {
            return String.format("%-40s (%s)", name, config.name().toLowerCase().replace("_", ""));
        }
    }

    static class GradleProject {
        private final List<Dependency> dependencies = new ArrayList<>();

        void addDep(String name, Configuration config) {
            dependencies.add(new Dependency(name, config));
        }

        // compile classpath: api + implementation
        List<Dependency> compileClasspath() {
            return dependencies.stream()
                    .filter(d -> d.config == Configuration.API
                              || d.config == Configuration.IMPLEMENTATION)
                    .toList();
        }

        // runtime classpath: api + implementation (sin testImplementation)
        List<Dependency> runtimeClasspath() {
            return dependencies.stream()
                    .filter(d -> d.config == Configuration.API
                              || d.config == Configuration.IMPLEMENTATION)
                    .toList();
        }

        // test classpath: todo
        List<Dependency> testClasspath() {
            return new ArrayList<>(dependencies);
        }

        // Classpath expuesto a consumidores (solo api es transitivo)
        List<Dependency> transitiveForConsumers() {
            return dependencies.stream()
                    .filter(d -> d.config == Configuration.API)
                    .toList();
        }

        void printClasspath(String name, List<Dependency> classpath) {
            System.out.println("=== " + name + " (" + classpath.size() + " entries) ===");
            classpath.forEach(d -> System.out.println("  " + d));
            System.out.println();
        }
    }

    public static void main(String[] args) {
        GradleProject project = new GradleProject();

        // api — transitivo: libs que son parte de la API pública
        project.addDep("com.google.guava:guava:33.0",           Configuration.API);
        project.addDep("org.slf4j:slf4j-api:2.0.9",            Configuration.API);

        // implementation — no transitivo: detalles de implementación
        project.addDep("ch.qos.logback:logback-classic:1.4",   Configuration.IMPLEMENTATION);
        project.addDep("com.fasterxml.jackson:jackson:2.16",    Configuration.IMPLEMENTATION);

        // testImplementation — solo en test
        project.addDep("org.junit.jupiter:junit-jupiter:5.10",  Configuration.TEST_IMPLEMENTATION);
        project.addDep("org.mockito:mockito-core:5.7",          Configuration.TEST_IMPLEMENTATION);

        System.out.println("=== Dependencias declaradas ===");
        project.dependencies.forEach(System.out::println);
        System.out.println();

        project.printClasspath("compileClasspath", project.compileClasspath());
        project.printClasspath("runtimeClasspath", project.runtimeClasspath());
        project.printClasspath("testClasspath",    project.testClasspath());
        project.printClasspath("Transitivo para consumidores (solo api)", project.transitiveForConsumers());

        System.out.println("=== Diferencia api vs implementation ===");
        System.out.println("  api            -> visible para proyectos que dependen de este módulo");
        System.out.println("  implementation -> encapsulado, NO visible para consumidores");
        System.out.println("  Uso de api: tipos que aparecen en métodos públicos");
        System.out.println("  Uso de implementation: lógica interna, drivers, frameworks internos");
    }
}
