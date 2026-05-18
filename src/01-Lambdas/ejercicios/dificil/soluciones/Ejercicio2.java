import java.util.List;
import java.util.function.Function;

public class Ejercicio2 {

    public static void main(String[] args) {

        // Currying: Function<A, Function<B, C>>
        // En lugar de sumar(int a, int b), tenemos sumar(a) que retorna una función que espera b
        Function<Integer, Function<Integer, Integer>> sumar = a -> b -> a + b;

        // Aplicación parcial: fijar el primer argumento
        Function<Integer, Integer> sumar5 = sumar.apply(5);
        Function<Integer, Integer> sumar10 = sumar.apply(10);

        System.out.println("sumar5 aplicada a 3: " + sumar5.apply(3));   // 8
        System.out.println("sumar5 aplicada a 7: " + sumar5.apply(7));   // 12
        System.out.println("sumar10 aplicada a 4: " + sumar10.apply(4)); // 14

        // Aplicar sumar5 a una lista de enteros
        List<Integer> numeros = List.of(1, 2, 3, 4, 5);
        System.out.println("\nLista original: " + numeros);
        System.out.print("Con sumar5: ");
        numeros.stream()
               .map(sumar5)
               .forEach(n -> System.out.print(n + " "));
        System.out.println();
    }
}
