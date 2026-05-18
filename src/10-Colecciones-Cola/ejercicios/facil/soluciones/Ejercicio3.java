import java.util.PriorityQueue;

public class Ejercicio3 {
    public static void main(String[] args) {
        PriorityQueue<Integer> pq = new PriorityQueue<>();

        pq.add(5);
        pq.add(2);
        pq.add(8);
        pq.add(1);
        pq.add(9);
        pq.add(3);

        System.out.println("PriorityQueue (min-heap). Extrayendo con poll():");
        while (!pq.isEmpty()) {
            System.out.print(pq.poll() + " ");
        }
        System.out.println();
        System.out.println("→ Los elementos salen en orden ascendente.");
    }
}
