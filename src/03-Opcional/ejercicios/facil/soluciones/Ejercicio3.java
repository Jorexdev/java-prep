import java.util.Optional;

public class Ejercicio3 {

    static Optional<String> buscarNombre(int id) {
        if (id == 1) {
            return Optional.of("Jorge");
        }
        return Optional.empty();
    }

    // El Supplier imprime un mensaje para evidenciar cuándo se ejecuta
    static String generarNombreDefault() {
        System.out.println("  [Supplier] Generando nombre default...");
        return "Anónimo";
    }

    public static void main(String[] args) {
        System.out.println("--- Optional con valor (id=1) ---");
        // El Optional tiene valor → el Supplier NO se invoca
        String nombre1 = buscarNombre(1).orElseGet(() -> generarNombreDefault());
        System.out.println("Resultado: " + nombre1);

        System.out.println("\n--- Optional vacío (id=99) ---");
        // El Optional está vacío → el Supplier SÍ se invoca
        String nombre2 = buscarNombre(99).orElseGet(() -> generarNombreDefault());
        System.out.println("Resultado: " + nombre2);
    }
}
