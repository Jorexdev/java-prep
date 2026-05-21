import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    // Clase abstracta base para tareas de generación de código
    static abstract class GenerateCodeTask {
        protected final String taskName;
        protected final List<String> classNames;

        GenerateCodeTask(String taskName, List<String> classNames) {
            this.taskName = taskName;
            this.classNames = classNames;
        }

        // Template method: genera el código para una clase
        abstract String generateCode(String className);

        // Ejecuta la tarea y retorna el código generado
        List<String> execute() {
            System.out.printf("> Task :%s%n", taskName);
            List<String> generated = new ArrayList<>();
            for (String name : classNames) {
                String code = generateCode(name);
                generated.add(code);
                System.out.printf("  Generado: %s.java%n", name);
            }
            System.out.printf("  %d clases generadas%n", generated.size());
            return generated;
        }
    }

    // Implementación concreta: genera DTOs simples
    static class DtoGeneratorTask extends GenerateCodeTask {
        DtoGeneratorTask(List<String> classNames) {
            super("generateDtos", classNames);
        }

        @Override
        String generateCode(String className) {
            return "public class " + className + " { }";
        }
    }

    // Implementación concreta: genera interfaces de repositorio
    static class RepositoryGeneratorTask extends GenerateCodeTask {
        RepositoryGeneratorTask(List<String> classNames) {
            super("generateRepositories", classNames);
        }

        @Override
        String generateCode(String className) {
            return "public interface " + className + "Repository { "
                    + "java.util.List<" + className + "> findAll(); "
                    + className + " findById(Long id); "
                    + "}";
        }
    }

    // Grafo de tareas simplificado para demostrar la fase codegen
    static class BuildGraph {
        private final List<GenerateCodeTask> codegenPhase = new ArrayList<>();
        private final List<String> compilationPhase = new ArrayList<>();

        void registerCodegen(GenerateCodeTask task) {
            codegenPhase.add(task);
        }

        void addSourceDir(String dir) { compilationPhase.add(dir); }

        void execute() {
            System.out.println("=== Fase: codegen ===");
            List<String> allGenerated = new ArrayList<>();
            for (GenerateCodeTask task : codegenPhase) {
                allGenerated.addAll(task.execute());
            }
            System.out.println();

            System.out.println("=== Fase: compileJava ===");
            System.out.println("> Task :compileJava");
            System.out.println("  Compilando " + allGenerated.size() + " clases generadas...");
            allGenerated.forEach(code ->
                System.out.println("  Compilando: " + code.substring(0, Math.min(60, code.length())) + "..."));
            System.out.println();

            System.out.println("=== Código generado completo ===");
            allGenerated.forEach(code -> System.out.println("  " + code));
        }
    }

    public static void main(String[] args) {
        BuildGraph build = new BuildGraph();

        // Registrar tareas en la fase codegen
        build.registerCodegen(new DtoGeneratorTask(
            List.of("UserDto", "OrderDto", "ProductDto", "PaymentDto")
        ));

        build.registerCodegen(new RepositoryGeneratorTask(
            List.of("User", "Order", "Product")
        ));

        build.execute();
    }
}
