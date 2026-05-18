import java.util.ArrayList;
import java.util.List;

public class Ejercicio6 {
    public static void main(String[] args) {
        List<String> palabras = new ArrayList<>(
                List.of("sol", "luna", "mar", "viento", "río", "tormenta", "nube", "cielo"));
        System.out.println("Antes:  " + palabras);

        palabras.removeIf(p -> p.length() < 4);
        System.out.println("Después (length >= 4): " + palabras);
    }
}
