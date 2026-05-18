import java.util.function.BiFunction;

public class Ejercicio4 {

    public static void main(String[] args) {

        BiFunction<String, Integer, String> repetir = (s, n) -> s.repeat(n);

        System.out.println(repetir.apply("ab", 3));   // ababab
        System.out.println(repetir.apply("java", 2)); // javajava
        System.out.println(repetir.apply("!", 5));    // !!!!!
    }
}
