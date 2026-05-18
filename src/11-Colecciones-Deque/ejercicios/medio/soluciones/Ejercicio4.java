import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio4 {

    static class PriorityDeque {
        private final Deque<String> deque = new ArrayDeque<>();

        /** Alta prioridad: se inserta al frente */
        void offerHigh(String msg) {
            deque.addFirst(msg);
            System.out.println("  [HIGH]   addFirst: " + msg);
        }

        /** Prioridad normal: se inserta al final */
        void offerNormal(String msg) {
            deque.addLast(msg);
            System.out.println("  [NORMAL] addLast:  " + msg);
        }

        String poll() {
            return deque.pollFirst();
        }

        boolean isEmpty() { return deque.isEmpty(); }
    }

    public static void main(String[] args) {
        PriorityDeque cola = new PriorityDeque();

        System.out.println("Añadiendo mensajes:");
        cola.offerNormal("Tarea rutinaria A");
        cola.offerNormal("Tarea rutinaria B");
        cola.offerHigh("Alerta crítica!");
        cola.offerNormal("Tarea rutinaria C");
        cola.offerHigh("Error en producción!");

        System.out.println("\nProcesando:");
        while (!cola.isEmpty()) {
            System.out.println("  poll() → " + cola.poll());
        }
    }
}
