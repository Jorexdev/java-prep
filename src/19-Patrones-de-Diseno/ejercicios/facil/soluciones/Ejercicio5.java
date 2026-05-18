import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ejercicio5 {

    interface EstrategiaOrden<T> {
        List<T> ordenar(List<T> lista);
    }

    static class OrdenarAscendente<T extends Comparable<T>> implements EstrategiaOrden<T> {
        @Override public List<T> ordenar(List<T> lista) {
            List<T> copia = new ArrayList<>(lista);
            copia.sort(Comparator.naturalOrder());
            return copia;
        }
    }

    static class OrdenarDescendente<T extends Comparable<T>> implements EstrategiaOrden<T> {
        @Override public List<T> ordenar(List<T> lista) {
            List<T> copia = new ArrayList<>(lista);
            copia.sort(Comparator.reverseOrder());
            return copia;
        }
    }

    static class OrdenarPorLongitud implements EstrategiaOrden<String> {
        @Override public List<String> ordenar(List<String> lista) {
            List<String> copia = new ArrayList<>(lista);
            copia.sort(Comparator.comparingInt(String::length));
            return copia;
        }
    }

    static class Ordenador<T> {
        private EstrategiaOrden<T> estrategia;

        Ordenador(EstrategiaOrden<T> estrategia) { this.estrategia = estrategia; }
        void setEstrategia(EstrategiaOrden<T> e)  { this.estrategia = e; }
        List<T> ordenar(List<T> lista)            { return estrategia.ordenar(lista); }
    }

    public static void main(String[] args) {
        List<String> palabras = List.of("banana", "kiwi", "manzana", "uva", "pera");

        Ordenador<String> ord = new Ordenador<>(new OrdenarAscendente<>());
        System.out.println("Ascendente:  " + ord.ordenar(palabras));

        ord.setEstrategia(new OrdenarDescendente<>());
        System.out.println("Descendente: " + ord.ordenar(palabras));

        ord.setEstrategia(new OrdenarPorLongitud());
        System.out.println("Longitud:    " + ord.ordenar(palabras));
    }
}
