import java.util.ArrayList;
import java.util.List;

public class Ejercicio4 {

    public static List<Integer> rotar(List<Integer> lista, int n) {
        int size = lista.size();
        // Los últimos n elementos van al principio
        List<Integer> parte2 = new ArrayList<>(lista.subList(size - n, size));
        List<Integer> parte1 = new ArrayList<>(lista.subList(0, size - n));
        List<Integer> resultado = new ArrayList<>();
        resultado.addAll(parte2);
        resultado.addAll(parte1);
        return resultado;
    }

    public static void main(String[] args) {
        List<Integer> lista = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        System.out.println("Lista original:       " + lista);

        List<Integer> rotada2 = rotar(lista, 2);
        System.out.println("Rotada 2 posiciones:  " + rotada2);

        List<Integer> rotada1 = rotar(lista, 1);
        System.out.println("Rotada 1 posición:    " + rotada1);
    }
}
