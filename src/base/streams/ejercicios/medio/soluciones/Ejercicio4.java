package base.streams.ejercicios.medio.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio4 {

    public static void main(String[] args) {

        // Ejercicio: detectar números que aparecen más de una vez en la lista
        List<Integer> numbers = List.of(1, 23, 1, 57, 3, 12, 23, 121);

        numbers
                .stream()
                .collect(Collectors.groupingBy(
                        Integer::intValue,   // clave: el número
                        Collectors.counting() // valor: cuántas veces aparece
                ))
                .entrySet()
                .stream()
                .filter(x -> x.getValue() >= 2) // solo los que aparecen más de una vez
                .forEach(System.out::println);
    }
}
