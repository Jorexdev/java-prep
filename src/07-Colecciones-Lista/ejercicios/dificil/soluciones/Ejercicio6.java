import java.util.ArrayList;
import java.util.List;

public class Ejercicio6 {

    public static <T> List<List<T>> particionar(List<T> lista, int n) {
        List<List<T>> resultado = new ArrayList<>();
        int size = lista.size();
        for (int i = 0; i < size; i += n) {
            int fin = Math.min(i + n, size);
            resultado.add(new ArrayList<>(lista.subList(i, fin)));
        }
        return resultado;
    }

    public static void main(String[] args) {
        List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("Lista original: " + numeros);

        List<List<Integer>> partes = particionar(numeros, 3);
        System.out.println("Particionada en grupos de 3:");
        partes.forEach(p -> System.out.println("  " + p));

        List<String> letras = List.of("a", "b", "c", "d", "e", "f", "g");
        System.out.println("\nLetras: " + letras);
        System.out.println("Particionada en grupos de 4:");
        particionar(letras, 4).forEach(p -> System.out.println("  " + p));
    }
}
