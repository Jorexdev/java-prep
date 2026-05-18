import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Ejercicio3 {

    static <T> List<T> filtrar(List<T> lista, Predicate<T> pred) {
        List<T> resultado = new ArrayList<>();
        for (T elemento : lista) {
            if (pred.test(elemento)) {
                resultado.add(elemento);
            }
        }
        return resultado;
    }

    public static void main(String[] args) {

        List<String> palabras = List.of("si", "no", "hola", "ok", "java", "stream");
        List<String> largas   = filtrar(palabras, s -> s.length() > 3);
        System.out.println("Strings con longitud > 3: " + largas);

        List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> pares   = filtrar(numeros, n -> n % 2 == 0);
        System.out.println("Números pares:            " + pares);
    }
}
