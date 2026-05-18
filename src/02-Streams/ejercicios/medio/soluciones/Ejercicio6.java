import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio6 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<Integer> resultado = nums.stream()
            .peek(n -> System.out.println("  [entrada] " + n))
            .filter(n -> n % 2 == 0)
            .peek(n -> System.out.println("  [par] " + n))
            .map(n -> n * 2)
            .peek(n -> System.out.println("  [*2] " + n))
            .filter(n -> n > 10)
            .peek(n -> System.out.println("  [>10] " + n))
            .collect(Collectors.toList());

        System.out.println("Resultado: " + resultado);
    }
}
