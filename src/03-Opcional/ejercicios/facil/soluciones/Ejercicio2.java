import java.util.Optional;

public class Ejercicio2 {

    // Retorna el nombre asociado al id, o vacío si no se conoce
    static Optional<String> buscarNombre(int id) {
        if (id == 1) {
            return Optional.of("Jorge");
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        // Id conocido — orElse no aplica su argumento pero el valor viene del Optional
        String nombre1 = buscarNombre(1).orElse("Desconocido");
        System.out.println("Id 1:  " + nombre1); // Jorge

        // Id desconocido — orElse devuelve el valor por defecto
        String nombre2 = buscarNombre(99).orElse("Desconocido");
        System.out.println("Id 99: " + nombre2); // Desconocido
    }
}
