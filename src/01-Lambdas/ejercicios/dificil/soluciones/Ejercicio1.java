import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Ejercicio1 {

    static void imprimirMensaje(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {

        // 1. Consumer — referencia a método de instancia de objeto concreto
        Consumer<String> lambda1  = s -> System.out.println(s);
        Consumer<String> metodo1  = System.out::println;

        // 2. Function<String,String> — referencia a método de instancia de tipo arbitrario
        Function<String, String> lambda2 = s -> s.toUpperCase();
        Function<String, String> metodo2 = String::toUpperCase;

        // 3. Function<String,Integer> — referencia a método estático
        Function<String, Integer> lambda3 = s -> Integer.parseInt(s);
        Function<String, Integer> metodo3 = Integer::parseInt;

        // 4. Supplier<List<String>> — referencia a constructor
        Supplier<List<String>> lambda4 = () -> new ArrayList<>();
        Supplier<List<String>> metodo4 = ArrayList::new;

        System.out.println("=== Consumer ===");
        lambda1.accept("hola lambda");
        metodo1.accept("hola método");

        System.out.println("\n=== Function toUpperCase ===");
        System.out.println(lambda2.apply("java"));
        System.out.println(metodo2.apply("java"));

        System.out.println("\n=== parseInt ===");
        System.out.println(lambda3.apply("42"));
        System.out.println(metodo3.apply("42"));

        System.out.println("\n=== Supplier ArrayList ===");
        System.out.println(lambda4.get().getClass().getSimpleName());
        System.out.println(metodo4.get().getClass().getSimpleName());
    }
}
