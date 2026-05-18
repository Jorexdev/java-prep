import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ejercicio5 {
    public static void main(String[] args) {
        List<String> nombres = new ArrayList<>(List.of("Zara", null, "Ana", "Luis", null, "Bea"));

        nombres.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        System.out.println("nullsFirst asc:  " + nombres);

        nombres.sort(Comparator.nullsLast(Comparator.naturalOrder()));
        System.out.println("nullsLast asc:   " + nombres);

        nombres.sort(Comparator.nullsLast(Comparator.naturalOrder()).reversed());
        System.out.println("nullsLast desc:  " + nombres);
    }
}
