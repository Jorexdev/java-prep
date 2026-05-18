import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio1 {
    public static void main(String[] args) {
        Deque<String> stack = new ArrayDeque<>();

        stack.push("A");
        stack.push("B");
        stack.push("C");
        stack.push("D");
        stack.push("E");

        System.out.println("Tope actual (peek): " + stack.peek());
        System.out.println("\nDesapilando (orden LIFO):");
        while (!stack.isEmpty()) {
            System.out.println("  pop() → " + stack.pop());
        }
    }
}
