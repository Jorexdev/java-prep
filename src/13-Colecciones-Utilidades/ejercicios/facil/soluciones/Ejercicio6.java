import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio6 {
    public static void main(String[] args) {
        List<String> mutable = new ArrayList<>(List.of("uno", "dos", "tres"));
        List<String> inmutable = Collections.unmodifiableList(mutable);

        System.out.println("Lista: " + inmutable);

        try {
            inmutable.add("cuatro");
        } catch (UnsupportedOperationException e) {
            System.out.println("UnsupportedOperationException capturada — no se puede modificar");
        }

        mutable.add("cuatro");
        System.out.println("Vista tras modificar original: " + inmutable);
    }
}
