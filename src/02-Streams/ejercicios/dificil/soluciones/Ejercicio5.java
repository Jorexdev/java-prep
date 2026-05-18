import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio5 {
    record Resultado(List<Integer> pares, Long sumaImpares) {}

    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Resultado r = nums.stream()
            .collect(Collectors.teeing(
                Collectors.filtering(n -> n % 2 == 0, Collectors.toList()),
                Collectors.filtering(n -> n % 2 != 0, Collectors.summingLong(Integer::longValue)),
                Resultado::new
            ));

        System.out.println("Pares:         " + r.pares());
        System.out.println("Suma impares:  " + r.sumaImpares());
    }
}
