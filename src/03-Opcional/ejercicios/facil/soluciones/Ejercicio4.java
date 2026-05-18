import java.util.Optional;

public class Ejercicio4 {

    public static void main(String[] args) {
        // Optional con valor — map transforma el contenido
        Optional<String> nombre = Optional.of("Jorge");
        Optional<Integer> longitud = nombre.map(String::length);

        System.out.println("Nombre:   " + nombre);   // Optional[Jorge]
        System.out.println("Longitud: " + longitud); // Optional[5]

        // Optional vacío — map propaga el vacío sin error
        Optional<String> vacio = Optional.empty();
        Optional<Integer> longitudVacio = vacio.map(String::length);

        System.out.println("\nVacío original: " + vacio);       // Optional.empty
        System.out.println("Longitud vacío: " + longitudVacio); // Optional.empty
    }
}
