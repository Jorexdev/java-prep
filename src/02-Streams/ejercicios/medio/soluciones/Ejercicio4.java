import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio4 {
    record Persona(String nombre, int edad) {}

    public static void main(String[] args) {
        List<String> tecnologias = List.of("Java", "Spring", "Kafka", "Docker", "Kubernetes");

        String unido = tecnologias.stream()
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println(unido);

        List<Persona> personas = List.of(
            new Persona("Ana", 30),
            new Persona("Luis", 25),
            new Persona("Marta", 35)
        );
        String csv = personas.stream()
            .map(p -> p.nombre() + "," + p.edad())
            .collect(Collectors.joining("\n", "nombre,edad\n", ""));
        System.out.println(csv);
    }
}
