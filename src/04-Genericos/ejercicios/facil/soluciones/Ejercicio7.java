import java.util.ArrayList;

public class Ejercicio7 {

    static class Pila<T> {
        private final ArrayList<T> elementos = new ArrayList<>();

        public void push(T elemento) {
            elementos.add(elemento);
        }

        // Retorna y elimina el elemento en la cima
        public T pop() {
            if (isEmpty()) throw new RuntimeException("Pila vacía");
            return elementos.remove(elementos.size() - 1);
        }

        // Retorna sin eliminar
        public T peek() {
            if (isEmpty()) throw new RuntimeException("Pila vacía");
            return elementos.get(elementos.size() - 1);
        }

        public boolean isEmpty() {
            return elementos.isEmpty();
        }

        public int size() {
            return elementos.size();
        }
    }

    public static void main(String[] args) {
        Pila<String> pila = new Pila<>();

        pila.push("primero");
        pila.push("segundo");
        pila.push("tercero");

        System.out.println("Cima (peek): " + pila.peek()); // tercero
        System.out.println("pop: " + pila.pop());           // tercero
        System.out.println("pop: " + pila.pop());           // segundo
        System.out.println("Tamaño: " + pila.size());       // 1
        System.out.println("¿Vacía? " + pila.isEmpty());    // false
        pila.pop();
        System.out.println("¿Vacía? " + pila.isEmpty());    // true
    }
}
