package base.colecciones.coladoble.implementaciones;

import java.util.LinkedList;

public class ExpLinkedListDeque {

    /*
        LinkedList implementa Deque mediante una lista doblemente enlazada.
        Permite operaciones eficientes O(1) en extremos, y funciona como cola o pila.
        A diferencia de ArrayDeque, sí permite nulos (aunque es desaconsejado).

        Literalmente es LinkedList a secas, si ya lo viste en ExpLinkedList no hay nada nuevo
     */


    /*                           CONSTRUCTORES                             */

    /*
        - LinkedList()
            → Crea una lista/deque vacía.

        - LinkedList(Collection<? extends E> c)
            → Crea una lista/deque con los elementos de la colección dada.
     */


    /*                             KEY FEATURES                            */

    /*
        - Deque respaldado por nodos doblemente enlazados.
        - O(1) en add/remove/get de extremos.
        - Permite null (no recomendado).
        - También implementa List (acceso por índice O(n)).
        - No está sincronizado.
     */


    /*                            VENTAJAS                                 */

    /*
        - Inserciones/eliminaciones muy rápidas en extremos.
        - API completa: List + Deque (versátil).
     */


    /*                           DESVENTAJAS                               */

    /*
        - Overhead de memoria por referencias prev/next en cada nodo.
        - Acceso por índice O(n) (más lento que ArrayDeque para accesos aleatorios).
     */


    public static void main(String[] args) {

        LinkedList<String> dq = new LinkedList<>();

        // offer(E): añade al final (modo cola)
        dq.offer("Java");
        dq.offer("Spring");
        dq.offer("Hibernate");
        System.out.println("LinkedList-Dequed tras offer: " + dq); // [Java, Spring, Hibernate]

        // peekFirst() / peekLast(): leen extremos sin eliminar
        System.out.println("peekFirst(): " + dq.peekFirst()); // Java
        System.out.println("peekLast(): " + dq.peekLast());   // Hibernate

        // pollFirst() / pollLast(): obtienen y eliminan extremos
        System.out.println("pollFirst(): " + dq.pollFirst()); // Java
        System.out.println("pollLast(): " + dq.pollLast());   // Hibernate
        System.out.println("Tras polls: " + dq);              // [Spring]

        // addFirst(E) / addLast(E): inserta en ambos extremos
        dq.addFirst("Primero");
        dq.addLast("Último");
        System.out.println("Tras addFirst/addLast: " + dq); // [Primero, Spring, Último]

        // push(E) / pop(): uso como pila (LIFO)
        dq.push("uno"); // añade al frente
        dq.push("dos");
        System.out.println("Tras push: " + dq);   // [dos, uno, Primero, Spring, Último]
        System.out.println("pop(): " + dq.pop()); // dos
        System.out.println("Tras pop: " + dq);    // [uno, Primero, Spring, Último]

        // removeFirstOccurrence / removeLastOccurrence
        dq.removeFirstOccurrence("Spring");
        dq.removeLastOccurrence("Último");
        System.out.println("Tras removeFirst/LastOccurrence: " + dq);

        // clear(): eliminar todos
        dq.clear();
        System.out.println("Tras clear(): size=" + dq.size()); // 0

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - getFirst/getLast: obtienen extremos (lanzan excepción si vacío).
        // - removeFirst/removeLast: eliminan extremos (excepción si vacío).
        // - descendingIterator(): iteración en orden inverso.
        // - Como List: add(index, e), get(index), set(index, e) (O(n)).
        // - Permite null (ojo a NPE en comparaciones/ordenaciones).
    }
}
