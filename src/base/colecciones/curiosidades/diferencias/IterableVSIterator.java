package base.colecciones.curiosidades.diferencias;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/*
    ITERABLE VS ITERATOR

    Iterable
    - Interfaz que cualquier colección puede implementar para ser recorrible.
    - Solo tiene un método: iterator(), que devuelve un Iterator.
    - Gracias a Iterable funciona el for-each en Java.
    - No tiene estado: solo sabe cómo crear un cursor.

    Iterator
    - Objeto con estado que representa un cursor sobre la colección.
    - Métodos: hasNext(), next(), remove().
    - Permite eliminar elementos durante la iteración de forma segura
      (evita ConcurrentModificationException).

    ListIterator
    - Extiende Iterator para listas.
    - Navegación bidireccional (hacia adelante y hacia atrás).
    - Permite add(), set() y remove() durante la iteración.

    ¿Cuándo usar Iterator en lugar de for-each?
    - Cuando necesitas eliminar elementos mientras recorres la colección.
    - Cuando necesitas recorrer en orden inverso (ListIterator).

    Preguntas típicas de entrevista:
    - ¿Qué es ConcurrentModificationException y cómo se evita?
    - ¿Por qué for-each no permite modificar la colección?
    - ¿Qué diferencia hay entre Iterator y ListIterator?
    - ¿Qué devuelve iterator.remove() si no has llamado next() antes? (IllegalStateException)
*/
public class IterableVSIterator {

    public static void main(String[] args) {
        List<String> lista = new ArrayList<>(List.of("Java", "Spring", "Hibernate"));

        // Iterable: el for-each llama internamente a lista.iterator()
        for (String elem : lista) {
            System.out.println("for-each: " + elem);
        }

        // Iterator: cursor manual con control total
        // remove() es la forma segura de eliminar durante la iteración
        Iterator<String> it = lista.iterator();
        while (it.hasNext()) {
            String valor = it.next();
            System.out.println("Iterator: " + valor);

            if ("Spring".equals(valor)) {
                it.remove(); // seguro: no lanza ConcurrentModificationException
            }
        }
        System.out.println("Lista tras remove: " + lista); // [Java, Hibernate]

        // ListIterator: bidireccional y con mutación durante la iteración
        ListIterator<String> lit = lista.listIterator();

        while (lit.hasNext()) {
            int idx = lit.nextIndex();
            String val = lit.next();
            System.out.println("ListIterator forward idx " + idx + ": " + val);
        }

        // add() inserta después de la posición actual del cursor
        lit.add("JPA");
        System.out.println("Tras add con ListIterator: " + lista);

        // previous() retrocede el cursor y devuelve el elemento
        if (lit.hasPrevious()) {
            String previo = lit.previous();
            lit.set(previo.toUpperCase()); // set() reemplaza el último retornado
        }
        System.out.println("Tras set en ListIterator: " + lista);
    }
}
