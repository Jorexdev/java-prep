import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ejercicio7 {
    public static void main(String[] args) {
        List<Long> fibonacci = Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
            .limit(10)
            .map(f -> f[0])
            .collect(Collectors.toList());
        System.out.println("Fibonacci(10): " + fibonacci);

        Random rnd = new Random(42);
        List<Integer> aleatorios = Stream.generate(() -> rnd.nextInt(100) + 1)
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
        System.out.println("Aleatorios distintos: " + aleatorios);
    }
}
