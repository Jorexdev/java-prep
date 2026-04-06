package base.colecciones.cola.implementaciones;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ExpConcurrentLinkedQueue {

    /*
        ConcurrentLinkedQueue es una cola no bloqueante (lock-free) y thread-safe
        basada en nodos enlazados. Ideal para alta concurrencia.
     */


    /*                           CONSTRUCTORES                             */

    /*
        - ConcurrentLinkedQueue()
            → Crea una cola vacía.

        - ConcurrentLinkedQueue(Collection<? extends E> c)
            → Crea una cola con los elementos de la colección.
     */


    /*                             KEY FEATURES                            */

    /*
        - Seguridad en hilos (no bloqueante).
        - Métodos típicos de Queue: offer, poll, peek.
        - Operaciones en tiempo amortizado O(1).
        - Tamaño (size) es O(n) y puede ser aproximado bajo alta concurrencia.
        - No permite null.
     */


    public static void main(String[] args) {

        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();

        // offer(E): inserta al final de la cola
        clq.offer("Java");
        clq.offer("Spring");
        clq.offer("Hibernate");
        System.out.println("CLQ tras offer: " + clq);

        // peek(): lee la cabeza SIN eliminar
        System.out.println("peek(): " + clq.peek()); // Java

        // poll(): obtiene y elimina la cabeza
        System.out.println("poll(): " + clq.poll()); // Java
        System.out.println("Tras poll: " + clq);     // [Spring, Hibernate]

        // isEmpty(): true si no hay elementos
        System.out.println("isEmpty(): " + clq.isEmpty()); // false

        // size(): número de elementos (puede ser costoso/concurrentemente aproximado)
        System.out.println("size(): " + clq.size()); // 2

        // remove(Object): elimina la primera ocurrencia del elemento
        clq.remove("Spring");
        System.out.println("Tras remove('Spring'): " + clq); // [Hibernate]

        // clear(): elimina todos los elementos
        clq.clear();
        System.out.println("Tras clear(): " + clq.isEmpty()); // true

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - addAll(Collection c): inserta todos los elementos de otra colección.
        // - forEach(Consumer): recorrer de forma thread-safe sin bloquear.
        // - iterator(): débilmente consistente (no lanza ConcurrentModificationException).
    }
}
