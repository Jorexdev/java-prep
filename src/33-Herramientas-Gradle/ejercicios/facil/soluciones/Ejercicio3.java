import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    static class SourceFile {
        final String name;
        final String content;

        SourceFile(String name, String content) {
            this.name = name;
            this.content = content;
        }

        // Simular checksum con hashCode
        String checksum() {
            return Integer.toHexString(content.hashCode());
        }
    }

    static class IncrementalCompiler {
        // Checksums del último build
        private final Map<String, String> lastChecksums = new HashMap<>();
        private int totalCompilations = 0;

        void compile(List<SourceFile> sources) {
            List<SourceFile> toCompile = new ArrayList<>();
            List<SourceFile> upToDate  = new ArrayList<>();

            for (SourceFile src : sources) {
                String currentChecksum = src.checksum();
                String lastChecksum    = lastChecksums.get(src.name);

                if (lastChecksum == null || !lastChecksum.equals(currentChecksum)) {
                    toCompile.add(src);
                } else {
                    upToDate.add(src);
                }
            }

            System.out.println("Archivos a compilar: " + toCompile.size() + "/" + sources.size());
            upToDate.forEach(f -> System.out.printf("  [UP-TO-DATE] %s%n", f.name));

            for (SourceFile src : toCompile) {
                System.out.printf("  [COMPILING ] %s (checksum: %s)%n", src.name, src.checksum());
                // Actualizar checksum
                lastChecksums.put(src.name, src.checksum());
                totalCompilations++;
            }

            if (toCompile.isEmpty()) {
                System.out.println("> Task :compileJava UP-TO-DATE");
            } else {
                System.out.printf("> Task :compileJava — compilados %d archivo(s)%n", toCompile.size());
            }
        }

        int totalCompilations() { return totalCompilations; }
    }

    public static void main(String[] args) {
        // 5 archivos fuente originales
        List<SourceFile> v1 = List.of(
            new SourceFile("UserService.java",    "public class UserService { void find() {} }"),
            new SourceFile("OrderService.java",   "public class OrderService { void create() {} }"),
            new SourceFile("PaymentService.java", "public class PaymentService { void pay() {} }"),
            new SourceFile("Controller.java",     "public class Controller { void handle() {} }"),
            new SourceFile("Repository.java",     "public class Repository { void save() {} }")
        );

        IncrementalCompiler compiler = new IncrementalCompiler();

        System.out.println("=== Primera compilación (todo nuevo) ===");
        compiler.compile(v1);
        System.out.println();

        // Segunda compilación: mismos archivos sin cambios
        System.out.println("=== Segunda compilación (sin cambios) ===");
        compiler.compile(v1);
        System.out.println();

        // Tercera compilación: 2 archivos modificados
        List<SourceFile> v2 = List.of(
            new SourceFile("UserService.java",    "public class UserService { void find() {} void delete() {} }"), // CAMBIA
            new SourceFile("OrderService.java",   "public class OrderService { void create() {} }"),  // igual
            new SourceFile("PaymentService.java", "public class PaymentService { void pay() {} void refund() {} }"), // CAMBIA
            new SourceFile("Controller.java",     "public class Controller { void handle() {} }"),    // igual
            new SourceFile("Repository.java",     "public class Repository { void save() {} }")       // igual
        );

        System.out.println("=== Tercera compilación (2 archivos modificados) ===");
        compiler.compile(v2);
        System.out.println();

        System.out.println("Total compilaciones individuales: " + compiler.totalCompilations());
        System.out.println("(5 en el primer build + 2 en el tercero = 7 compilaciones total)");
    }
}
