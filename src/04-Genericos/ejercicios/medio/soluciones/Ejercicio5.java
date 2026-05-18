import java.util.List;

public class Ejercicio5 {

    static <T extends Comparable<T>> T minimo(List<T> lista) {
        if (lista.isEmpty()) throw new IllegalArgumentException("La lista está vacía");
        T min = lista.get(0);
        for (T elemento : lista) {
            if (elemento.compareTo(min) < 0) {
                min = elemento;
            }
        }
        return min;
    }

    public static void main(String[] args) {

        List<Integer> enteros = List.of(5, 3, 8, 1, 9, 2);
        System.out.println("Mínimo entero:  " + minimo(enteros));  // 1

        List<String> palabras = List.of("plátano", "manzana", "kiwi", "cereza");
        System.out.println("Mínimo string:  " + minimo(palabras)); // cereza (orden lexicográfico)

        try {
            minimo(List.of());
        } catch (IllegalArgumentException e) {
            System.out.println("Lista vacía:    " + e.getMessage());
        }
    }
}
