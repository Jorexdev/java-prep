package base.colecciones.coladoble.implementaciones;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ExpConcurrentLinkedDeque {



















    public static void main(String[] args) {

        ConcurrentLinkedDeque<String> dq = new ConcurrentLinkedDeque<>();

        // offerFirst / offerLast: insertar en extremos (devuelven true/false)
        dq.offerFirst("front-1");
        dq.offerLast("back-1");
        dq.offerLast("back-2");
        System.out.println("CLD tras offers: " + dq);

        // peekFirst / peekLast: leen extremos sin eliminar
        System.out.println("peekFirst(): " + dq.peekFirst()); // front-1
        System.out.println("peekLast(): " + dq.peekLast());   // back-2

        // pollFirst / pollLast: obtienen y eliminan extremos
        System.out.println("pollFirst(): " + dq.pollFirst()); // front-1
        System.out.println("pollLast(): " + dq.pollLast());   // back-2
        System.out.println("Tras polls: " + dq);              // [back-1]

        // remove(Object): elimina la primera ocurrencia si existe
        dq.remove("back-1");
        System.out.println("Tras remove('back-1'): " + dq);   // []

        // isEmpty() / size(): estado de la deque
        System.out.println("isEmpty(): " + dq.isEmpty());
        System.out.println("size(): " + dq.size()); // O(n), aproximado en concurrencia

        // clear(): elimina todos los elementos
        dq.clear();
        System.out.println("Tras clear(): " + dq.isEmpty()); // true

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - addFirst/addLast: como offer* pero con excepción si no se puede (raro aquí).
        // - iterator(): débilmente consistente, apto para concurrencia.
        // - forEach(Consumer): recorrido thread-safe sin bloquear.
        // - No permite null.
    }
}
