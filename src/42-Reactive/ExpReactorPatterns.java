import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// Simulación de Mono<T> y Flux<T> con generics + callbacks
// No usa Project Reactor — demuestra el patrón con Java puro
public class ExpReactorPatterns {

    // ======================= MONO =======================
    // Mono: 0 o 1 elemento de forma asíncrona
    static class Mono<T> {
        private final T value;
        private final Throwable error;

        private Mono(T value, Throwable error) {
            this.value = value;
            this.error = error;
        }

        // Constructores de fábrica
        public static <T> Mono<T> just(T value) {
            return new Mono<>(value, null);
        }

        public static <T> Mono<T> empty() {
            return new Mono<>(null, null);
        }

        public static <T> Mono<T> error(Throwable t) {
            return new Mono<>(null, t);
        }

        // Transformación: map aplica función al valor si existe
        public <R> Mono<R> map(Function<T, R> fn) {
            if (error != null) return Mono.error(error);
            if (value == null) return Mono.empty();
            try {
                return Mono.just(fn.apply(value));
            } catch (Exception e) {
                return Mono.error(e);
            }
        }

        // flatMap: función que devuelve otro Mono
        public <R> Mono<R> flatMap(Function<T, Mono<R>> fn) {
            if (error != null) return Mono.error(error);
            if (value == null) return Mono.empty();
            try {
                return fn.apply(value);
            } catch (Exception e) {
                return Mono.error(e);
            }
        }

        // Fallback de error
        public Mono<T> onErrorReturn(T fallback) {
            if (error != null) return Mono.just(fallback);
            return this;
        }

        // Suscribirse con callbacks
        public void subscribe(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete) {
            if (error != null) {
                onError.accept(error);
                return;
            }
            if (value != null) onNext.accept(value);
            onComplete.run();
        }

        // Suscripción simplificada
        public void subscribe(Consumer<T> onNext) {
            subscribe(onNext, e -> System.err.println("Error: " + e.getMessage()), () -> {});
        }

        @Override
        public String toString() {
            if (error != null) return "Mono[error=" + error.getMessage() + "]";
            return "Mono[" + value + "]";
        }
    }

    // ======================= FLUX =======================
    // Flux: 0 a N elementos
    static class Flux<T> {
        private final List<T> items;
        private final Throwable error;

        private Flux(List<T> items, Throwable error) {
            this.items = items != null ? items : List.of();
            this.error = error;
        }

        public static <T> Flux<T> just(@SuppressWarnings("unchecked") T... values) {
            return new Flux<>(List.of(values), null);
        }

        public static <T> Flux<T> fromList(List<T> list) {
            return new Flux<>(new ArrayList<>(list), null);
        }

        public static <T> Flux<T> error(Throwable t) {
            return new Flux<>(null, t);
        }

        public Flux<T> filter(Predicate<T> pred) {
            if (error != null) return Flux.error(error);
            List<T> result = new ArrayList<>();
            for (T item : items) {
                if (pred.test(item)) result.add(item);
            }
            return new Flux<>(result, null);
        }

        public <R> Flux<R> map(Function<T, R> fn) {
            if (error != null) return Flux.error(error);
            List<R> result = new ArrayList<>();
            for (T item : items) result.add(fn.apply(item));
            return new Flux<>(result, null);
        }

        public <R> Flux<R> flatMap(Function<T, Flux<R>> fn) {
            if (error != null) return Flux.error(error);
            List<R> result = new ArrayList<>();
            for (T item : items) {
                Flux<R> inner = fn.apply(item);
                result.addAll(inner.items);
            }
            return new Flux<>(result, null);
        }

        public Flux<T> take(int n) {
            if (error != null) return Flux.error(error);
            return new Flux<>(items.subList(0, Math.min(n, items.size())), null);
        }

        public Flux<T> skip(int n) {
            if (error != null) return Flux.error(error);
            return new Flux<>(items.subList(Math.min(n, items.size()), items.size()), null);
        }

        public void subscribe(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete) {
            if (error != null) { onError.accept(error); return; }
            for (T item : items) onNext.accept(item);
            onComplete.run();
        }

        public void subscribe(Consumer<T> onNext) {
            subscribe(onNext, e -> System.err.println("Error: " + e.getMessage()), () -> {});
        }

        public int count() { return items.size(); }

        @Override
        public String toString() {
            if (error != null) return "Flux[error=" + error.getMessage() + "]";
            return "Flux" + items;
        }
    }

    // ======================= COLD vs HOT =======================
    // Cold publisher: cada subscriber recibe la secuencia completa desde el inicio
    // Hot publisher: los subscribers solo reciben eventos futuros (como una radio)
    static class HotPublisher<T> {
        private final List<Consumer<T>> subscribers = new ArrayList<>();

        public void subscribe(Consumer<T> subscriber) {
            subscribers.add(subscriber);
            System.out.println("  Nuevo subscriber registrado. Total: " + subscribers.size());
        }

        // Emite a todos los subscribers actuales (solo los registrados en este momento)
        public void emit(T item) {
            System.out.println("  Emitiendo: " + item + " -> " + subscribers.size() + " subscribers");
            subscribers.forEach(s -> s.accept(item));
        }
    }

    public static void main(String[] args) {

        System.out.println("=== Mono: 0 o 1 elemento ===\n");

        // Mono con valor
        Mono<String> mono = Mono.just("hola-mundo");
        mono.map(String::toUpperCase)
            .subscribe(
                v  -> System.out.println("onNext: " + v),
                e  -> System.out.println("onError: " + e.getMessage()),
                () -> System.out.println("onComplete")
            );

        System.out.println();

        // Mono vacío
        Mono<String> empty = Mono.empty();
        empty.subscribe(
            v  -> System.out.println("onNext: " + v),
            e  -> System.out.println("onError: " + e.getMessage()),
            () -> System.out.println("onComplete (vacío)")
        );

        System.out.println();

        // Mono con error
        Mono<String> conError = Mono.<String>error(new RuntimeException("fallo simulado"))
            .onErrorReturn("valor-fallback");
        conError.subscribe(
            v  -> System.out.println("onNext (fallback): " + v),
            e  -> System.out.println("onError: " + e.getMessage()),
            () -> System.out.println("onComplete")
        );

        System.out.println();
        System.out.println("=== Flux: 0 a N elementos ===\n");

        // Pipeline Flux: filter → map → take
        Flux<Integer> numeros = Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        numeros.filter(n -> n % 2 == 0)   // solo pares
               .map(n -> n * 10)           // multiplicar por 10
               .take(3)                    // solo los 3 primeros
               .subscribe(
                   v  -> System.out.println("onNext: " + v),
                   e  -> System.out.println("onError: " + e.getMessage()),
                   () -> System.out.println("onComplete")
               );

        System.out.println();

        // flatMap: cada número genera un Flux de ese número repetido N veces
        Flux<Integer> expandido = Flux.just(1, 2, 3)
            .flatMap(n -> Flux.just(n, n, n)); // cada n -> [n, n, n]
        System.out.print("flatMap result: ");
        expandido.subscribe(v -> System.out.print(v + " "));
        System.out.println();

        System.out.println();
        System.out.println("=== Cold vs Hot Publisher ===\n");

        // Cold: cada subscribe reproduce la secuencia completa desde el inicio
        System.out.println("Cold publisher (Flux.fromList):");
        List<String> fuente = List.of("A", "B", "C");
        Flux<String> cold = Flux.fromList(fuente);
        System.out.print("  Sub-1: "); cold.subscribe(v -> System.out.print(v + " ")); System.out.println();
        System.out.print("  Sub-2: "); cold.subscribe(v -> System.out.print(v + " ")); System.out.println();
        System.out.println("  -> Ambos reciben A, B, C desde el principio");

        System.out.println();

        // Hot: los subscribers solo reciben lo que se emite después de suscribirse
        System.out.println("Hot publisher (eventos en tiempo real):");
        HotPublisher<String> hot = new HotPublisher<>();
        hot.emit("evento-0 (nadie escucha aún)");

        hot.subscribe(e -> System.out.println("  [Sub-1] " + e));
        hot.emit("evento-1");

        hot.subscribe(e -> System.out.println("  [Sub-2] " + e));
        hot.emit("evento-2");

        System.out.println("  -> Sub-1 recibió evento-1 y evento-2; Sub-2 solo evento-2");
    }
}
