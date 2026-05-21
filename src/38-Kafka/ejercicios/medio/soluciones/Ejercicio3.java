import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Ejercicio3 {

    static class Topic<T> {
        final String name;
        final List<T> records = new ArrayList<>();

        Topic(String name) {
            this.name = name;
        }

        void add(T record) {
            records.add(record);
        }
    }

    static class StreamPipeline<T, R> {
        private final Topic<T> source;
        private Predicate<T> filterFn;
        private Function<T, R> mapFn;

        StreamPipeline(Topic<T> source) {
            this.source = source;
        }

        StreamPipeline<T, R> filter(Predicate<T> predicate) {
            this.filterFn = predicate;
            return this;
        }

        StreamPipeline<T, R> map(Function<T, R> fn) {
            this.mapFn = fn;
            return this;
        }

        void to(Consumer<R> sink) {
            for (T item : source.records) {
                if (filterFn != null && !filterFn.test(item)) continue;
                R mapped = mapFn != null ? mapFn.apply(item) : (R) item;
                sink.accept(mapped);
            }
        }
    }

    static class Venta {
        final String id;
        final double precio;

        Venta(String id, double precio) {
            this.id = id;
            this.precio = precio;
        }

        @Override
        public String toString() {
            return "Venta{id='" + id + "', precio=" + precio + "}";
        }
    }

    public static void main(String[] args) {
        Topic<String> ventasRaw = new Topic<>("ventas-raw");
        String[] rawData = {"P1:150.0", "P2:80.0", "P3:200.0", "P4:50.0", "P5:120.0", "P6:30.0", "P7:175.0", "P8:99.0"};
        for (String raw : rawData) {
            ventasRaw.add(raw);
        }

        Topic<Venta> ventasValidas = new Topic<>("ventas-validas");

        new StreamPipeline<String, Venta>(ventasRaw)
                .filter(raw -> Double.parseDouble(raw.split(":")[1]) > 100)
                .map(raw -> {
                    String[] parts = raw.split(":");
                    return new Venta(parts[0], Double.parseDouble(parts[1]));
                })
                .to(venta -> {
                    ventasValidas.add(venta);
                    System.out.println("[PIPELINE] " + venta);
                });

        System.out.println("\n[TOPIC '" + ventasValidas.name + "'] " + ventasValidas.records.size() + " registros:");
        for (Venta v : ventasValidas.records) {
            System.out.println("  " + v);
        }
    }
}
