import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio6 {
    public static void main(String[] args) {
        List<Integer> lista = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        System.out.println("Original: " + lista);

        Collections.rotate(lista, 2);
        System.out.println("rotate(2): " + lista); // [4, 5, 1, 2, 3]

        Collections.rotate(lista, -1);
        System.out.println("rotate(-1):" + lista); // [5, 1, 2, 3, 4]

        Collections.fill(lista, 0);
        System.out.println("fill(0):   " + lista); // [0, 0, 0, 0, 0]
    }
}
