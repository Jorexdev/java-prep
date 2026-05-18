import java.util.ArrayList;
import java.util.List;

public class Ejercicio7 {

    public static List<Integer> fusionar(List<Integer> a, List<Integer> b) {
        List<Integer> resultado = new ArrayList<>();
        int i = 0, j = 0;

        while (i < a.size() && j < b.size()) {
            if (a.get(i) <= b.get(j)) {
                resultado.add(a.get(i++));
            } else {
                resultado.add(b.get(j++));
            }
        }

        // Añadir los elementos restantes
        while (i < a.size()) resultado.add(a.get(i++));
        while (j < b.size()) resultado.add(b.get(j++));

        return resultado;
    }

    public static void main(String[] args) {
        List<Integer> listaA = List.of(1, 3, 5);
        List<Integer> listaB = List.of(2, 4, 6);
        System.out.println("Lista A: " + listaA);
        System.out.println("Lista B: " + listaB);
        System.out.println("Fusionada: " + fusionar(listaA, listaB)); // [1,2,3,4,5,6]

        List<Integer> listaC = List.of(1, 2, 8, 10);
        List<Integer> listaD = List.of(3, 5, 7, 9, 11);
        System.out.println("\nLista C: " + listaC);
        System.out.println("Lista D: " + listaD);
        System.out.println("Fusionada: " + fusionar(listaC, listaD));
    }
}
