package base.colecciones.conjunto.implementaciones;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedList;

public class ExpAbstractSet {



















    // Ejemplo de implementación personalizada de AbstractSet
    static class MiSet<E> extends AbstractSet<E> {
        private final LinkedList<E> datos = new LinkedList<>();

        // size(): obligatorio implementar
        @Override
        public int size() {
            return datos.size();
        }

        // iterator(): obligatorio implementar
        @Override
        public Iterator<E> iterator() {
            return datos.iterator();
        }

        // add(E e): opcional implementar; aquí aseguramos no duplicar
        @Override
        public boolean add(E e) {
            if (!datos.contains(e)) {
                datos.add(e);
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) {

        // Crear nuestro propio Set personalizado extendiendo AbstractSet
        MiSet<String> miSet = new MiSet<>();

        // add(E e): añade si no existe en el Set
        miSet.add("Java");
        miSet.add("Spring");
        miSet.add("Java"); // duplicado → no se añade
        System.out.println("MiSet tras add(): " + miSet); // [Java, Spring]

        // contains(Object o): ya implementado en AbstractCollection (se recorre con iterator)
        System.out.println("¿Contiene 'Spring'? " + miSet.contains("Spring")); // true

        // size(): número de elementos
        System.out.println("Tamaño: " + miSet.size()); // 2

        // isEmpty(): retorna true si está vacío
        System.out.println("¿Está vacío? " + miSet.isEmpty()); // false

        // iterator(): recorrer el Set
        System.out.print("Elementos en MiSet: ");
        for (String s : miSet) {
            System.out.print(s + " ");
        }
        System.out.println();

        // clear(): no lo implementamos → lanzará UnsupportedOperationException si lo llamamos
        try {
            miSet.clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("clear() no soportado en nuestra implementación de MiSet");
        }

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - remove(Object o): por defecto lanza UnsupportedOperationException (sobrescribir si lo necesitas).
        // - addAll(Collection c): se apoya en add(); si no lo sobrescribes, usa la implementación de AbstractCollection.
        // - retainAll(Collection c) / removeAll(Collection c): disponibles desde AbstractCollection.
        // - equals(Object o) / hashCode(): ya implementados en AbstractSet de forma correcta para conjuntos.
    }
}
