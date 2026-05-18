import java.util.List;
import java.util.function.Predicate;

public class Ejercicio7 {

    public static void main(String[] args) {

        Predicate<Integer> esPar = n -> n % 2 == 0;

        List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        numeros.stream()
                .filter(esPar)
                .forEach(System.out::println);
    }
}
