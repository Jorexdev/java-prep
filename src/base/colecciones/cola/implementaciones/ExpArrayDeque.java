package base.colecciones.cola.implementaciones;

import java.util.ArrayDeque;

public class ExpArrayDeque {

    /*
        ArrayDeque es una implementación de Deque (doble extremo) basada en un buffer circular.
        Sirve como cola (FIFO) y como pila (LIFO) de alto rendimiento.
        No permite null, y suele ser más rápida que Stack y LinkedList para uso como deque/pila.
     */


    /*                           CONSTRUCTORES                             */

    /*
        - ArrayDeque()
            → Crea un deque vacío.

        - ArrayDeque(int initialCapacity)
            → Crea un deque con capacidad inicial.
     */


    /*                             KEY FEATURES                            */

    /*
        - Deque FIFO/LIFO de alto rendimiento (respaldo por array circular).
        - Operaciones O(1) amortizado en extremos.
        - No permite null.
        - No está sincronizado.
     */


    public static void main(String[] args) {

        ArrayDeque<String> dq = new ArrayDeque<>();

        // offer(E): en modo cola, añade al final (cola)
        dq.offer("Java");      // añade al final
        dq.offer("Spring");
        dq.offer("Hibernate");
        System.out.println("Deque tras offer (cola): " + dq); // [Java, Spring, Hibernate]

        // peek(): mira la cabeza (frente) sin eliminar (cola)
        System.out.println("peek(): " + dq.peek()); // Java

        // poll(): obtiene y elimina la cabeza (cola)
        System.out.println("poll(): " + dq.poll()); // Java
        System.out.println("Tras poll: " + dq);     // [Spring, Hibernate]

        // addFirst(E)/addLast(E): inserta al inicio/fin (deque)
        dq.addFirst("Primero");  // inicio
        dq.addLast("Último");    // fin
        System.out.println("Tras addFirst/addLast: " + dq); // [Primero, Spring, Hibernate, Último]

        // getFirst()/getLast(): obtiene extremos sin eliminar
        System.out.println("getFirst(): " + dq.getFirst()); // Primero
        System.out.println("getLast(): " + dq.getLast());   // Último

        // removeFirst()/removeLast(): elimina extremos
        dq.removeFirst();
        dq.removeLast();
        System.out.println("Tras removeFirst/Last: " + dq); // [Spring, Hibernate]

        // push(E)/pop(): uso como pila (LIFO) en el extremo frontal
        dq.push("uno"); // push añade al frente
        dq.push("dos");
        System.out.println("Tras push: " + dq);     // [dos, uno, Spring, Hibernate]
        System.out.println("pop(): " + dq.pop());   // dos
        System.out.println("Tras pop: " + dq);      // [uno, Spring, Hibernate]

        // clear(): elimina todos los elementos
        dq.clear();
        System.out.println("Tras clear(): size=" + dq.size()); // 0

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - offerFirst/offerLast: como addFirst/addLast pero devuelven false en vez de lanzar excepción.
        // - peekFirst/peekLast: leen extremos sin eliminar (devuelven null si vacío).
        // - pollFirst/pollLast: obtienen y eliminan extremos (devuelven null si vacío).
        // - removeFirstOccurrence/removeLastOccurrence: eliminan primera/última aparición.
        // - ¡No permite null!
    }
}
