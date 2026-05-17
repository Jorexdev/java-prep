import java.util.List;

public class Ejercicio2 {

    public static void main(String[] args) {

        // Ejercicio: transformar todas las palabras a mayúsculas
        List<String> words = List.of("hey", "teXTo", "LOREM IPSUM", "prueba123");

        List<String> upperCaseWords = words
                .stream()
                .map(String::toUpperCase) // transforma cada elemento sin modificar la lista original
                .toList();                // recoge en una nueva lista inmutable

        upperCaseWords.forEach(System.out::println);
    }
}
