import java.util.Iterator;

public class Ejercicio4 {

    static class Rango implements Iterable<Integer> {
        private final int inicio;
        private final int fin;

        Rango(int inicio, int fin) { this.inicio = inicio; this.fin = fin; }

        @Override public Iterator<Integer> iterator() {
            return new Iterator<>() {
                private int actual = inicio;
                @Override public boolean hasNext() { return actual <= fin; }
                @Override public Integer next()    { return actual++; }
            };
        }
    }

    public static void main(String[] args) {
        for (int n : new Rango(1, 10)) {
            System.out.print(n + " ");
        }
        System.out.println();

        new Rango(5, 8).forEach(n -> System.out.print(n + " "));
        System.out.println();
    }
}
