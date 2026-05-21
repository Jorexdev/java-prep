import java.util.LinkedHashMap;
import java.util.Map;

public class Ejercicio6 {

    static class Pom {
        final String groupId;
        final String artifactId;
        final String version;
        final Pom parent;
        final Map<String, String> ownProperties; // propiedades declaradas en este POM

        Pom(String groupId, String artifactId, String version, Pom parent, Map<String, String> properties) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.parent = parent;
            this.ownProperties = properties;
        }

        // Devuelve las propiedades efectivas: herencia del padre + sobreescrituras propias
        Map<String, String> effectiveProperties() {
            Map<String, String> effective = new LinkedHashMap<>();
            if (parent != null) {
                effective.putAll(parent.effectiveProperties()); // heredar del padre
            }
            effective.putAll(ownProperties); // sobreescribir con las propias
            return effective;
        }

        String effectiveGroupId()  { return groupId  != null ? groupId  : (parent != null ? parent.effectiveGroupId()  : null); }
        String effectiveVersion()  { return version  != null ? version  : (parent != null ? parent.effectiveVersion()  : null); }

        void print() {
            System.out.println("=== " + artifactId + " ===");
            System.out.println("  groupId:  " + effectiveGroupId());
            System.out.println("  version:  " + effectiveVersion());
            System.out.println("  parent:   " + (parent != null ? parent.artifactId : "ninguno"));
            System.out.println("  Propiedades efectivas:");
            effectiveProperties().forEach((k, v) -> System.out.printf("    %-25s = %s%n", k, v));
        }
    }

    public static void main(String[] args) {
        // POM raíz (abuelo)
        Pom grandParent = new Pom(
            "com.example", "company-parent", "1.0.0", null,
            Map.of(
                "java.version",      "21",
                "spring.version",    "6.1.2",
                "encoding",          "UTF-8",
                "project.build.dir", "target",
                "maven.test.skip",   "false"
            )
        );

        // POM padre hereda del abuelo, sobreescribe spring.version
        Pom parent = new Pom(
            null, "app-parent", null, grandParent,
            Map.of(
                "spring.version", "6.2.0",   // sobreescribe
                "log.level",      "INFO"      // nueva propiedad
            )
        );

        // POM hijo hereda del padre, sobreescribe encoding y maven.test.skip
        Pom child = new Pom(
            null, "my-service", "2.0.0", parent,
            Map.of(
                "encoding",        "ISO-8859-1",  // sobreescribe
                "maven.test.skip", "true"          // sobreescribe
            )
        );

        grandParent.print();
        System.out.println();
        parent.print();
        System.out.println();
        child.print();

        System.out.println();
        System.out.println("=== Verificación de herencia ===");
        Map<String, String> eff = child.effectiveProperties();
        System.out.println("java.version     = " + eff.get("java.version")     + " (heredado del abuelo)");
        System.out.println("spring.version   = " + eff.get("spring.version")   + " (sobreescrito por padre)");
        System.out.println("encoding         = " + eff.get("encoding")          + " (sobreescrito por hijo)");
        System.out.println("log.level        = " + eff.get("log.level")         + " (del padre)");
        System.out.println("maven.test.skip  = " + eff.get("maven.test.skip")   + " (sobreescrito por hijo)");
    }
}
