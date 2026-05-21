import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio4 {

    static class SparseCheckout {
        private final List<String> patterns;

        SparseCheckout(List<String> patterns) {
            this.patterns = patterns;
            System.out.println("Sparse checkout configurado con patterns: " + patterns);
        }

        // Comprueba si la ruta coincide con alguno de los patterns
        boolean matches(String path) {
            return patterns.stream().anyMatch(pattern -> matchGlob(pattern, path));
        }

        // Glob simple: soporta *.ext y src/** y prefijos de directorio
        static boolean matchGlob(String pattern, String path) {
            // Patrón *.ext — coincide con archivos en cualquier nivel con esa extensión
            if (pattern.startsWith("*.")) {
                String ext = pattern.substring(1); // ".java"
                String filename = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
                return filename.endsWith(ext);
            }
            // Patrón dir/** — coincide con todo bajo ese directorio
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3); // "src"
                return path.startsWith(prefix + "/");
            }
            // Patrón exacto o prefijo de directorio
            return path.equals(pattern) || path.startsWith(pattern + "/");
        }

        // Materializa los archivos del árbol que coinciden con los patterns
        List<String> checkout(List<String> allFiles) {
            List<String> included = allFiles.stream()
                    .filter(this::matches)
                    .collect(Collectors.toList());
            List<String> excluded = allFiles.stream()
                    .filter(f -> !matches(f))
                    .collect(Collectors.toList());

            System.out.println();
            System.out.println("=== Archivos INCLUIDOS (" + included.size() + ") ===");
            included.forEach(f -> System.out.println("  [+] " + f));

            System.out.println();
            System.out.println("=== Archivos EXCLUIDOS (" + excluded.size() + ") ===");
            excluded.forEach(f -> System.out.println("  [-] " + f));

            return included;
        }
    }

    public static void main(String[] args) {
        // Árbol de 20 archivos con rutas variadas
        List<String> allFiles = List.of(
            "src/main/java/App.java",
            "src/main/java/service/UserService.java",
            "src/main/java/service/OrderService.java",
            "src/main/java/controller/UserController.java",
            "src/main/java/model/User.java",
            "src/main/resources/application.properties",
            "src/main/resources/banner.txt",
            "src/test/java/UserServiceTest.java",
            "src/test/java/OrderServiceTest.java",
            "src/test/resources/test.properties",
            "docs/README.md",
            "docs/API.md",
            "docs/architecture.png",
            "scripts/deploy.sh",
            "scripts/build.sh",
            ".github/workflows/ci.yml",
            ".github/CODEOWNERS",
            "pom.xml",
            "Makefile",
            "LICENSE"
        );

        System.out.println("=== Árbol completo (" + allFiles.size() + " archivos) ===");
        allFiles.forEach(f -> System.out.println("  " + f));
        System.out.println();

        // Pattern 1: *.java — todos los archivos Java
        System.out.println("=== SparseCheckout con pattern '*.java' ===");
        SparseCheckout onlyJava = new SparseCheckout(List.of("*.java"));
        onlyJava.checkout(allFiles);

        System.out.println();
        System.out.println("=== SparseCheckout con pattern 'src/**' ===");
        SparseCheckout srcOnly = new SparseCheckout(List.of("src/**"));
        srcOnly.checkout(allFiles);

        System.out.println();
        System.out.println("=== SparseCheckout combinado: '*.java' + 'docs/**' ===");
        SparseCheckout combined = new SparseCheckout(List.of("*.java", "docs/**"));
        List<String> result = combined.checkout(allFiles);

        System.out.println();
        System.out.printf("Resultado: %d de %d archivos materializados%n",
                result.size(), allFiles.size());
    }
}
