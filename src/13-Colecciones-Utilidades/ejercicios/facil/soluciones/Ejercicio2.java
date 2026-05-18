import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio2 {
    public static void main(String[] args) {
        List<String> lista = new ArrayList<>(List.of("a", "b", "c", "d", "e"));
        System.out.println("Original: " + lista);

        Collections.shuffle(lista);
        System.out.println("Shuffle:  " + lista);

        Collections.swap(lista, 0, lista.size() - 1);
        System.out.println("Swap 0↔4: " + lista);
    }
}
