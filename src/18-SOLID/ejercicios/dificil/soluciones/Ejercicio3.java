import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio3 {

    interface Legible<T> {
        T obtener(int indice);
        int tamaño();
    }

    interface Coleccion<T> extends Legible<T> {
        void agregar(T elemento);
    }

    static class ColeccionOrdenada<T extends Comparable<T>> implements Coleccion<T> {
        private final List<T> elementos = new ArrayList<>();

        @Override public void agregar(T e) {
            elementos.add(e);
            Collections.sort(elementos);
        }
        @Override public T obtener(int i) { return elementos.get(i); }
        @Override public int tamaño()     { return elementos.size(); }
        @Override public String toString() { return elementos.toString(); }
    }

    static class ColeccionInmutable<T> implements Legible<T> {
        private final List<T> elementos;
        ColeccionInmutable(List<T> elementos) { this.elementos = List.copyOf(elementos); }

        @Override public T obtener(int i) { return elementos.get(i); }
        @Override public int tamaño()     { return elementos.size(); }
        @Override public String toString() { return elementos.toString(); }
    }

    public static void main(String[] args) {
        ColeccionOrdenada<Integer> ordenada = new ColeccionOrdenada<>();
        ordenada.agregar(5); ordenada.agregar(2); ordenada.agregar(8); ordenada.agregar(1);
        System.out.println("Ordenada: " + ordenada);

        ColeccionInmutable<String> inmutable = new ColeccionInmutable<>(List.of("z", "a", "m"));
        System.out.println("Inmutable[1]: " + inmutable.obtener(1));
        System.out.println("Tamaño: " + inmutable.tamaño());

        // ColeccionInmutable implementa Legible (no Coleccion) para respetar LSP:
        // no tiene sentido que un contrato de escritura sea honrado por algo inmutable
        System.out.println("LSP respetado: ColeccionInmutable implementa Legible, no Coleccion");
    }
}
