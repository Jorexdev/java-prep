import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio2 {

    enum Scope { COMPILE, TEST, RUNTIME, PROVIDED }

    static class Dependency {
        final String groupId;
        final String artifactId;
        final String version;
        final Scope scope;

        Dependency(String groupId, String artifactId, String version, Scope scope) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.scope = scope;
        }

        String coordinates() {
            return groupId + ":" + artifactId + ":" + version;
        }

        @Override
        public String toString() {
            return String.format("%-45s [%s]", coordinates(), scope.name().toLowerCase());
        }
    }

    public static void main(String[] args) {
        // POM simulado con 8 dependencias en distintos scopes
        List<Dependency> pom = List.of(
            new Dependency("org.springframework",  "spring-context",  "6.1.2",  Scope.COMPILE),
            new Dependency("org.springframework",  "spring-web",      "6.1.2",  Scope.COMPILE),
            new Dependency("com.fasterxml.jackson","jackson-databind","2.16.0", Scope.COMPILE),
            new Dependency("org.slf4j",            "slf4j-api",       "2.0.9",  Scope.COMPILE),
            new Dependency("ch.qos.logback",       "logback-classic", "1.4.14", Scope.RUNTIME),
            new Dependency("javax.servlet",        "javax.servlet-api","4.0.1", Scope.PROVIDED),
            new Dependency("org.junit.jupiter",    "junit-jupiter",   "5.10.1", Scope.TEST),
            new Dependency("org.mockito",          "mockito-core",    "5.7.0",  Scope.TEST)
        );

        System.out.println("=== POM — todas las dependencias ===");
        pom.forEach(System.out::println);
        System.out.println();

        // Agrupar por scope
        Map<Scope, List<Dependency>> byScope = pom.stream()
                .collect(Collectors.groupingBy(d -> d.scope));

        System.out.println("=== Dependencias agrupadas por scope ===");
        byScope.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().name()))
                .forEach(entry -> {
                    System.out.println("[" + entry.getKey().name().toLowerCase() + "]");
                    entry.getValue().forEach(d -> System.out.println("  " + d.coordinates()));
                    System.out.println();
                });

        System.out.println("=== Resumen ===");
        byScope.forEach((scope, deps) ->
            System.out.printf("  %-10s: %d dependencia(s)%n", scope.name().toLowerCase(), deps.size()));
    }
}
