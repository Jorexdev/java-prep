import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Ejercicio7 {

    public static void main(String[] args) {

        Predicate<Integer> mayorQueCien = n -> n > 100;
        Predicate<Integer> multiploDeCinco = n -> n % 5 == 0;

        Predicate<Integer> cualquiera = mayorQueCien.or(multiploDeCinco);

        List<Integer> numeros = List.of(3, 5, 50, 99, 100, 101, 200, 7, 15, 83);

        List<Integer> resultado = numeros.stream()
                .filter(cualquiera)
                .collect(Collectors.toList());

        System.out.println("Mayores que 100 O múltiplos de 5: " + resultado);
    }
}
