import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ejercicio2 {
    record Persona(String nombre, String ciudad) {}

    public static void main(String[] args) {
        List<Persona> personas = List.of(
            new Persona("Ana",    "Madrid"),
            new Persona("Luis",   "Barcelona"),
            new Persona("Marta",  "Madrid"),
            new Persona("Carlos", "Valencia"),
            new Persona("Jorge",  "Barcelona"),
            new Persona("Bea",    "Madrid")
        );

        Map<String, Long> conteo = personas.stream()
            .collect(Collectors.groupingBy(Persona::ciudad, Collectors.counting()));
        System.out.println("Conteo por ciudad: " + conteo);

        Map<String, List<String>> nombresPorCiudad = personas.stream()
            .collect(Collectors.groupingBy(Persona::ciudad,
                     Collectors.mapping(Persona::nombre, Collectors.toList())));
        nombresPorCiudad.forEach((ciudad, nombres) ->
            System.out.println(ciudad + ": " + nombres));
    }
}
