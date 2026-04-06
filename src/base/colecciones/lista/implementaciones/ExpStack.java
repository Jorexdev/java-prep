package base.colecciones.lista.implementaciones;

import java.util.Stack;

public class ExpStack {

    /*
        En Java, los Stack representan una estructura de datos tipo "pila" (LIFO: Last-In, First-Out).
        Están implementados como una subclase de Vector y heredan todos sus métodos, 
        pero añaden operaciones específicas de pila como push, pop, peek y search.

        Aunque es funcional, en la práctica muchas veces se recomienda usar 
        Deque (ejemplo: ArrayDeque) para implementar pilas, ya que ofrece mejor rendimiento.
     */


    /*                           CONSTRUCTORES                             */

    /*
        - Stack()
            → Crea una pila vacía.
     */


    /*                             KEY FEATURES                            */

    /*
        - Stack sigue el principio LIFO (el último en entrar es el primero en salir).
        - Hereda de Vector → sincronizado por defecto (thread-safe).
        - Permite nulos y duplicados.
        - Soporta genéricos (type-safety).
        - Mantiene orden de inserción, pero las operaciones típicas son sobre la "cima".
     */


    /*                            VENTAJAS                                 */

    /*
        - Fácil de usar para implementar comportamiento LIFO.
        - Métodos específicos: push, pop, peek, search.
        - Seguridad en hilos (synchronized).
     */


    /*                           DESVENTAJAS                               */

    /*
        - Al heredar de Vector, puede ser menos eficiente que Deque (ej. ArrayDeque).
        - Considerada clase "legacy" → mejor usar implementaciones modernas.
     */


    public static void main(String[] args) {

        // Crear un Stack vacío
        Stack<String> stack = new Stack<>();

        // push(E e): inserta un elemento en la parte superior de la pila
        stack.push("Java");
        stack.push("Spring");
        stack.push("Hibernate");
        System.out.println("Tras push: " + stack); // [Java, Spring, Hibernate]

        // peek(): devuelve el elemento en la cima sin eliminarlo
        System.out.println("peek(): " + stack.peek()); // Hibernate

        // pop(): devuelve y elimina el elemento en la cima
        System.out.println("pop(): " + stack.pop()); // Hibernate
        System.out.println("Tras pop: " + stack); // [Java, Spring]

        // search(Object o): devuelve la posición (1-based) desde la cima o -1 si no está
        System.out.println("Posición de 'Java': " + stack.search("Java")); // 2
        System.out.println("Posición de 'Spring': " + stack.search("Spring")); // 1
        System.out.println("Posición de 'Hibernate': " + stack.search("Hibernate")); // -1 (ya no está)

        // empty(): devuelve true si la pila está vacía
        System.out.println("¿Está vacía? " + stack.empty()); // false

        // size(): devuelve el número de elementos
        System.out.println("Tamaño actual: " + stack.size());

        // clear(): elimina todos los elementos
        stack.clear();
        System.out.println("Tras clear(): vacío=" + stack.empty() + ", size=" + stack.size());


        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - add(E e), add(int index, E e): heredados de Vector (no típicos de una pila).
        // - capacity(): capacidad interna (heredado de Vector).
        // - elements(): devuelve un Enumeration (forma antigua de recorrer).
        // - toArray(): convierte la pila en un array.
    }
}
