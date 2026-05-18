import java.util.List;
import java.util.function.Consumer;

public class Ejercicio3 {

    public static void main(String[] args) {

        Consumer<String> imprimirConLongitud = s -> System.out.println(s + " -> " + s.length());

        List<String> palabras = List.of("lambda", "stream", "interfaz", "fun", "composicion");

        palabras.forEach(imprimirConLongitud);
    }
}
