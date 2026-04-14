package base.colecciones.lista.implementaciones;

import java.util.AbstractList;
import java.util.List;

public class ExpAbstractList {



















    // Ejemplo de una implementación personalizada de AbstractList
    static class MiLista<E> extends AbstractList<E> {
        private final E[] datos;

        // Constructor: recibe un array como respaldo
        public MiLista(E[] array) {
            this.datos = array;
        }

        // get(int index): obligatorio implementar
        @Override
        public E get(int index) {
            return datos[index];
        }

        // size(): obligatorio implementar
        @Override
        public int size() {
            return datos.length;
        }
    }

    public static void main(String[] args) {

        // Creamos una lista personalizada a partir de un array
        String[] nombres = {"programando", "con", "Jorge"};
        List<String> miLista = new MiLista<>(nombres);

        // get(int index): obtiene el elemento en la posición dada
        System.out.println("Elemento en índice 1: " + miLista.get(1)); // con

        // size(): devuelve el tamaño de la lista
        System.out.println("Tamaño de la lista: " + miLista.size()); // 3

        // add(E e): lanzará UnsupportedOperationException
        // porque no está implementado en nuestra clase
        try {
            miLista.add("nuevo");
        } catch (UnsupportedOperationException e) {
            System.out.println("add() no soportado por defecto en AbstractList");
        }

        // contains(Object o): ya implementado en AbstractList
        System.out.println("¿Contiene 'Jorge'? " + miLista.contains("Jorge")); // true

        // indexOf(Object o): busca el índice de un elemento
        System.out.println("Índice de 'con': " + miLista.indexOf("con")); // 1

        // isEmpty(): devuelve true si está vacía
        System.out.println("¿Está vacía? " + miLista.isEmpty()); // false

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - set(int index, E element): por defecto lanza UnsupportedOperationException.
        // - add(int index, E element): también no soportado salvo que lo sobrescribas.
        // - remove(int index): igual, debes implementarlo si lo quieres usar.
        // - subList(int from, int to): ya implementado (devuelve una vista).
        // - iterator(): ya implementado, permite recorrer la lista.
    }
}
