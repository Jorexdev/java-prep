import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EjercicioExtra {

    static List<Persona> people = List.of(
            new Persona("Jorge", List.of("Amazon", "Oracle")),
            new Persona("Luis",  List.of("BytesColaborativos", "Oracle")),
            new Persona("David", List.of("Microsoft", "Red Hat")),
            new Persona("Juan",  List.of("Amazon", "Google")),
            new Persona("Ana",   List.of("Google", "BytesColaborativos")),
            new Persona("Izaro", List.of("Amazon", "Microsoft")));

    public static void main(String[] args) {

        // Ejercicio extra: top 3 empresas más mencionadas entre todas las personas
        people.stream()
                .map(Persona::getTitles)
                .flatMap(List::stream)                         // aplana a Stream<String> de empresas
                .collect(Collectors.groupingBy(
                        Function.identity(),                   // clave: nombre de la empresa
                        Collectors.counting()                  // valor: veces que aparece
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // más frecuentes primero
                .limit(3)                                                      // top 3
                .forEach(System.out::println);
    }

    static class Persona {

        private final List<String> titles;

        Persona(String nombre, List<String> titles) {
            this.titles = titles;
        }

        public List<String> getTitles() {
            return titles;
        }
    }
}
