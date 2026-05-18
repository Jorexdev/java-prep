import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Ejercicio6 {

    public static void main(String[] args) {

        Function<String, Integer> aLongitud = String::length;

        List<String> palabras = List.of("lambda", "stream", "optional", "map", "filter");

        List<Integer> longitudes = palabras.stream()
                .map(aLongitud)
                .collect(Collectors.toList());

        System.out.println("Palabras:   " + palabras);
        System.out.println("Longitudes: " + longitudes);
    }
}
