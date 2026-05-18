import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Ejercicio3 {

    public static void main(String[] args) {

        Function<String, String> limpiar = String::trim;
        Function<String, String> mayusculas = String::toUpperCase;

        // andThen: primero limpiar, luego mayusculas
        Function<String, String> limpiarYMayusculas = limpiar.andThen(mayusculas);

        List<String> entradas = List.of("  hola  ", " mundo ", "  java  ", " streams ");

        List<String> resultado = entradas.stream()
                .map(limpiarYMayusculas)
                .collect(Collectors.toList());

        resultado.forEach(System.out::println);
    }
}
