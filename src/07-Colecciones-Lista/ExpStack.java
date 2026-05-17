import java.util.Stack;

public class ExpStack {



















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
