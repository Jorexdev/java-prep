import java.util.ArrayDeque;
import java.util.Queue;

public class Ejercicio1 {
    public static void main(String[] args) {
        Queue<String> queue = new ArrayDeque<>();

        queue.offer("A");
        queue.offer("B");
        queue.offer("C");
        queue.offer("D");
        queue.offer("E");

        System.out.println("Primer elemento (peek): " + queue.peek());
        System.out.println("\nExtrayendo en orden FIFO:");
        while (!queue.isEmpty()) {
            System.out.println("  poll() → " + queue.poll());
        }
    }
}
