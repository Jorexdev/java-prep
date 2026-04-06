package base.streams.ejemplosercicios.medio.soluciones;

import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio6 {
    public static void main(String[] args) {
        List<Double> numeros = List.of(12.5, 7.3, 18.9, 4.0, 15.6, 9.1, 21.4);

        String string = numeros
                .stream()
                .collect(Collectors.summarizingDouble(Double::doubleValue))
                .toString();

        System.out.printf(string);
    }
}
