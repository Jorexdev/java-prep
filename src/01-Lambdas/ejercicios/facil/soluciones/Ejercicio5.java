import java.util.List;
import java.util.function.Supplier;

public class Ejercicio5 {

    public static void main(String[] args) {

        Supplier<List<String>> nombres = () -> List.of("Jorge", "Ana", "Luis");

        System.out.println("Primera llamada:  " + nombres.get());
        System.out.println("Segunda llamada:  " + nombres.get());
    }
}
