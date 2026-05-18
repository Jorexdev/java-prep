import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Ejercicio1 {

    static class ListaOrdenada<T> {
        private final List<T> elementos = new ArrayList<>();
        private final Comparator<T> comparador;

        ListaOrdenada(Comparator<T> comparador) { this.comparador = comparador; }

        void add(T elemento) {
            elementos.add(elemento);
            elementos.sort(comparador);
        }

        T get(int indice)    { return elementos.get(indice); }
        int size()           { return elementos.size(); }
        List<T> toList()     { return Collections.unmodifiableList(elementos); }

        @Override public String toString() { return elementos.toString(); }
    }

    public static void main(String[] args) {
        ListaOrdenada<Integer> nums = new ListaOrdenada<>(Integer::compareTo);
        nums.add(5); nums.add(2); nums.add(8); nums.add(1); nums.add(4);
        System.out.println("Enteros asc: " + nums);

        ListaOrdenada<String> palabras = new ListaOrdenada<>(
            Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())
        );
        palabras.add("banana"); palabras.add("kiwi"); palabras.add("uva"); palabras.add("manzana");
        System.out.println("Strings por longitud: " + palabras);
    }
}
