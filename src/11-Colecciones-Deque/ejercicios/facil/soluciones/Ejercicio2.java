import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio2 {
    public static void main(String[] args) {
        Deque<Integer> queue = new ArrayDeque<>();

        queue.offerLast(1);
        queue.offerLast(2);
        queue.offerLast(3);
        queue.offerLast(4);
        queue.offerLast(5);

        System.out.println("Extrayendo (orden FIFO):");
        while (!queue.isEmpty()) {
            System.out.println("  pollFirst() → " + queue.pollFirst());
        }
    }
}
