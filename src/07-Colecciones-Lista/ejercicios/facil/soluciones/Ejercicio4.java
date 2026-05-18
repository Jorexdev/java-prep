import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio4 {
    public static void main(String[] args) {
        List<Integer> numeros = new ArrayList<>(List.of(7, 2, 9, 1, 5, 3, 8));
        System.out.println("Original:          " + numeros);

        Collections.sort(numeros);
        System.out.println("sort() ascendente: " + numeros);

        Collections.reverse(numeros);
        System.out.println("reverse():         " + numeros);
    }
}
