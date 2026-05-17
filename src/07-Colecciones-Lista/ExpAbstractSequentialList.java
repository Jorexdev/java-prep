import java.util.AbstractSequentialList;
import java.util.LinkedList;
import java.util.ListIterator;

public class ExpAbstractSequentialList {



















    // Ejemplo mínimo de implementación de AbstractSequentialList
    static class MiSecuencialList<E> extends AbstractSequentialList<E> {
        private final LinkedList<E> datos;

        public MiSecuencialList(LinkedList<E> lista) {
            this.datos = lista;
        }

        // size(): obligatorio implementar
        @Override
        public int size() {
            return datos.size();
        }

        // listIterator(int index): obligatorio implementar
        @Override
        public ListIterator<E> listIterator(int index) {
            return datos.listIterator(index);
        }
    }

    public static void main(String[] args) {

        // Crear una lista secuencial personalizada con respaldo de LinkedList
        LinkedList<String> respaldo = new LinkedList<>();
        respaldo.add("programando");
        respaldo.add("con");
        respaldo.add("Jorge");

        MiSecuencialList<String> miLista = new MiSecuencialList<>(respaldo);

        // size(): devuelve el tamaño de la lista
        System.out.println("Tamaño: " + miLista.size()); // 3

        // listIterator(int index): obtiene un iterador comenzando en un índice dado
        ListIterator<String> it = miLista.listIterator(1);
        System.out.println("Elemento en índice 1 usando listIterator: " + it.next()); // con

        // get(int index): AbstractSequentialList ya lo implementa usando listIterator()
        System.out.println("Elemento en índice 2: " + miLista.get(2)); // Jorge

        // contains(Object o): ya implementado en AbstractList → busca recorriendo la lista
        System.out.println("¿Contiene 'Jorge'? " + miLista.contains("Jorge")); // true

        // isEmpty(): retorna true si no hay elementos
        System.out.println("¿Está vacía? " + miLista.isEmpty()); // false

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - add(int index, E element): por defecto implementado usando listIterator().
        // - remove(int index): también implementado usando listIterator().
        // - set(int index, E element): lo mismo, usa listIterator().
        // - subList(int from, int to): devuelve una vista de la lista.
        // - iterator(): implementado para recorrido secuencial.
    }
}
