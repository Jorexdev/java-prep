import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio4 {
    public static void main(String[] args) {
        Deque<String> historial = new ArrayDeque<>();

        historial.push("escribir 'Hola mundo'");
        historial.push("copiar selección");
        historial.push("pegar texto");

        System.out.println("Historial de acciones: " + historial);
        System.out.println("Última acción: " + historial.peek());

        System.out.println("\n--- UNDO ---");
        String deshecha = historial.pop();
        System.out.println("Deshaciendo: " + deshecha);
        System.out.println("Historial tras undo: " + historial);
    }
}
