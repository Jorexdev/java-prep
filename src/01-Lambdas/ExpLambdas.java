import java.util.*;
import java.util.function.*;

public class ExpLambdas {

    public static void main(String[] args) {

        // Runnable como lambda: no recibe parámetros, ejecuta un bloque de código
        Runnable r = () -> System.out.println("Hola Lambda desde Runnable");
        r.run();

        // Function<T,R>: recibe un String y devuelve un Integer (su longitud)
        Function<String, Integer> contar = s -> s.length(); // Sustituible por String::length
        System.out.println("Longitud de 'Java': " + contar.apply("Java")); // 4

        // BiFunction<T,U,R>: recibe dos Integer y devuelve la operacion que pongamos en el lambda
        BiFunction<Integer, Integer, Integer> sumar = (a, b) -> a + b; // Sustituible por Integer::sum
        System.out.println("Suma 5+3 = " + sumar.apply(5, 3)); // 8

        // Consumer<T>: recibe un valor y lo consume (sin devolver nada)
        Consumer<String> imprimir = s -> System.out.println("Imprimiendo: " + s);
        imprimir.accept("Lambda con Consumer");


        // Supplier<T>: no recibe nada, devuelve un valor
        Supplier<Double> random = () -> Math.random(); // Sustituible por Math::random
        System.out.println("Número aleatorio: " + random.get());


        List<String> lista = new ArrayList<>(List.of("Java", "Spring", "Hibernate"));

        // forEach con lambda
        lista.forEach(elem -> System.out.println("Elemento: " + elem));

        // removeIf con Predicate<T>: elimina strings cortos (<6)
        lista.removeIf(s -> s.length() < 6);
        System.out.println("Tras removeIf (<6): " + lista);

        // sort con Comparator<T> implementado como lambda
        lista.sort((a, b) -> a.compareToIgnoreCase(b));// Sustituible por String::compareToIgnoreCase
        System.out.println("Ordenados: " + lista);

        // replaceAll con UnaryOperator<T>: transforma cada elemento
        lista.replaceAll(s -> s.toUpperCase()); // Sustituible por String::toUpperCase
        System.out.println("replaceAll -> " + lista);

        // ================== MÉTODOS DE REFERENCIA ==================

        List<String> otros = List.of("uno", "dos", "tres");

        // Atajo: System.out::println es equivalente a (s -> System.out.println(s))
        otros.forEach(System.out::println);
    }
}
