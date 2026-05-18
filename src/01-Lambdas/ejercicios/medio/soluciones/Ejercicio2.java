import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Ejercicio2 {

    public static void main(String[] args) {

        Predicate<String> longitudMayorQueTres = s -> s.length() > 3;

        Predicate<String> empiezaPorVocal = s -> !s.isEmpty()
                && "aeiouAEIOU".indexOf(s.charAt(0)) >= 0;

        Predicate<String> ambas = longitudMayorQueTres.and(empiezaPorVocal);

        List<String> palabras = List.of("ana", "elefante", "sol", "isla", "oro", "universo", "ola");

        List<String> resultado = palabras.stream()
                .filter(ambas)
                .collect(Collectors.toList());

        System.out.println("Cumplen ambas condiciones: " + resultado);
    }
}
