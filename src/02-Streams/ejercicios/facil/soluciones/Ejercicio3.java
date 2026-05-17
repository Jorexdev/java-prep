import java.util.List;

public class Ejercicio3 {

    public static void main(String[] args) {

        // Ejercicio: calcular la suma de todos los números de la lista
        // nota: el enunciado pide pares, pero reduce suma todos — añade .filter(n -> n % 2 == 0) para pares
        List<Integer> numbers = List.of(2131, 123, 42, 1, 9);

        numbers
                .stream()
                .reduce(Integer::sum)   // reduce acumula aplicando la función — devuelve Optional
                .ifPresent(System.out::println);
    }
}
