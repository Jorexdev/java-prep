import java.util.ArrayList;
import java.util.List;

public class Ejercicio2 {
    public static void main(String[] args) {
        List<String> frutas = new ArrayList<>(
                List.of("manzana", "pera", "mango", "pera", "uva", "pera"));

        System.out.println("Lista: " + frutas);

        System.out.println("¿Contiene 'mango'? " + frutas.contains("mango"));
        System.out.println("¿Contiene 'kiwi'?  " + frutas.contains("kiwi"));

        System.out.println("Primera 'pera': índice " + frutas.indexOf("pera"));
        System.out.println("Última  'pera': índice " + frutas.lastIndexOf("pera"));
    }
}
