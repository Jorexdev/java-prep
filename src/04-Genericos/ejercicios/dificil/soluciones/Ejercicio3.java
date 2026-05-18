import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Ejercicio3 {

    static <T> Optional<T> primerQueCompleta(List<T> lista, Predicate<T> pred) {
        return lista.stream().filter(pred).findFirst();
    }

    public static void main(String[] args) {

        List<Integer> numeros = List.of(1, 3, 5, 4, 7, 8, 2);
        Optional<Integer> primerPar = primerQueCompleta(numeros, n -> n % 2 == 0);
        System.out.println("Primer par: " + primerPar.orElse(-1)); // 4

        List<String> nombres = List.of("Ana", "Luis", "Jorge", "Carla", "Juan");
        Optional<String> conJ = primerQueCompleta(nombres, s -> s.startsWith("J"));
        System.out.println("Primer con J: " + conJ.orElse("ninguno")); // Jorge

        Optional<Integer> ningun = primerQueCompleta(numeros, n -> n > 100);
        System.out.println("Mayor de 100: " + ningun.orElse(-1)); // -1
    }
}
