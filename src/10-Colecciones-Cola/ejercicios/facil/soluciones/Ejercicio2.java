import java.util.ArrayDeque;
import java.util.Queue;

public class Ejercicio2 {
    public static void main(String[] args) {
        Queue<String> cola = new ArrayDeque<>();

        // Clientes que llegan
        cola.offer("Cliente1 — María");
        cola.offer("Cliente2 — Juan");
        cola.offer("Cliente3 — Pedro");
        cola.offer("Cliente4 — Laura");

        System.out.println("Cola de espera: " + cola);
        System.out.println("\nAtendiendo clientes:");
        while (!cola.isEmpty()) {
            System.out.println("  Atendiendo: " + cola.poll());
        }
        System.out.println("Cola vacía. Todos atendidos.");
    }
}
