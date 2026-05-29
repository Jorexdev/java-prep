import java.util.List;
import java.util.function.BiFunction;

// Zip: combinar dos listas elemento a elemento
public class Ejercicio5 {

    // Implementación genérica de zip
    static <A, B, C> List<C> zip(List<A> listA, List<B> listB, BiFunction<A, B, C> combiner) {
        int size = Math.min(listA.size(), listB.size());
        List<C> result = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(combiner.apply(listA.get(i), listB.get(i)));
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("=== Zip: nombres + puntuaciones ===\n");

        List<String> nombres     = List.of("Ana", "Bob", "Carlos", "Diana");
        List<Integer> puntuaciones = List.of(85, 92, 78, 95);

        System.out.println("Lista A (nombres):      " + nombres);
        System.out.println("Lista B (puntuaciones): " + puntuaciones);
        System.out.println();

        List<String> resultado = zip(nombres, puntuaciones, (n, p) -> n + " → " + p);
        System.out.println("Resultado del zip:");
        resultado.forEach(r -> System.out.println("  " + r));

        System.out.println();

        // Caso con listas de distinta longitud: el zip para en el más corto
        System.out.println("=== Zip con listas de distinta longitud ===\n");
        List<String> corta  = List.of("X", "Y");
        List<Integer> larga = List.of(1, 2, 3, 4, 5);

        System.out.println("Lista corta: " + corta + " (size=" + corta.size() + ")");
        System.out.println("Lista larga: " + larga + " (size=" + larga.size() + ")");

        List<String> zipCorto = zip(corta, larga, (a, b) -> a + "=" + b);
        System.out.println("Zip result (para en el más corto): " + zipCorto);

        System.out.println();
        System.out.println("=== Zip como operador reactivo ===");
        System.out.println("En Project Reactor: Flux.zip(fluxA, fluxB, combiner)");
        System.out.println("Combina elemento a elemento, respetando backpressure de ambos publishers.");
        System.out.println("Si un publisher emite más rápido, espera al más lento.");
    }
}
