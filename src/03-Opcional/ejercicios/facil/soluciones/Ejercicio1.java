import java.util.Optional;

public class Ejercicio1 {

    public static void main(String[] args) {
        // Optional creado con un valor no nulo
        Optional<String> conValor = Optional.of("Hola");

        // Optional creado con ofNullable — acepta null sin lanzar excepción
        Optional<String> deNull = Optional.ofNullable(null);

        // Optional vacío explícito
        Optional<String> vacio = Optional.empty();

        System.out.println("=== conValor ===");
        System.out.println("isPresent: " + conValor.isPresent()); // true
        System.out.println("isEmpty:   " + conValor.isEmpty());   // false

        System.out.println("\n=== deNull (ofNullable con null) ===");
        System.out.println("isPresent: " + deNull.isPresent()); // false
        System.out.println("isEmpty:   " + deNull.isEmpty());   // true

        System.out.println("\n=== vacio (Optional.empty) ===");
        System.out.println("isPresent: " + vacio.isPresent()); // false
        System.out.println("isEmpty:   " + vacio.isEmpty());   // true
    }
}
