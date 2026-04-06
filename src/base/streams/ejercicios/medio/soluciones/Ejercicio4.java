package base.streams.ejemplosercicios.medio.soluciones;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Ejercicio4 {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 23, 1,57, 3,12, 23 ,121);

        numbers
                .stream()
                .collect(Collectors.groupingBy(
                        Integer::intValue,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .filter(x -> x.getValue() >= 2)
                .forEach(System.out::println);    }
}
