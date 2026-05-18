import java.util.ArrayList;

public class Ejercicio1 {
    public static void main(String[] args) {
        ArrayList<String> nombres = new ArrayList<>();
        nombres.add("Carlos");
        nombres.add("Lucía");
        nombres.add("Ana");
        nombres.add("Pedro");
        nombres.add("Marta");
        System.out.println("Original:          " + nombres);

        nombres.remove(2); // elimina por índice → "Ana" en pos 2
        System.out.println("Tras remove(2):    " + nombres);

        nombres.remove("Ana"); // elimina por valor la primera ocurrencia
        System.out.println("Tras remove(\"Ana\"): " + nombres);
    }
}
