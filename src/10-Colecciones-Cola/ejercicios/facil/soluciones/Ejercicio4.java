import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

public class Ejercicio4 {
    public static void main(String[] args) {
        Queue<String> vacia = new ArrayDeque<>();

        // poll() → retorna null sin lanzar excepción
        String resultado = vacia.poll();
        System.out.println("poll() en cola vacía → " + resultado); // null

        // remove() → lanza NoSuchElementException
        try {
            vacia.remove();
        } catch (NoSuchElementException e) {
            System.out.println("remove() en cola vacía → " + e.getClass().getSimpleName());
        }

        System.out.println("\nResumen:");
        System.out.println("  poll()   — seguro, retorna null si vacía");
        System.out.println("  remove() — lanza NoSuchElementException si vacía");
    }
}
