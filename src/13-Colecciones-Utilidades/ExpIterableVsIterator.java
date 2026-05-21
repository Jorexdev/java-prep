import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ExpIterableVsIterator {

    public static void main(String[] args) {

        List<String> lista = new ArrayList<>(List.of("Java", "Spring", "Hibernate"));

        // Iterable: for-each llama internamente a lista.iterator() — solo lectura
        for (String elem : lista) {
            System.out.println("for-each: " + elem);
        }

        // Iterator: cursor manual — permite remove() seguro durante la iteración
        // for-each lanzaría ConcurrentModificationException si modificas la colección
        Iterator<String> it = lista.iterator();
        while (it.hasNext()) {
            String valor = it.next();
            if ("Spring".equals(valor)) {
                it.remove(); // elimina el elemento actual sin ConcurrentModificationException
            }
        }
        System.out.println("Tras iterator.remove: " + lista); // [Java, Hibernate]

        // ListIterator: extiende Iterator con navegación bidireccional y add/set
        ListIterator<String> lit = lista.listIterator();

        while (lit.hasNext()) {
            System.out.println("forward idx=" + lit.nextIndex() + ": " + lit.next());
        }

        lit.add("JPA"); // inserta después de la posición actual del cursor
        System.out.println("Tras add: " + lista);

        // previous() retrocede el cursor al elemento anterior
        if (lit.hasPrevious()) {
            String prev = lit.previous();
            lit.set(prev.toUpperCase()); // set() reemplaza el último elemento retornado
        }
        System.out.println("Tras set en reverso: " + lista);
    }
}
