import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Ejercicio5 {

    // Devuelve el primer Optional con valor de la lista
    static <T> Optional<T> firstNonEmpty(List<Optional<T>> opciones) {
        return opciones.stream()
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(o -> o); // desenvuelve Optional<Optional<T>> → Optional<T>
    }

    public static void main(String[] args) {
        // La tercera opción tiene valor, las dos primeras están vacías
        List<Optional<String>> opciones = Arrays.asList(
                Optional.empty(),
                Optional.empty(),
                Optional.of("¡Este!"),
                Optional.of("Otro")
        );

        Optional<String> resultado = firstNonEmpty(opciones);
        System.out.println("Primer no vacío: " + resultado); // Optional[¡Este!]
        System.out.println("Valor:           " + resultado.orElse("ninguno"));

        // Todos vacíos
        List<Optional<String>> todosVacios = Arrays.asList(
                Optional.empty(),
                Optional.empty()
        );
        Optional<String> vacio = firstNonEmpty(todosVacios);
        System.out.println("\nTodos vacíos: " + vacio); // Optional.empty

        // Lista vacía
        Optional<String> listaVacia = firstNonEmpty(List.of());
        System.out.println("Lista vacía:  " + listaVacia); // Optional.empty
    }
}
