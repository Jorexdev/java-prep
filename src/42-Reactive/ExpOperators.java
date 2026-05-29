import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

// Operadores reactivos encadenables: map, filter, flatMap, zip, merge, take, skip
// Implementados como clases genéricas sin dependencias externas
public class ExpOperators {

    // ======================= PIPELINE =======================
    // Pipeline encadenable sobre una lista de elementos
    static class Pipeline<T> {
        private final List<T> items;

        Pipeline(List<T> items) {
            this.items = new ArrayList<>(items);
        }

        @SafeVarargs
        static <T> Pipeline<T> of(T... values) {
            return new Pipeline<>(Arrays.asList(values));
        }

        // map: transforma cada elemento 1:1
        <R> Pipeline<R> map(Function<T, R> fn) {
            List<R> result = new ArrayList<>();
            for (T item : items) result.add(fn.apply(item));
            return new Pipeline<>(result);
        }

        // filter: descarta elementos que no cumplen el predicado
        Pipeline<T> filter(Predicate<T> pred) {
            List<T> result = new ArrayList<>();
            for (T item : items) {
                if (pred.test(item)) result.add(item);
            }
            return new Pipeline<>(result);
        }

        // flatMap: cada elemento produce un sub-pipeline, todos se aplanan
        <R> Pipeline<R> flatMap(Function<T, Pipeline<R>> fn) {
            List<R> result = new ArrayList<>();
            for (T item : items) {
                result.addAll(fn.apply(item).items);
            }
            return new Pipeline<>(result);
        }

        // take: tomar los primeros N elementos
        Pipeline<T> take(int n) {
            return new Pipeline<>(items.subList(0, Math.min(n, items.size())));
        }

        // skip: saltar los primeros N elementos
        Pipeline<T> skip(int n) {
            return new Pipeline<>(items.subList(Math.min(n, items.size()), items.size()));
        }

        // distinct: eliminar duplicados (orden de primera aparición)
        Pipeline<T> distinct() {
            List<T> result = new ArrayList<>();
            for (T item : items) {
                if (!result.contains(item)) result.add(item);
            }
            return new Pipeline<>(result);
        }

        // zip: combinar dos pipelines elemento a elemento con una función
        <U, R> Pipeline<R> zip(Pipeline<U> other, BiFunction<T, U, R> combiner) {
            int size = Math.min(items.size(), other.items.size());
            List<R> result = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                result.add(combiner.apply(items.get(i), other.items.get(i)));
            }
            return new Pipeline<>(result);
        }

        // merge: concatenar dos pipelines (BUFFER merge, no interleaved)
        Pipeline<T> merge(Pipeline<T> other) {
            List<T> result = new ArrayList<>(items);
            result.addAll(other.items);
            return new Pipeline<>(result);
        }

        // reduce: acumular elementos en un valor único
        <R> R reduce(R identity, BiFunction<R, T, R> accumulator) {
            R result = identity;
            for (T item : items) result = accumulator.apply(result, item);
            return result;
        }

        // collect: materializar a lista
        List<T> collect() {
            return new ArrayList<>(items);
        }

        // forEach: efecto secundario por cada elemento
        void forEach(java.util.function.Consumer<T> action) {
            items.forEach(action);
        }

        @Override
        public String toString() {
            return items.toString();
        }
    }

    public static void main(String[] args) {

        System.out.println("=== map ===");
        // Transformación 1:1
        Pipeline<String> mapped = Pipeline.of("java", "reactive", "streams")
            .map(String::toUpperCase);
        System.out.println("Input:  [java, reactive, streams]");
        System.out.println("Output: " + mapped);

        System.out.println();
        System.out.println("=== filter ===");
        Pipeline<Integer> filtered = Pipeline.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .filter(n -> n % 2 == 0);
        System.out.println("Input:  [1..10]");
        System.out.println("Output (solo pares): " + filtered);

        System.out.println();
        System.out.println("=== flatMap ===");
        // Cada número genera su tabla de multiplicar hasta 3
        Pipeline<String> flatted = Pipeline.of(2, 3)
            .flatMap(n -> Pipeline.of(n + "x1=" + (n*1), n + "x2=" + (n*2), n + "x3=" + (n*3)));
        System.out.println("Input:  [2, 3]  (cada n → tabla hasta 3)");
        System.out.println("Output: " + flatted);

        System.out.println();
        System.out.println("=== take y skip ===");
        Pipeline<Integer> nums = Pipeline.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("take(3): " + nums.take(3));
        System.out.println("skip(7): " + nums.skip(7));
        System.out.println("skip(3).take(4): " + nums.skip(3).take(4)); // ventana deslizante

        System.out.println();
        System.out.println("=== zip ===");
        // Combinar dos pipelines elemento a elemento
        Pipeline<String> nombres = Pipeline.of("Ana", "Bob", "Carlos");
        Pipeline<Integer> edades  = Pipeline.of(30, 25, 35);
        Pipeline<String> zipped = nombres.zip(edades, (n, e) -> n + "(" + e + ")");
        System.out.println("nombres: " + nombres);
        System.out.println("edades:  " + edades);
        System.out.println("zip:     " + zipped);

        System.out.println();
        System.out.println("=== merge ===");
        Pipeline<Integer> p1 = Pipeline.of(1, 3, 5);
        Pipeline<Integer> p2 = Pipeline.of(2, 4, 6);
        System.out.println("p1:    " + p1);
        System.out.println("p2:    " + p2);
        System.out.println("merge: " + p1.merge(p2));

        System.out.println();
        System.out.println("=== distinct ===");
        Pipeline<String> conDuplicados = Pipeline.of("java", "java", "python", "java", "go", "python");
        System.out.println("Input:    " + conDuplicados);
        System.out.println("distinct: " + conDuplicados.distinct());

        System.out.println();
        System.out.println("=== reduce ===");
        int suma = Pipeline.of(1, 2, 3, 4, 5).reduce(0, Integer::sum);
        String concat = Pipeline.of("a", "b", "c", "d").reduce("", (acc, s) -> acc + s);
        System.out.println("sum([1,2,3,4,5]): " + suma);
        System.out.println("concat([a,b,c,d]): " + concat);

        System.out.println();
        System.out.println("=== Pipeline encadenado complejo ===");
        // Palabras de una frase → solo >3 letras → únicas → mayúsculas → ordenadas → primeras 5
        String frase = "el rapido zorro marron salta sobre el perro perezoso";
        Pipeline<String> resultado = Pipeline.of(frase.split(" "))
            .filter(w -> w.length() > 3)
            .distinct()
            .map(String::toUpperCase)
            .skip(0)
            .take(5);
        System.out.println("Frase:     \"" + frase + "\"");
        System.out.println("Resultado: " + resultado);
    }
}
