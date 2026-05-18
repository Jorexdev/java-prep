import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio7 {
    public static void main(String[] args) {
        List<Integer> conDuplicados = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        List<Integer> limpio = conDuplicados.stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Original:  " + conDuplicados);
        System.out.println("Distinct + sorted: " + limpio);
    }
}
