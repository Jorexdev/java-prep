import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio2 {
    public static void main(String[] args) {
        List<String> nombres = List.of("ana", "luis", "carlos", "marta", "jorge");
        List<String> mayusculas = nombres.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
        System.out.println(mayusculas);
    }
}
