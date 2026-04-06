package base.streams.ejercicios.facil.soluciones;

import java.util.List;

public class Ejercicio2 {
    public static void main(String[] args) {
        List<String> words = List.of(
            "hey", "teXTo", "LOREM IPSUM", "prueba123"
        );

        List<String> upperCaseWords = words
                .stream()
                .map(String::toUpperCase)
                .toList();

        upperCaseWords.forEach(System.out::println);
    }
}
