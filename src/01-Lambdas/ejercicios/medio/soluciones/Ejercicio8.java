import java.util.List;
import java.util.function.BinaryOperator;

public class Ejercicio8 {

    public static void main(String[] args) {

        BinaryOperator<Integer> maximo = (a, b) -> a >= b ? a : b;

        List<Integer> numeros = List.of(34, 7, 91, 56, 23, 88, 12);

        int max = numeros.stream()
                .reduce(Integer.MIN_VALUE, maximo);

        System.out.println("Lista:  " + numeros);
        System.out.println("Máximo: " + max);
    }
}
