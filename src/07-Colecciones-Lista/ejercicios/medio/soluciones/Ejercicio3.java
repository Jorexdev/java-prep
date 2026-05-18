import java.util.LinkedList;

public class Ejercicio3 {

    public static void main(String[] args) {
        // --- LinkedList como STACK (LIFO) ---
        System.out.println("=== LinkedList como Stack (LIFO) ===");
        LinkedList<String> stack = new LinkedList<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");
        stack.push("D");
        stack.push("E");
        System.out.println("peek (tope): " + stack.peek());
        System.out.println("Extrayendo en orden LIFO:");
        while (!stack.isEmpty()) {
            System.out.println("  pop -> " + stack.pop());
        }

        // --- LinkedList como QUEUE (FIFO) ---
        System.out.println("\n=== LinkedList como Queue (FIFO) ===");
        LinkedList<String> queue = new LinkedList<>();
        queue.offer("A");
        queue.offer("B");
        queue.offer("C");
        queue.offer("D");
        queue.offer("E");
        System.out.println("peek (frente): " + queue.peek());
        System.out.println("Extrayendo en orden FIFO:");
        while (!queue.isEmpty()) {
            System.out.println("  poll -> " + queue.poll());
        }
    }
}
