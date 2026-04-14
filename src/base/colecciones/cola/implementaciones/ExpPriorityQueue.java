package base.colecciones.cola.implementaciones;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ExpPriorityQueue {











    public static void main(String[] args) {

        // Cola de prioridades por orden natural (min-heap)
        PriorityQueue<Integer> pq = new PriorityQueue<>();

        // offer(E): inserta manteniendo la prioridad
        pq.offer(30);
        pq.offer(10);
        pq.offer(20);
        System.out.println("PriorityQueue (natural): " + pq); // heap interno (no orden estricto al imprimir)

        // peek(): ver el mínimo (cabeza) sin eliminar
        System.out.println("peek(): " + pq.peek()); // 10

        // poll(): extrae el mínimo
        System.out.println("poll(): " + pq.poll()); // 10
        System.out.println("Tras poll: " + pq);     // [20, 30] (orden de heap)

        // contains(Object): verifica presencia
        System.out.println("contains(30): " + pq.contains(30)); // true

        // Comparator inverso (max-heap)
        PriorityQueue<Integer> pqMax = new PriorityQueue<>(Comparator.reverseOrder());
        pqMax.add(30); // add: como offer, pero con excepción si no hay capacidad (raro en PQ)
        pqMax.add(10);
        pqMax.add(20);
        System.out.println("PriorityQueue (max-heap): " + pqMax); // heap interno

        // peek(): ver el máximo
        System.out.println("peek() max: " + pqMax.peek()); // 30

        // clear(): vacía la cola
        pqMax.clear();
        System.out.println("clear() → size: " + pqMax.size()); // 0

        // toArray(): convierte a array (no garantiza orden habilitado)
        Object[] arr = pq.toArray();
        System.out.println("toArray().length: " + arr.length);
    }
}
