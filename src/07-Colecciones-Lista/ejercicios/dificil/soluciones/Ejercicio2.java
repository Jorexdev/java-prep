import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio2 {

    public static void invertir(List<Integer> lista) {
        int izq = 0;
        int der = lista.size() - 1;
        while (izq < der) {
            Collections.swap(lista, izq, der);
            izq++;
            der--;
        }
    }

    public static void main(String[] args) {
        List<Integer> lista = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        System.out.println("Original:  " + lista);
        invertir(lista);
        System.out.println("Invertida: " + lista); // [5, 4, 3, 2, 1]

        List<Integer> par = new ArrayList<>(List.of(10, 20, 30, 40));
        System.out.println("\nOriginal:  " + par);
        invertir(par);
        System.out.println("Invertida: " + par); // [40, 30, 20, 10]
    }
}
