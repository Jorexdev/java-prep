package base.colecciones.listaa.implementaciones;

import java.util.LinkedList;

public class ExpLinkedList {

    /*
        En Java, los LinkedList implementan una lista doblemente enlazada donde los
        elementos no se almacenan en memoria contigua.
        Cada nodo contiene:
            - Los datos
            - Una referencia al nodo anterior
            - Una referencia al nodo siguiente
     */


    /*                           CONSTRUCTORES                             */

    /*
        - LinkedList()
            → Crea una lista enlazada vacía.

        - LinkedList(Collection<? extends E> c)
            → Crea una lista enlazada que contiene todos los elementos de la colección dada,
              en el mismo orden que los retorna el iterador de esa colección.
     */


    /*                             KEY FEATURES                            */

    /*
        - LinkedList es una lista doblemente enlazada (no basada en array).
        - No ofrece acceso indexado rápido (buscar por índice es O(n)).
        - Implementa las interfaces List, Deque y Queue → puede usarse como lista, cola o pila.
        - Soporte de genéricos; asegura la seguridad del tipo en compilación (type-safety).
        - No está sincronizado; para seguridad en hilos se usa Collections.synchronizedList().
        - Permite nulos y duplicados.
        - Mantiene orden de inserción.
     */


    /*                            VENTAJAS                                 */

    /*
        - Inserciones y eliminaciones rápidas en extremos (O(1)).
        - Útil para implementar estructuras de datos como colas y pilas.
        - Flexibilidad al poder usarse como List y como Deque.
        - Soporta nulos y duplicados.
     */


    /*                           DESVENTAJAS                               */

    /*
        - Acceso lento por índice (O(n)).
        - Mayor consumo de memoria que ArrayList (referencias prev/next en cada nodo).
        - Iterar suele ser más costoso que en un ArrayList.
     */


    public static void main(String[] args) {

        // Crear un LinkedList vacío
        LinkedList<String> linkedList = new LinkedList<>();

        // add(E e): añade elementos al final de la lista
        linkedList.add("programando");
        linkedList.add("con");
        linkedList.add("Jorge");
        System.out.println(linkedList); // [programando, con, Jorge]

        // addFirst(E e): añade un elemento al inicio
        linkedList.addFirst("Inicio");

        // addLast(E e): añade un elemento al final
        linkedList.addLast("Final");
        System.out.println(linkedList); // [Inicio, programando, con, Jorge, Final]

        // getFirst(): obtiene el primer elemento sin eliminarlo
        System.out.println("Primero: " + linkedList.getFirst()); // Inicio

        // getLast(): obtiene el último elemento sin eliminarlo
        System.out.println("Último: " + linkedList.getLast());   // Final

        // removeFirst(): elimina el primer elemento
        linkedList.removeFirst();

        // removeLast(): elimina el último elemento
        linkedList.removeLast();
        System.out.println("Tras eliminar extremos: " + linkedList); // [programando, con, Jorge]

        // peek(): devuelve el primer elemento, o null si la lista está vacía (no lo elimina)
        System.out.println("peek(): " + linkedList.peek());       // programando

        // peekFirst(): igual que peek(), devuelve el primero
        System.out.println("peekFirst(): " + linkedList.peekFirst()); // programando

        // peekLast(): devuelve el último elemento (no lo elimina)
        System.out.println("peekLast(): " + linkedList.peekLast());   // Jorge

        // poll(): devuelve y elimina el primer elemento, o null si está vacía
        System.out.println("poll(): " + linkedList.poll());       // programando

        // pollFirst(): devuelve y elimina el primero
        System.out.println("pollFirst(): " + linkedList.pollFirst()); // con

        // pollLast(): devuelve y elimina el último
        System.out.println("pollLast(): " + linkedList.pollLast());   // Jorge
        System.out.println("Lista tras polls: " + linkedList); // []

        // push(E e): inserta un elemento al inicio (como en una pila)
        linkedList.push("uno");

        // push(E e): otro ejemplo (se añade arriba de "uno")
        linkedList.push("dos");
        System.out.println("Tras push: " + linkedList); // [dos, uno]

        // pop(): obtiene y elimina el primer elemento (como en una pila LIFO)
        System.out.println("pop(): " + linkedList.pop()); // dos
        System.out.println("Tras pop: " + linkedList); // [uno]

        // addAll(Collection c): añade todos los elementos de otra colección
        linkedList.addAll(java.util.List.of("A", "B", "C", "B"));
        System.out.println("addAll(): " + linkedList); // [uno, A, B, C, B]

        // contains(Object o): devuelve true si existe ese elemento en la lista
        System.out.println("¿Contiene 'B'? " + linkedList.contains("B")); // true

        // indexOf(Object o): devuelve el índice de la primera ocurrencia o -1
        System.out.println("Primera 'B': " + linkedList.indexOf("B"));    // 2

        // lastIndexOf(Object o): devuelve el índice de la última ocurrencia o -1
        System.out.println("Última 'B': " + linkedList.lastIndexOf("B")); // 4

        // removeFirstOccurrence(Object o): elimina la primera aparición del objeto
        linkedList.removeFirstOccurrence("B");

        // removeLastOccurrence(Object o): elimina la última aparición del objeto
        linkedList.removeLastOccurrence("B");
        System.out.println("Tras eliminar ocurrencias: " + linkedList); // [uno, A, C]

        // toArray(): convierte la lista a un array Object[]
        Object[] arreglo = linkedList.toArray();
        System.out.println("toArray() -> length: " + arreglo.length);

        // clear(): elimina todos los elementos de la lista
        linkedList.clear();
        System.out.println("Tras clear(): vacío=" + linkedList.isEmpty() + ", size=" + linkedList.size());


        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - descendingIterator(): devuelve un iterador en orden inverso.
        // - offer(), offerFirst(), offerLast(): para añadir con semántica de colas/deques.
        // - listIterator(int index): iterador empezando en un índice dado.
        // - spliterator(): para Streams / procesamiento paralelo.
        // - clone(): devuelve una copia superficial (shallow copy).
    }
}
