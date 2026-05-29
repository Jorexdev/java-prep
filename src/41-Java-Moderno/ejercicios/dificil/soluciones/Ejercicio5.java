import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Ejercicio5 {

    record Empleado(String nombre, String dept, double salario) {}

    // =================== AST DEL DSL ===================
    // Query<T, R>: T es el tipo de entrada, R el tipo de salida
    sealed interface Query<T, R> permits Source, FilterOp, MapOp, ReduceOp, GroupByOp, LimitOp, SortOp {}

    // Nodo raíz: fuente de datos
    record Source<T>(List<T> data) implements Query<T, T> {}

    // Operaciones que mantienen el tipo
    record FilterOp<T, R>(Query<T, R> source, Predicate<R> predicate) implements Query<T, R> {}
    record SortOp<T, R>(Query<T, R> source, Comparator<R> comparator) implements Query<T, R> {}
    record LimitOp<T, R>(Query<T, R> source, int limit) implements Query<T, R> {}

    // Map: cambia el tipo de salida
    record MapOp<T, R, U>(Query<T, R> source, Function<R, U> mapper) implements Query<T, U> {}

    // Reduce: produce un solo valor
    record ReduceOp<T, R, U>(Query<T, R> source, U identity, BinaryOperator<U> accumulator,
                              Function<R, U> mapper) implements Query<T, U> {}

    // GroupBy: produce Map<K, List<R>>
    record GroupByOp<T, R, K>(Query<T, R> source, Function<R, K> classifier) implements Query<T, Map<K, List<R>>> {}

    // =================== EXECUTOR ===================
    @SuppressWarnings("unchecked")
    static <T, R> List<R> execute(Query<T, R> q) {
        Stream<R> stream = toStream(q);
        return stream.collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    static <T, R> Stream<R> toStream(Query<T, R> q) {
        return switch (q) {
            case Source<T> s       -> (Stream<R>) s.data().stream();
            case FilterOp<T, R> f  -> toStream(f.source()).filter(f.predicate());
            case SortOp<T, R> s    -> toStream(s.source()).sorted(s.comparator());
            case LimitOp<T, R> l   -> toStream(l.source()).limit(l.limit());
            // MapOp: el compilador no puede inferir U aquí, pero en runtime es correcto
            case MapOp<?, ?, ?> m  -> {
                var src = (Query<T, Object>) m.source();
                var fn = (Function<Object, R>) m.mapper();
                yield toStream(src).map(fn);
            }
            // GroupBy y Reduce no producen Stream<R> directamente: se manejan por separado
            case ReduceOp<?, ?, ?> r ->
                throw new UnsupportedOperationException("ReduceOp usa executeReduce()");
            case GroupByOp<?, ?, ?> g ->
                throw new UnsupportedOperationException("GroupByOp usa executeGroupBy()");
        };
    }

    @SuppressWarnings("unchecked")
    static <T, R, U> U executeReduce(ReduceOp<T, R, U> op) {
        return toStream(op.source())
            .map(op.mapper())
            .reduce(op.identity(), op.accumulator());
    }

    @SuppressWarnings("unchecked")
    static <T, R, K> Map<K, List<R>> executeGroupBy(GroupByOp<T, R, K> op) {
        return toStream(op.source())
            .collect(Collectors.groupingBy(op.classifier()));
    }

    // =================== FLUENT BUILDER ===================
    // Builder envuelve una Query para permitir la API fluida
    static class QueryBuilder<T, R> {
        private final Query<T, R> query;

        QueryBuilder(Query<T, R> q) { this.query = q; }

        QueryBuilder<T, R> filter(Predicate<R> pred) {
            return new QueryBuilder<>(new FilterOp<>(query, pred));
        }

        QueryBuilder<T, R> sortBy(Comparator<R> cmp) {
            return new QueryBuilder<>(new SortOp<>(query, cmp));
        }

        QueryBuilder<T, R> limit(int n) {
            return new QueryBuilder<>(new LimitOp<>(query, n));
        }

        <U> QueryBuilder<T, U> map(Function<R, U> fn) {
            return new QueryBuilder<>(new MapOp<>(query, fn));
        }

        List<R> toList() {
            return execute(query);
        }

        <U> U reduce(U identity, Function<R, U> mapper, BinaryOperator<U> acc) {
            return executeReduce(new ReduceOp<>(query, identity, acc, mapper));
        }

        <K> Map<K, List<R>> groupBy(Function<R, K> classifier) {
            return executeGroupBy(new GroupByOp<>(query, classifier));
        }
    }

    static <T> QueryBuilder<T, T> from(List<T> data) {
        return new QueryBuilder<>(new Source<>(data));
    }

    public static void main(String[] args) {
        List<Empleado> empleados = List.of(
            new Empleado("Ana",     "Engineering", 75000),
            new Empleado("Bob",     "Engineering", 85000),
            new Empleado("Carlos",  "Engineering", 62000),
            new Empleado("Diana",   "Marketing",   58000),
            new Empleado("Eva",     "Marketing",   67000),
            new Empleado("Frank",   "Marketing",   54000),
            new Empleado("Grace",   "HR",          55000),
            new Empleado("Hector",  "HR",          60000),
            new Empleado("Isabel",  "Engineering", 92000),
            new Empleado("Javier",  "Marketing",   71000)
        );

        System.out.println("=== DSL de Consultas con Sealed Classes ===\n");

        // 1. Top 3 salarios por departamento
        System.out.println("--- Top 3 salarios por departamento ---");
        Map<String, List<Empleado>> porDept = from(empleados)
            .sortBy(Comparator.comparingDouble(Empleado::salario).reversed())
            .groupBy(Empleado::dept);

        porDept.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                System.out.println("  " + entry.getKey() + ":");
                entry.getValue().stream().limit(3).forEach(e ->
                    System.out.printf("    %-10s %.0f€%n", e.nombre(), e.salario())
                );
            });

        // 2. Nombres con salario > media por departamento
        System.out.println("\n--- Nombres con salario > media del dept ---");
        Map<String, Double> mediaPorDept = new HashMap<>();
        from(empleados).groupBy(Empleado::dept).forEach((dept, lista) -> {
            double media = lista.stream().mapToDouble(Empleado::salario).average().orElse(0);
            mediaPorDept.put(dept, media);
        });

        List<String> sobreMedia = from(empleados)
            .filter(e -> e.salario() > mediaPorDept.get(e.dept()))
            .sortBy(Comparator.comparing(Empleado::nombre))
            .map(e -> e.nombre() + " (" + e.dept() + ", " + String.format("%.0f", e.salario()) + "€)")
            .toList();
        sobreMedia.forEach(s -> System.out.println("  " + s));

        // 3. Numero de empleados por departamento
        System.out.println("\n--- Empleados por departamento ---");
        Map<String, List<Empleado>> conteo = from(empleados).groupBy(Empleado::dept);
        conteo.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %-15s %d empleados%n", e.getKey(), e.getValue().size()));

        // 4. Salario total de Engineering
        System.out.println("\n--- Salario total Engineering ---");
        double totalEng = from(empleados)
            .filter(e -> e.dept().equals("Engineering"))
            .reduce(0.0, Empleado::salario, Double::sum);
        System.out.printf("  Total: %.0f€%n", totalEng);

        // 5. Pipeline simple: filter + sort + limit + map
        System.out.println("\n--- Top 3 nombres con salario > 60000 ---");
        List<String> top3 = from(empleados)
            .filter(e -> e.salario() > 60000)
            .sortBy(Comparator.comparingDouble(Empleado::salario).reversed())
            .limit(3)
            .map(Empleado::nombre)
            .toList();
        top3.forEach(n -> System.out.println("  " + n));
    }
}
