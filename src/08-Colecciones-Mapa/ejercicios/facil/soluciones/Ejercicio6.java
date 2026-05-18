import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Ejercicio6 {

    public static void main(String[] args) {
        List<String> nombres = List.of("Ana", "Luis", "Marta");
        List<Integer> edades  = List.of(25, 30, 28);

        Map<String, Integer> mapa = IntStream.range(0, nombres.size())
            .boxed()
            .collect(Collectors.toMap(nombres::get, edades::get));

        System.out.println("Mapa nombre -> edad:");
        mapa.forEach((nombre, edad) ->
            System.out.println("  " + nombre + ": " + edad)
        );
    }
}
