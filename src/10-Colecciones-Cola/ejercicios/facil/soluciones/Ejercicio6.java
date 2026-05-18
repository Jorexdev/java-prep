import java.util.Comparator;
import java.util.PriorityQueue;

public class Ejercicio6 {
    public static void main(String[] args) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

        maxHeap.add(5);
        maxHeap.add(2);
        maxHeap.add(8);
        maxHeap.add(1);
        maxHeap.add(9);

        System.out.println("PriorityQueue con reverseOrder. Extrayendo con poll():");
        while (!maxHeap.isEmpty()) {
            System.out.print(maxHeap.poll() + " ");
        }
        System.out.println();
        System.out.println("→ Los elementos salen en orden descendente.");
    }
}
