import java.util.ArrayDeque;
import java.util.Queue;

public class Ejercicio6 {

    static class StackConColas<T> {
        private Queue<T> q1 = new ArrayDeque<>();
        private Queue<T> q2 = new ArrayDeque<>();

        public void push(T valor) {
            q1.offer(valor);
        }

        public T pop() {
            if (q1.isEmpty()) throw new java.util.NoSuchElementException("Stack vacío");

            // Mover todos los elementos de q1 a q2, excepto el último
            while (q1.size() > 1) {
                q2.offer(q1.poll());
            }
            T tope = q1.poll(); // el último de q1 es el que se apilò último (LIFO)

            // Intercambiar q1 y q2
            Queue<T> temp = q1;
            q1 = q2;
            q2 = temp;

            return tope;
        }

        public T peek() {
            if (q1.isEmpty()) throw new java.util.NoSuchElementException("Stack vacío");
            while (q1.size() > 1) q2.offer(q1.poll());
            T tope = q1.poll();
            q2.offer(tope);
            Queue<T> temp = q1; q1 = q2; q2 = temp;
            return tope;
        }

        public boolean isEmpty() { return q1.isEmpty(); }
        public int size()        { return q1.size(); }
    }

    public static void main(String[] args) {
        StackConColas<Integer> stack = new StackConColas<>();

        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);

        System.out.println("Stack LIFO con dos Queues:");
        System.out.println("peek() → " + stack.peek()); // 4
        while (!stack.isEmpty()) {
            System.out.println("pop()  → " + stack.pop());
        }
        // Orden esperado: 4, 3, 2, 1
    }
}
