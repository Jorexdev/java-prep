package base.colecciones.coladoble.implementaciones;

import java.util.ArrayDeque;

public class ExpArrayDeque {



















    public static void main(String[] args) {

        ArrayDeque<String> dq = new ArrayDeque<>();

        // offer(E): añade al final (modo cola)
        dq.offer("Java");
        dq.offer("Spring");
        dq.offer("Hibernate");
        System.out.println("Deque tras offer (cola): " + dq); // [Java, Spring, Hibernate]

        // peek(): lee la cabeza sin eliminar (cola)
        System.out.println("peek(): " + dq.peek()); // Java

        // poll(): obtiene y elimina la cabeza (cola)
        System.out.println("poll(): " + dq.poll()); // Java
        System.out.println("Tras poll: " + dq);     // [Spring, Hibernate]

        // addFirst(E) / addLast(E): inserta al inicio/fin (deque)
        dq.addFirst("Primero");
        dq.addLast("Último");
        System.out.println("Tras addFirst/addLast: " + dq); // [Primero, Spring, Hibernate, Último]

        // getFirst() / getLast(): obtiene extremos sin eliminar
        System.out.println("getFirst(): " + dq.getFirst()); // Primero
        System.out.println("getLast(): " + dq.getLast());   // Último

        // removeFirst() / removeLast(): elimina extremos
        dq.removeFirst();
        dq.removeLast();
        System.out.println("Tras removeFirst/Last: " + dq); // [Spring, Hibernate]

        // push(E) / pop(): uso como pila (LIFO) en el extremo frontal
        dq.push("uno");
        dq.push("dos");
        System.out.println("Tras push: " + dq);   // [dos, uno, Spring, Hibernate]
        System.out.println("pop(): " + dq.pop()); // dos
        System.out.println("Tras pop: " + dq);    // [uno, Spring, Hibernate]

        // clear(): elimina todos los elementos
        dq.clear();
        System.out.println("Tras clear(): size=" + dq.size()); // 0

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - offerFirst/offerLast: como addFirst/addLast pero devuelven false en lugar de lanzar excepción.
        // - peekFirst/peekLast: leen extremos sin eliminar (null si vacío).
        // - pollFirst/pollLast: obtienen y eliminan extremos (null si vacío).
        // - removeFirstOccurrence/removeLastOccurrence: eliminan primera/última aparición.
        // - ¡No permite null!
    }
}
