import java.util.Collections;
import java.util.List;

public class Ejercicio3 {
    public static void main(String[] args) {
        List<String> lista = Collections.nCopies(5, "java");
        System.out.println("nCopies: " + lista);

        List<Integer> a = List.of(1, 2, 3, 4, 5);
        List<Integer> b = List.of(4, 5, 6, 7);
        List<Integer> c = List.of(8, 9, 10);

        System.out.println("frequency(a, 3):  " + Collections.frequency(a, 3));
        System.out.println("disjoint(a, b):   " + Collections.disjoint(a, b)); // false
        System.out.println("disjoint(a, c):   " + Collections.disjoint(a, c)); // true
    }
}
