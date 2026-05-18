import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio1 {

    public static void main(String[] args) {

        List<String> palabras = new ArrayList<>(List.of("banana", "kiwi", "manzana", "uva", "fresa"));

        Collections.sort(palabras, (a, b) -> a.length() - b.length());

        palabras.forEach(System.out::println);
    }
}
