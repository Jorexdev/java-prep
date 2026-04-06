package base.streams.ejercicios.facil.soluciones;

import java.util.List;

public class Ejercicio3 {
    public static void main(String[] args) {
        List<Integer> numberz = List.of(2131,123,42,1,9);

        numberz
                .stream()
                .reduce(Integer::sum)
                .ifPresent(System.out::println);
    }
}
