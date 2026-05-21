import java.util.concurrent.StructuredTaskScope;

public class Ejercicio4 {
    public static void main(String[] args) {
        System.out.println("=== StructuredTaskScope.ShutdownOnFailure ===\n");

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            StructuredTaskScope.Subtask<String> subtarea1 = scope.fork(() -> {
                Thread.sleep(50);
                System.out.println("  subtarea-1: completada");
                return "resultado-1";
            });

            StructuredTaskScope.Subtask<String> subtarea2 = scope.fork(() -> {
                Thread.sleep(20); // falla primero
                System.out.println("  subtarea-2: lanzando excepcion...");
                throw new RuntimeException("tarea-2 fallo");
            });

            StructuredTaskScope.Subtask<String> subtarea3 = scope.fork(() -> {
                Thread.sleep(100); // la mas lenta, deberia cancelarse
                System.out.println("  subtarea-3: completada");
                return "resultado-3";
            });

            scope.join();           // esperar a que alguna falle o todas terminen

            System.out.println();
            System.out.println("Estado tras join:");
            System.out.println("  subtarea-1 estado: " + subtarea1.state());
            System.out.println("  subtarea-2 estado: " + subtarea2.state());
            System.out.println("  subtarea-3 estado: " + subtarea3.state());
            System.out.println();

            scope.throwIfFailed();  // lanza la excepcion de subtarea-2

        } catch (Exception e) {
            System.out.println("=== Excepcion capturada en main ===");
            System.out.println("Tipo: " + e.getClass().getSimpleName());
            System.out.println("Mensaje: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Explicacion ===");
        System.out.println("ShutdownOnFailure: cuando cualquier subtarea falla,");
        System.out.println("se cancela el scope y las subtareas restantes son interrumpidas.");
        System.out.println("throwIfFailed() re-lanza la primera excepcion capturada.");
        System.out.println("Garantiza que ninguna subtarea queda 'flotando' tras un fallo.");
    }
}
