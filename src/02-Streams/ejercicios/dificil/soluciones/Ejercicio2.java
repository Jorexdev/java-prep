import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Ejercicio2 {
    public static void main(String[] args) {
        List<Integer> millón = IntStream.rangeClosed(1, 1_000_000).boxed().collect(Collectors.toList());

        long t0 = System.nanoTime();
        long sumaSecuencial = millón.stream().mapToLong(Integer::longValue).sum();
        long t1 = System.nanoTime();

        long sumaParalela = millón.parallelStream().mapToLong(Integer::longValue).sum();
        long t2 = System.nanoTime();

        long sumaIntStream = IntStream.rangeClosed(1, 1_000_000).asLongStream().sum();
        long t3 = System.nanoTime();

        System.out.println("Suma secuencial:  " + sumaSecuencial + " → " + (t1 - t0) / 1_000_000 + "ms");
        System.out.println("Suma paralela:    " + sumaParalela   + " → " + (t2 - t1) / 1_000_000 + "ms");
        System.out.println("IntStream.sum():  " + sumaIntStream  + " → " + (t3 - t2) / 1_000_000 + "ms");
        System.out.println("Nota: parallelStream puede ser más lento para listas pequeñas por overhead de fork/join");
    }
}
