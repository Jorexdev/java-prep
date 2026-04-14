package base.streams.ejercicios.facil.soluciones;

import java.util.List;

public class Ejercicio9 {

    public static void main(String[] args) {

        // Ejercicio: verificar si TODOS los strings tienen longitud mayor que 3
        List<String> words = List.of("A", "Ay", "Ayu", "Ayud", "Ayuda");

        boolean todos = words
                .stream()
                .allMatch(x -> x.length() > 3); // true solo si todos los elementos cumplen la condición

        System.out.println("Todos tienen longitud > 3: " + todos); // false — "A", "Ay", "Ayu" no la cumplen
    }
}
