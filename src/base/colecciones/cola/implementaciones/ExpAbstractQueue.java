package base.colecciones.cola.implementaciones;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;

public class ExpAbstractQueue {













    // Ejemplo mínimo: una cola respaldada por LinkedList
    static class MiQueue<E> extends AbstractQueue<E> {
        private final LinkedList<E> respaldo = new LinkedList<>();

        // offer(E e): inserta elemento al final; devuelve true si se añadió
        @Override
        public boolean offer(E e) {
            return respaldo.offer(e);
        }

        // poll(): obtiene y elimina la cabeza o null si está vacía
        @Override
        public E poll() {
            return respaldo.poll();
        }

        // peek(): obtiene (sin eliminar) la cabeza o null si está vacía
        @Override
        public E peek() {
            return respaldo.peek();
        }

        // iterator(): recorre los elementos de la cola en orden
        @Override
        public Iterator<E> iterator() {
            return respaldo.iterator();
        }

        // size(): número de elementos
        @Override
        public int size() {
            return respaldo.size();
        }
    }

    public static void main(String[] args) {

        MiQueue<String> q = new MiQueue<>();

        // offer(E): inserta al final, devuelve true si se añadió
        q.offer("Java");
        q.offer("Spring");
        q.offer("Hibernate");
        System.out.println("Queue tras offer: " + q); // [Java, Spring, Hibernate]

        // peek(): mira la cabeza SIN eliminar
        System.out.println("peek(): " + q.peek()); // Java

        // poll(): obtiene y elimina la cabeza
        System.out.println("poll(): " + q.poll()); // Java
        System.out.println("Tras poll: " + q);     // [Spring, Hibernate]

        // add(E): como offer pero lanza IllegalStateException si no puede añadir
        q.add("JPA");
        System.out.println("Tras add: " + q); // [Spring, Hibernate, JPA]

        // element(): como peek, pero lanza NoSuchElementException si está vacía
        System.out.println("element(): " + q.element()); // Spring

        // remove(): como poll, pero lanza NoSuchElementException si está vacía
        System.out.println("remove(): " + q.remove()); // Spring
        System.out.println("Tras remove: " + q);       // [Hibernate, JPA]
    }
}
