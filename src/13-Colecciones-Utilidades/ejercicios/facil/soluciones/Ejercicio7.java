import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Ejercicio7 {
    public static void main(String[] args) {
        List<String> palabras = new ArrayList<>(
            List.of("alfa", "beta", "agua", "casa", "arbol", "mesa", "azul")
        );

        System.out.println("Antes:  " + palabras);

        Iterator<String> it = palabras.iterator();
        while (it.hasNext()) {
            if (it.next().startsWith("a")) it.remove();
        }

        System.out.println("Después: " + palabras);
    }
}
