import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Ejercicio8 {

    public static void main(String[] args) {
        List<String> palabras = new ArrayList<>(List.of("hola", "mundo", "java", "lista", "iterator"));

        System.out.println("Lista original: " + palabras);

        ListIterator<String> it = palabras.listIterator();
        while (it.hasNext()) {
            String actual = it.next();
            it.set(actual.toUpperCase());
        }

        System.out.println("Lista en mayúsculas: " + palabras);
    }
}
