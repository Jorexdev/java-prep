import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio1 {
    public static void main(String[] args) {
        List<List<String>> listas = List.of(
            List.of("a", "b", "c"),
            List.of("d", "e"),
            List.of("f", "g", "h", "i")
        );
        List<String> plana = listas.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        System.out.println("Aplanada: " + plana);

        List<String> frases = List.of("hola mundo", "java es genial", "streams son potentes");
        List<String> palabras = frases.stream()
            .flatMap(f -> java.util.Arrays.stream(f.split(" ")))
            .collect(Collectors.toList());
        System.out.println("Palabras: " + palabras);
    }
}
