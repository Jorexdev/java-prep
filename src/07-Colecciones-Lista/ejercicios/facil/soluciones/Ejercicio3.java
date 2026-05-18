import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Ejercicio3 {
    public static void main(String[] args) {
        List<Integer> numeros = new ArrayList<>(List.of(1, 2, 3, 4, 5));

        // (a) for-each
        System.out.print("for-each:          ");
        for (int n : numeros) {
            System.out.print(n + " ");
        }
        System.out.println();

        // (b) Iterator
        System.out.print("Iterator:          ");
        Iterator<Integer> it = numeros.iterator();
        while (it.hasNext()) {
            System.out.print(it.next() + " ");
        }
        System.out.println();

        // (c) ListIterator hacia atrás — se posiciona al final primero
        System.out.print("ListIterator (←):  ");
        ListIterator<Integer> lit = numeros.listIterator(numeros.size());
        while (lit.hasPrevious()) {
            System.out.print(lit.previous() + " ");
        }
        System.out.println();
    }
}
