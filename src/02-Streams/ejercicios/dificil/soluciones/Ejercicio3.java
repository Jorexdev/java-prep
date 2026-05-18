import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Ejercicio3 {
    public static void main(String[] args) {
        List<List<Integer>> matriz = List.of(
            List.of(1, 2, 3),
            List.of(4, 5, 6),
            List.of(7, 8, 9)
        );

        int filas = matriz.size();
        int cols  = matriz.get(0).size();

        List<List<Integer>> transpuesta = IntStream.range(0, cols)
            .mapToObj(col -> IntStream.range(0, filas)
                .mapToObj(fila -> matriz.get(fila).get(col))
                .collect(Collectors.toList()))
            .collect(Collectors.toList());

        System.out.println("Original:");
        matriz.forEach(System.out::println);
        System.out.println("Transpuesta:");
        transpuesta.forEach(System.out::println);
    }
}
