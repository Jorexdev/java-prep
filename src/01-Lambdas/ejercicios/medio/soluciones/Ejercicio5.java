import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Ejercicio5 {

    public static void main(String[] args) {

        Predicate<Integer> esPar = n -> n % 2 == 0;

        // negate() invierte el predicado sin escribir una nueva lambda
        Predicate<Integer> esImpar = esPar.negate();

        List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<Integer> pares = numeros.stream().filter(esPar).collect(Collectors.toList());
        List<Integer> impares = numeros.stream().filter(esImpar).collect(Collectors.toList());

        System.out.println("Pares:   " + pares);
        System.out.println("Impares: " + impares);
    }
}
