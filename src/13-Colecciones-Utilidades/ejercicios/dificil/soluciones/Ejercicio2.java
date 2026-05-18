import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class Ejercicio2 {

    static class IteradorFiltro<T> implements Iterator<T> {
        private final Iterator<T> base;
        private final Predicate<T> predicado;
        private T siguiente;
        private boolean tieneSiguiente;

        IteradorFiltro(Iterator<T> base, Predicate<T> predicado) {
            this.base = base;
            this.predicado = predicado;
            avanzar();
        }

        private void avanzar() {
            tieneSiguiente = false;
            while (base.hasNext()) {
                T candidato = base.next();
                if (predicado.test(candidato)) {
                    siguiente = candidato;
                    tieneSiguiente = true;
                    return;
                }
            }
        }

        @Override public boolean hasNext() { return tieneSiguiente; }

        @Override public T next() {
            if (!tieneSiguiente) throw new NoSuchElementException();
            T resultado = siguiente;
            avanzar();
            return resultado;
        }
    }

    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Iterator<Integer> pares = new IteradorFiltro<>(nums.iterator(), n -> n % 2 == 0);
        System.out.print("Pares: ");
        while (pares.hasNext()) System.out.print(pares.next() + " ");
        System.out.println();

        Iterator<String> largos = new IteradorFiltro<>(
            List.of("hi", "hello", "hola", "hey", "howdy").iterator(),
            s -> s.length() > 3
        );
        System.out.print("Longitud >3: ");
        while (largos.hasNext()) System.out.print(largos.next() + " ");
        System.out.println();
    }
}
