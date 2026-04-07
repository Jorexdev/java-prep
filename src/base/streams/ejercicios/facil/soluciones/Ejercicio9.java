package base.streams.ejercicios.facil.soluciones;

import java.util.List;
import java.util.function.Predicate;

public class Ejercicio9 {
    public static void main(String[] args) {
        List<String> wordList = List.of("A", "Ay", "Ayu", "Ayud", "Ayuda");

        Predicate<String> predicate = x-> x.length() >= 3;

        wordList
                .stream()
                .filter(predicate)
                .forEach(System.out::println);
    }
}
