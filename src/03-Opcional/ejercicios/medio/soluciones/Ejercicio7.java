import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ejercicio7 {

    public static void main(String[] args) {
        List<Optional<String>> lista = Arrays.asList(
                Optional.of("Java"),
                Optional.empty(),
                Optional.of("Kotlin"),
                Optional.empty(),
                Optional.of("Scala"),
                Optional.empty()
        );

        // Optional::stream convierte cada Optional en un Stream<T> de 0 o 1 elementos
        // flatMap aplana todos esos streams → solo los valores presentes
        List<String> presentes = lista.stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        System.out.println("Lista original (tamaño): " + lista.size());
        System.out.println("Solo presentes:          " + presentes);
        System.out.println("Cantidad de presentes:   " + presentes.size());
    }
}
