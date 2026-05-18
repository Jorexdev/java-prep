import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

public class Ejercicio8 {
    public static void main(String[] args) {
        List<String> palabras = List.of("hola", "mundo", "java", "streams", "api");
        int sumaLongitudes = palabras.stream().mapToInt(String::length).sum();
        OptionalDouble media = palabras.stream().mapToInt(String::length).average();
        System.out.println("Suma longitudes: " + sumaLongitudes);
        System.out.printf("Media longitud:  %.2f%n", media.orElse(0));

        System.out.print("Range 1-10: ");
        IntStream.rangeClosed(1, 10).forEach(n -> System.out.print(n + " "));
        System.out.println();
    }
}
