import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Ejercicio5 {

    // --- Representación de una clase con su capa e imports ---

    record ArchClass(String nombre, String capa, List<String> imports) {}

    // --- Regla de arquitectura ---

    static class ArchRule {
        final String descripcion;
        final Predicate<ArchClass> afecta;        // clases a las que aplica la regla
        final Predicate<ArchClass> esCumplida;    // condición que debe satisfacer

        ArchRule(String descripcion, Predicate<ArchClass> afecta, Predicate<ArchClass> esCumplida) {
            this.descripcion = descripcion;
            this.afecta      = afecta;
            this.esCumplida  = esCumplida;
        }
    }

    // --- Resultado de una violación ---

    record Violation(String clase, String regla, String detalle) {}

    // --- Validator de arquitectura ---

    static class ArchitectureValidator {
        private final List<ArchRule> rules = new ArrayList<>();

        void addRule(ArchRule rule) { rules.add(rule); }

        List<Violation> validate(List<ArchClass> classes) {
            List<Violation> violations = new ArrayList<>();
            for (ArchClass cls : classes) {
                for (ArchRule rule : rules) {
                    if (rule.afecta.test(cls) && !rule.esCumplida.test(cls)) {
                        String detalle = "Clase '" + cls.nombre() + "' [" + cls.capa()
                            + "] viola: " + rule.descripcion
                            + " (imports: " + cls.imports() + ")";
                        violations.add(new Violation(cls.nombre(), rule.descripcion, detalle));
                    }
                }
            }
            return violations;
        }
    }

    // --- Helpers ---

    static boolean importaDe(ArchClass cls, String capa) {
        return cls.imports().stream().anyMatch(imp -> imp.contains("." + capa + "."));
    }

    public static void main(String[] args) {

        // ---- Definición de las reglas ----
        ArchitectureValidator validator = new ArchitectureValidator();

        // R1: service NO debe importar de repository directamente
        validator.addRule(new ArchRule(
            "Las clases @Service no deben importar clases @Repository directamente",
            cls -> "service".equals(cls.capa()),
            cls -> !importaDe(cls, "repository")
        ));

        // R2: controller solo puede importar de service
        validator.addRule(new ArchRule(
            "Las clases @Controller solo pueden importar de service, no de repository",
            cls -> "controller".equals(cls.capa()),
            cls -> !importaDe(cls, "repository")
        ));

        // R3: repository no puede importar de controller ni de service
        validator.addRule(new ArchRule(
            "Las clases @Repository no deben importar de controller ni de service",
            cls -> "repository".equals(cls.capa()),
            cls -> !importaDe(cls, "controller") && !importaDe(cls, "service")
        ));

        // ---- Definición de clases (con violaciones intencionadas) ----
        List<ArchClass> classes = List.of(
            // Correctas
            new ArchClass("PedidoController",   "controller",
                List.of("com.app.service.PedidoService", "com.app.model.Pedido")),
            new ArchClass("PedidoService",       "service",
                List.of("com.app.model.Pedido", "com.app.event.PedidoCreado")),
            new ArchClass("PedidoRepository",    "repository",
                List.of("com.app.model.Pedido", "java.util.List")),
            new ArchClass("ClienteService",      "service",
                List.of("com.app.model.Cliente", "com.app.event.ClienteActualizado")),

            // VIOLACION 1: controller importa repository directamente
            new ArchClass("StockController",     "controller",
                List.of("com.app.service.StockService",
                        "com.app.repository.StockRepository")),  // viola R2

            // VIOLACION 2: service importa repository directamente
            new ArchClass("FacturaService",      "service",
                List.of("com.app.repository.FacturaRepository",  // viola R1
                        "com.app.model.Factura")),

            // VIOLACION 3: repository importa de service
            new ArchClass("AuditoriaRepository", "repository",
                List.of("com.app.model.Evento",
                        "com.app.service.AuditoriaService")),    // viola R3

            // Correcta
            new ArchClass("ProductoRepository",  "repository",
                List.of("com.app.model.Producto", "java.util.Map"))
        );

        // ---- Ejecutar validación ----
        List<Violation> violations = validator.validate(classes);

        System.out.println("=== Architecture Test ===");
        System.out.println("Clases analizadas: " + classes.size());
        System.out.println("Reglas evaluadas:  " + 3 + "\n");

        if (violations.isEmpty()) {
            System.out.println("PASS  Sin violaciones — arquitectura limpia");
        } else {
            System.out.println("Se encontraron " + violations.size() + " violaciones:\n");
            for (Violation v : violations) {
                System.out.println("FAIL  " + v.detalle());
            }
        }

        // ---- Resumen por capa ----
        System.out.println("\n--- Clases sin violaciones ---");
        Set<String> clasesConViolacion = violations.stream()
            .map(Violation::clase)
            .collect(Collectors.toSet());
        classes.stream()
            .filter(c -> !clasesConViolacion.contains(c.nombre()))
            .forEach(c -> System.out.println("  OK  " + c.nombre() + " [" + c.capa() + "]"));
    }
}
