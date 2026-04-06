package base.colecciones.concurrencia.ejemplos;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ExpConcurrentLinkedQueue {

    /*
        ConcurrentLinkedQueue es una cola concurrente lock-free (no bloqueante)
        basada en nodos enlazados con punteros atómicos. Ideal para alta concurrencia.
     */

    /*                             KEY FEATURES                            */

    /*
        - Thread-safe sin bloqueos globales.
        - Operaciones típicas O(1) amortizado: offer, poll, peek.
        - Iterador débilmente consistente (sin C.M.E.).
        - No permite null.
    */

    public static void main(String[] args) {

        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();

        // offer(E): inserta al final (devuelve true si se añadió)
        q.offer("Java");
        q.offer("Spring");
        q.offer("Hibernate");
        System.out.println("CLQ tras offer → " + q);

        // peek(): lee la cabeza sin eliminar
        System.out.println("peek() → " + q.peek()); // Java

        // poll(): obtiene y elimina la cabeza
        System.out.println("poll() → " + q.poll()); // Java
        System.out.println("Tras poll → " + q);     // [Spring, Hibernate]

        // remove(Object): elimina primera ocurrencia si existe
        q.remove("Spring");
        System.out.println("Tras remove('Spring') → " + q); // [Hibernate]

        // isEmpty / size (size puede ser O(n) y aproximado en alta concurrencia)
        System.out.println("isEmpty → " + q.isEmpty() + ", size → " + q.size());

        // clear(): vacía la cola
        q.clear();
        System.out.println("Tras clear → isEmpty: " + q.isEmpty());

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - forEach/iterator: iteración segura sin bloqueo (débilmente consistente).
        // - add(E): similar a offer (aquí no falla por capacidad).
    }
}
