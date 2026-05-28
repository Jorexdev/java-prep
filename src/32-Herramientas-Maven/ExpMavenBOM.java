import java.util.*;

// Simula el mecanismo de BOM (Bill of Materials) de Maven y la regla nearest-wins.
// En Maven real: <dependencyManagement> con <scope>import</scope> importa el BOM.
public class ExpMavenBOM {

    // ── Modelo de grafo de dependencias ───────────────────────────────────────
    record Dep(String artifact, String version, int depth) {
        @Override public String toString() {
            return artifact + ":" + version + " (depth=" + depth + ")";
        }
    }

    // ── 1. REGLA NEAREST-WINS — conflicto de versiones ───────────────────────
    // Maven usa breadth-first search en el árbol de dependencias.
    // La versión más CERCANA a la raíz gana.
    //
    // app (depth=0)
    //   ├─ lib-a:1.0 (depth=1) → depende de jackson:2.13
    //   └─ lib-b:2.0 (depth=1) → depende de jackson:2.15
    //       └─ lib-c:1.0 (depth=2) → depende de jackson:2.14
    //
    // Resultado: jackson:2.13 gana (lib-a es depth=1, primer encontrado en BFS)
    // Solución: declarar jackson explícitamente en el pom raíz (depth=0 siempre gana).
    static void nearestWins() {
        System.out.println("── 1. Nearest-wins — resolución de conflictos ──");

        // BFS simulation: map artifact → primera (más cercana) que se encuentra
        Map<String, Dep> resolved = new LinkedHashMap<>();

        // Orden de visita BFS: depth 0 → 1 → 2
        List<Dep> allDeps = List.of(
            // depth 1 (transitive de libs directas)
            new Dep("jackson-databind", "2.13.0", 1), // de lib-a
            new Dep("jackson-databind", "2.15.0", 1), // de lib-b (mismo depth, primero en BFS gana)
            new Dep("jackson-databind", "2.14.0", 2), // de lib-c (depth mayor → pierde)
            new Dep("slf4j-api",        "1.7.36", 1),
            new Dep("slf4j-api",        "2.0.0",  2)  // pierde
        );

        for (Dep d : allDeps) {
            resolved.merge(d.artifact(), d, (existing, incoming) ->
                incoming.depth() < existing.depth() ? incoming : existing
            );
        }

        System.out.println("  Versiones resueltas (nearest-wins):");
        resolved.values().forEach(d -> System.out.println("  ✓ " + d));

        System.out.println();
        System.out.println("  Para forzar una versión: declararla en <dependencyManagement>");
        System.out.println("  del pom raíz → depth=0, siempre gana.");
    }

    // ── 2. BOM — Bill of Materials ───────────────────────────────────────────
    // Un BOM es un pom.xml con solo <dependencyManagement>: define versiones
    // sin añadir dependencias reales. Importar un BOM fija todas esas versiones
    // para el proyecto que lo importe, sin que cada módulo las repita.
    //
    // spring-boot-dependencies es el BOM más conocido: define ~1500 versiones.
    //
    // Importar BOM:
    // <dependencyManagement>
    //   <dependency>
    //     <groupId>org.springframework.boot</groupId>
    //     <artifactId>spring-boot-dependencies</artifactId>
    //     <version>3.2.0</version>
    //     <type>pom</type>
    //     <scope>import</scope>
    //   </dependency>
    // </dependencyManagement>
    //
    // Usar sin versión (la toma del BOM):
    // <dependency>
    //   <groupId>com.fasterxml.jackson.core</groupId>
    //   <artifactId>jackson-databind</artifactId>
    //   <!-- sin <version> → viene del BOM -->
    // </dependency>
    static void bomDemo() {
        System.out.println("── 2. BOM (Bill of Materials) ──");

        // Simular el BOM de Spring Boot 3.2.0
        Map<String, String> springBootBom = Map.of(
            "jackson-databind",  "2.15.3",
            "slf4j-api",         "2.0.9",
            "logback-classic",   "1.4.14",
            "hibernate-core",    "6.4.1.Final",
            "mockito-core",      "5.7.0",
            "junit-jupiter",     "5.10.1"
        );

        // Dependencias del proyecto: sin versión explícita → tomar del BOM
        List<String> projectDeps = List.of(
            "jackson-databind",
            "slf4j-api",
            "junit-jupiter",
            "guava" // no está en el BOM → error si no se declara versión
        );

        System.out.println("  Dependencias resueltas contra spring-boot-dependencies:3.2.0");
        for (String dep : projectDeps) {
            String version = springBootBom.get(dep);
            if (version != null) {
                System.out.println("  ✓ " + dep + " → " + version + " (del BOM)");
            } else {
                System.out.println("  ✗ " + dep + " → VERSION REQUERIDA (no está en BOM)");
            }
        }
    }

    // ── 3. MAVEN ENFORCER PLUGIN ──────────────────────────────────────────────
    // Falla el build si se violan reglas de proyecto.
    // Reglas más usadas:
    //   - requireJavaVersion: mínimo Java 21
    //   - bannedDependencies: prohibir log4j 1.x
    //   - requireUpperBoundDeps: fuerza que las versiones resueltas >= versiones declaradas
    //   - requireReleaseDeps: prohibir SNAPSHOT en producción
    static class EnforcerRule {
        final String name;
        final String description;
        final boolean passes;

        EnforcerRule(String name, String description, boolean passes) {
            this.name = name; this.description = description; this.passes = passes;
        }
    }

    static void enforcerPlugin() {
        System.out.println("\n── 3. Maven Enforcer Plugin ──");

        String currentJava = "21";
        List<String> deps  = List.of("spring-core:6.1.0", "log4j:1.2.17", "jackson:2.15.0-SNAPSHOT");
        boolean isRelease  = true;

        List<EnforcerRule> rules = List.of(
            new EnforcerRule("requireJavaVersion",
                "Java >= 17 requerido, actual: " + currentJava,
                Integer.parseInt(currentJava) >= 17),

            new EnforcerRule("bannedDependencies",
                "log4j 1.x está prohibido (CVE críticos)",
                deps.stream().noneMatch(d -> d.startsWith("log4j:1."))),

            new EnforcerRule("requireReleaseDeps",
                "Snapshots prohibidos en build de release",
                !isRelease || deps.stream().noneMatch(d -> d.contains("SNAPSHOT")))
        );

        boolean buildFails = false;
        for (EnforcerRule rule : rules) {
            String status = rule.passes ? "PASS" : "FAIL";
            System.out.println("  [" + status + "] " + rule.name + ": " + rule.description);
            if (!rule.passes) buildFails = true;
        }

        System.out.println();
        System.out.println("  Build: " + (buildFails ? "FALLIDO (Enforcer violations)" : "OK"));
    }

    // ── 4. MULTI-MODULE: parent POM + dependencyManagement ────────────────────
    // Parent POM centraliza: versiones (via BOM/dependencyManagement), plugins,
    // propiedades comunes. Los módulos hijos heredan sin repetir versiones.
    //
    // my-project/pom.xml (parent, packaging=pom)
    //   ├─ my-api/pom.xml     (packaging=jar, parent=my-project)
    //   ├─ my-service/pom.xml
    //   └─ my-model/pom.xml
    static void multiModuleStructure() {
        System.out.println("── 4. Multi-module: parent POM + dependencyManagement ──");

        Map<String, String> parentDeps = Map.of(
            "jackson-databind", "2.15.3",
            "spring-core",      "6.1.0",
            "junit-jupiter",    "5.10.1"
        );

        String[] modules = { "my-api", "my-service", "my-model" };
        Map<String, List<String>> moduleDeps = Map.of(
            "my-api",     List.of("jackson-databind", "spring-core"),
            "my-service", List.of("spring-core", "junit-jupiter"),
            "my-model",   List.of("jackson-databind")
        );

        System.out.println("  Parent dependencyManagement (versiones centralizadas):");
        parentDeps.forEach((a, v) -> System.out.println("    " + a + " = " + v));
        System.out.println();

        for (String mod : modules) {
            System.out.println("  Módulo [" + mod + "]:");
            moduleDeps.get(mod).forEach(dep ->
                System.out.println("    " + dep + " → " + parentDeps.get(dep) + " (sin <version> en el módulo)")
            );
        }
        System.out.println();
        System.out.println("  Ventaja: actualizar una versión en el parent actualiza todos los módulos.");
    }

    public static void main(String[] args) {
        nearestWins();
        System.out.println();
        bomDemo();
        enforcerPlugin();
        multiModuleStructure();
    }
}
