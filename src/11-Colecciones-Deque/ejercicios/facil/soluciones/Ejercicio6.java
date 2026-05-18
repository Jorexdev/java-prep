import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio6 {
    public static void main(String[] args) {
        Deque<Integer> deque = new ArrayDeque<>();
        deque.addLast(1);
        deque.addLast(2);
        deque.addLast(3);
        deque.addLast(4);
        deque.addLast(5);

        System.out.println("Deque: " + deque);
        System.out.println("peekFirst() → " + deque.peekFirst()); // 1  (no modifica)
        System.out.println("peekLast()  → " + deque.peekLast());  // 5  (no modifica)
        System.out.println("Tamaño tras peek: " + deque.size());   // 5 — sin cambios
        System.out.println("Deque sigue igual: " + deque);
    }
}
