import java.util.ArrayList;
<parameter name="content">import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio2 {

    static class BOM {
        final String name;
        final Map<String, String> managedVersions; // artifactId -> version

        BOM(String name, Map<String, String> managedVersions) {
            this.name = name;
            this.managedVersions = managedVersions;
        }
    }

    static class Dependency {
        final String groupId;
        final String artifactId;
        final String version; // null = sin versión explícita (debe venir del BOM)

        Dependency(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        String key() { return artifactId; }
    }

    static class Pom {
        final String artifactId;
        final List<Dependency> dependencies;
        final BOM importedBom;

        Pom(String artifactId, List<Dependency> dependencies, BOM importedBom) {
            this.artifactId = artifactId;
            this.dependencies = dependencies;
            this.importedBom = importedBom;
        }

        // Resuelve la versión de una dependencia: explícita > BOM
        String resolveVersion(Dependency dep) {
            if (dep.version != null) return dep.version;
            if (importedBom != null && importedBom.managedVersions.containsKey(dep.key())) {
                return importedBom.managedVersions.get(dep.key());
            }
            return "UNRESOLVED";
        }

        void printEffective() {
            System.out.println("=== POM: " + artifactId + " ===");
            if (importedBom != null) {
                System.out.println("BOM importado: " + importedBom.name);
            }
            System.out.println();
            System.out.printf("%-35s %-15s %-10s%n", "dependency", "version", "source");
            System.out.println("-".repeat(65));
            for (Dependency dep : dependencies) {
                String resolved = resolveVersion(dep);
                String source = dep.version != null ? "explicit" :
                        (importedBom != null && importedBom.managedVersions.containsKey(dep.key()) ? "BOM" : "MISSING");
                System.out.printf("%-35s %-15s %-10s%n",
                        dep.groupId + ":" + dep.artifactId, resolved, source);
            }
        }
    }

    public static void main(String[] args) {
        // Spring BOM gestiona las versiones de componentes Spring
        BOM springBom = new BOM("spring-framework-bom:6.1.2",
            Map.of(
                "spring-context", "6.1.2",
                "spring-web",     "6.1.2",
                "spring-tx",      "6.1.2",
                "spring-jdbc",    "6.1.2",
                "spring-aop",     "6.1.2"
            )
        );

        // POM del proyecto: algunas deps con versión explícita, otras sin versión (usan BOM)
        List<Dependency> deps = new ArrayList<>();
        deps.add(new Dependency("org.springframework", "spring-context", null));      // BOM
        deps.add(new Dependency("org.springframework", "spring-web",     null));      // BOM
        deps.add(new Dependency("org.springframework", "spring-tx",      "6.0.0"));   // explícita (override BOM)
        deps.add(new Dependency("org.springframework", "spring-jdbc",    null));      // BOM
        deps.add(new Dependency("org.slf4j",           "slf4j-api",      "2.0.9"));   // explícita (no en BOM)

        Pom myApp = new Pom("my-app", deps, springBom);
        myApp.printEffective();

        System.out.println();
        System.out.println("Nota: spring-tx tiene versión explícita 6.0.0 que tiene");
        System.out.println("precedencia sobre la versión 6.1.2 del BOM.");
    }
}
