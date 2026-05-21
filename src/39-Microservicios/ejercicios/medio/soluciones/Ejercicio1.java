import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    static class SagaStep {
        final String nombre;
        final Runnable action;
        final Runnable compensation;

        SagaStep(String nombre, Runnable action, Runnable compensation) {
            this.nombre = nombre;
            this.action = action;
            this.compensation = compensation;
        }
    }

    static class SagaOrchestrator {
        private final List<SagaStep> steps;

        SagaOrchestrator(List<SagaStep> steps) {
            this.steps = steps;
        }

        void execute() {
            List<SagaStep> completed = new ArrayList<>();
            for (SagaStep step : steps) {
                try {
                    System.out.println("  Ejecutando: " + step.nombre);
                    step.action.run();
                    completed.add(step);
                } catch (Exception e) {
                    System.out.println("  FALLO en: " + step.nombre + " → " + e.getMessage());
                    System.out.println("  Compensando en orden inverso...");
                    for (int i = completed.size() - 1; i >= 0; i--) {
                        SagaStep s = completed.get(i);
                        System.out.println("  Compensando: " + s.nombre);
                        s.compensation.run();
                    }
                    return;
                }
            }
            System.out.println("  Saga completada con éxito.");
        }
    }

    static List<SagaStep> buildSteps(boolean failPago) {
        List<SagaStep> steps = new ArrayList<>();
        steps.add(new SagaStep("CrearPedido",
            () -> System.out.println("    [+] Pedido #1001 creado"),
            () -> System.out.println("    [-] Pedido #1001 cancelado")));
        steps.add(new SagaStep("ReservarStock",
            () -> System.out.println("    [+] Stock reservado: 2x ProductoA"),
            () -> System.out.println("    [-] Stock liberado: 2x ProductoA")));
        steps.add(new SagaStep("CobrarPago", () -> {
            if (failPago) throw new RuntimeException("Fondos insuficientes");
            System.out.println("    [+] Pago cobrado: 59.99€");
        }, () -> System.out.println("    [-] Pago reembolsado")));
        steps.add(new SagaStep("CrearEnvio",
            () -> System.out.println("    [+] Envío programado"),
            () -> System.out.println("    [-] Envío cancelado")));
        return steps;
    }

    public static void main(String[] args) {
        System.out.println("=== Saga exitosa ===");
        new SagaOrchestrator(buildSteps(false)).execute();

        System.out.println("\n=== Saga con fallo en CobrarPago ===");
        new SagaOrchestrator(buildSteps(true)).execute();
    }
}
