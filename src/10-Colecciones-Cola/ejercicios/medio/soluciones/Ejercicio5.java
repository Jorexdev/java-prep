import java.util.PriorityQueue;

public class Ejercicio5 {
    public static void main(String[] args) {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.add(5);
        pq.add(1);
        pq.add(8);
        pq.add(2);
        pq.add(9);

        // for-each NO garantiza orden de prioridad — itera internamente el array del heap
        System.out.println("Iteración con for-each (orden NO garantizado):");
        System.out.print("  ");
        for (int n : pq) {
            System.out.print(n + " ");
        }
        System.out.println();

        // poll() SÍ garantiza el orden de prioridad
        System.out.println("\nExtracción con poll() (orden correcto):");
        System.out.print("  ");
        while (!pq.isEmpty()) {
            System.out.print(pq.poll() + " ");
        }
        System.out.println();
        System.out.println("\nConclusión: para obtener el orden correcto, usa poll() — no for-each.");
    }
}
