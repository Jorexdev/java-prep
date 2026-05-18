import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Ejercicio4 {

    static <T, R> Function<T, R> memoizar(Function<T, R> fn) {
        Map<T, R> cache = new HashMap<>();
        return input -> cache.computeIfAbsent(input, fn);
    }

    public static void main(String[] args) {

        Function<Integer, Integer> cuadrado = n -> {
            System.out.println("  Calculando cuadrado de " + n + "...");
            return n * n;
        };

        Function<Integer, Integer> cuadradoMemo = memoizar(cuadrado);

        System.out.println("Primera llamada con 5: " + cuadradoMemo.apply(5));
        System.out.println("Segunda llamada con 5: " + cuadradoMemo.apply(5)); // no imprime "Calculando..."
        System.out.println("Primera llamada con 7: " + cuadradoMemo.apply(7));
        System.out.println("Segunda llamada con 7: " + cuadradoMemo.apply(7)); // no imprime "Calculando..."
        System.out.println("Primera llamada con 5 de nuevo: " + cuadradoMemo.apply(5)); // caché
    }
}
