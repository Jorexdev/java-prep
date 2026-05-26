import java.util.*;
import java.util.stream.*;

public class ExpStreamAdvanced {

    public static void main(String[] args) {

        // ======================================
        // 1. takeWhile y dropWhile (Java 9+)
        //    Solo tienen sentido sobre streams ORDENADOS; en desordenados el resultado es impredecible
        // ======================================

        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // takeWhile: toma elementos MIENTRAS se cumple la condición; para en el primero que falla
        List<Integer> menoresDe5 = nums.stream()
                .takeWhile(n -> n < 5)
                .toList();
        System.out.println("takeWhile (< 5): " + menoresDe5);  // [1, 2, 3, 4]

        // dropWhile: descarta MIENTRAS se cumple la condición; emite el resto desde el primero que falla
        List<Integer> desde5 = nums.stream()
                .dropWhile(n -> n < 5)
                .toList();
        System.out.println("dropWhile (< 5): " + desde5);      // [5, 6, 7, 8, 9, 10]

        // ======================================
        // 2. Stream.iterate con predicado (Java 9+)
        // ======================================

        // Java 8: iterate(seed, f) — infinito, necesita limit()
        // Java 9: iterate(seed, hasNext, f) — se detiene solo, como un for-loop funcional
        List<Integer> potencias = Stream.iterate(1, n -> n <= 1000, n -> n * 2)
                .toList();
        System.out.println("iterate con predicado: " + potencias);

        // ======================================
        // 3. Stream.ofNullable (Java 9+)
        // ======================================

        // Evita NPE: devuelve stream de 1 elemento si no es null, stream vacío si es null
        String valorNulo = null;
        long conteoNulo = Stream.ofNullable(valorNulo).count();
        long conteoReal = Stream.ofNullable("Hola").count();
        System.out.println("ofNullable(null).count() = " + conteoNulo);  // 0
        System.out.println("ofNullable(str).count()  = " + conteoReal);  // 1

        // Útil para aplanar listas que pueden ser null (combinado con flatMap)
        List<String> posibleNula = null;
        List<String> resultado = Stream.ofNullable(posibleNula)
                .flatMap(Collection::stream)
                .toList();
        System.out.println("flatMap sobre lista null: " + resultado);  // []

        // ======================================
        // 4. peek — solo para depurar, no para lógica real
        //    peek es intermedio: no fuerza evaluación por sí solo
        // ======================================

        System.out.println("\n--- peek en pipeline ---");
        List<String> nombres = List.of("Ana", "Luis", "Marta", "Alba");
        nombres.stream()
                .peek(n -> System.out.println("  [antes filter] " + n))
                .filter(n -> n.length() > 3)
                .peek(n -> System.out.println("  [después filter] " + n))
                .map(String::toUpperCase)
                .toList(); // sin operación terminal, peek nunca se ejecutaría

        // ======================================
        // 5. findFirst vs findAny en paralelo
        // ======================================

        Optional<Integer> primeroPar = nums.parallelStream()
                .filter(n -> n % 2 == 0)
                .findFirst();       // garantiza el primero según el orden del stream
        System.out.println("\nfindFirst par: " + primeroPar.orElse(-1));  // siempre 2

        // findAny puede devolver cualquier elemento que pase el filtro — más eficiente en paralelo
        Optional<Integer> cualquierPar = nums.parallelStream()
                .filter(n -> n % 2 == 0)
                .findAny();         // puede ser 2, 4, 6... — no determinista
        System.out.println("findAny par:   " + cualquierPar.orElse(-1) + " (cualquier par)");

        // ======================================
        // 6. reduce — con identidad, acumulador y combinador
        // ======================================

        // Forma 1: con identidad + acumulador (siempre devuelve T)
        int suma = IntStream.rangeClosed(1, 5)
                .reduce(0, Integer::sum);  // 0+1+2+3+4+5
        System.out.println("\nreduce suma: " + suma);

        // Forma 3: identidad + acumulador + combinador (necesario en paralelo cuando T ≠ U)
        // Suma las longitudes de todas las palabras (T=int, U=String → necesita combinador)
        int longitudTotal = Stream.of("hola", "mundo", "java")
                .reduce(0,
                        (acum, s) -> acum + s.length(),  // acumulador: combina int con String
                        Integer::sum);                    // combinador: fusiona dos ints parciales
        System.out.println("reduce con combinador: " + longitudTotal);  // 13

        // ======================================
        // 7. Stream.concat — fusionar dos streams
        // ======================================

        Stream<String> backend  = Stream.of("Spring", "Kafka");
        Stream<String> frontend = Stream.of("React", "Vue");
        List<String> todos = Stream.concat(backend, frontend).toList();
        System.out.println("Stream.concat: " + todos);
    }
}
