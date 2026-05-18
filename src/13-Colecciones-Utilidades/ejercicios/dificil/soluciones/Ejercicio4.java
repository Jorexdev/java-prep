import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Ejercicio4 {

    static class IteradorFibonacci implements Iterator<Long> {
        private long a = 0, b = 1;

        @Override public boolean hasNext() { return true; }

        @Override public Long next() {
            long resultado = a;
            long temp = a + b;
            a = b;
            b = temp;
            return resultado;
        }
    }

    public static void main(String[] args) {
        var spliterator = Spliterators.spliteratorUnknownSize(
            new IteradorFibonacci(),
            java.util.Spliterator.ORDERED
        );

        var primeros15 = StreamSupport.stream(spliterator, false)
            .limit(15)
            .collect(Collectors.toList());

        System.out.println("Fibonacci(15): " + primeros15);

        var spliterator2 = Spliterators.spliteratorUnknownSize(new IteradorFibonacci(), 0);
        long suma = StreamSupport.stream(spliterator2, false)
            .limit(10)
            .mapToLong(Long::longValue)
            .sum();
        System.out.println("Suma primeros 10: " + suma);
    }
}
