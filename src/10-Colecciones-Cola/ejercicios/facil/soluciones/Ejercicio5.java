import java.util.ArrayDeque;
import java.util.Queue;

public class Ejercicio5 {
    public static void main(String[] args) {
        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(10);
        queue.offer(20);
        queue.offer(30);
        queue.offer(40);
        queue.offer(50);

        System.out.println("Iterando con for-each (no modifica la cola):");
        for (Integer n : queue) {
            System.out.print(n + " ");
        }
        System.out.println();

        System.out.println("\nTamaño tras iteración: " + queue.size()); // 5 — sin cambios
        System.out.println("Primer elemento sigue siendo: " + queue.peek()); // 10
    }
}
