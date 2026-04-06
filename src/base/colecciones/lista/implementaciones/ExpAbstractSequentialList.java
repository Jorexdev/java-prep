package base.colecciones.listaa.implementaciones;

import java.util.AbstractSequentialList;
import java.util.LinkedList;
import java.util.ListIterator;


public class ExpAbstractSequentialList {

    /*
        En Java, AbstractSequentialList es una clase abstracta que extiende AbstractList.
        Está diseñada para listas que no soportan acceso aleatorio eficiente
        (ej: LinkedList), sino que funcionan mejor con recorridos secuenciales.

        Su implementación obliga a definir:
            - size()
            - listIterator(int index)
     */


    /*                           CONSTRUCTORES                             */

    /*
        - AbstractSequentialList no tiene constructores públicos directos,
          ya que es abstracta.
        - Para usarla, debes extenderla e implementar al menos:
            → size()
            → listIterator(int index)
     */


    /*                             KEY FEATURES                            */

    /*
        - Clase abstracta base para listas secuenciales.
        - Extiende AbstractList.
        - Pensada para implementaciones donde acceder por índice es O(n).
        - Implementa muchos métodos de List usando listIterator().
        - No está sincronizada.
        - Permite nulos y duplicados (depende de la implementación).
        - Mantiene orden de inserción.
     */


    /*                            VENTAJAS                                 */

    /*
        - Ideal para listas enlazadas o estructuras donde el recorrido secuencial es natural.
        - Evita tener que reimplementar lógica de List.
        - Se adapta fácilmente a estructuras personalizadas.
     */


    /*                           DESVENTAJAS                               */

    /*
        - No se puede instanciar directamente (es abstracta).
        - Acceso lento por índice (O(n)).
        - Requiere implementar métodos con iteradores.
     */


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
