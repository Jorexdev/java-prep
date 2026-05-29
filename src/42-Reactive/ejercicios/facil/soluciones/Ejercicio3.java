import java.util.function.Consumer;
import java.util.function.Function;

// Mono<T> simulado: 0 o 1 elemento — éxito, vacío, error, onErrorReturn
public class Ejercicio3 {

    static class Mono<T> {
        private final T value;
        private final Throwable error;
        private final boolean isEmpty;

        private Mono(T value, Throwable error, boolean isEmpty) {
            this.value = value;
            this.error = error;
            this.isEmpty = isEmpty;
        }

        public static <T> Mono<T> just(T value) {
            return new Mono<>(value, null, false);
        }

        public static <T> Mono<T> empty() {
            return new Mono<>(null, null, true);
        }

        public static <T> Mono<T> error(Throwable t) {
            return new Mono<>(null, t, false);
        }

        public <R> Mono<R> map(Function<T, R> fn) {
            if (error != null) return Mono.error(error);
            if (isEmpty) return Mono.empty();
            try {
                return Mono.just(fn.apply(value));
            } catch (Exception e) {
                return Mono.error(e);
            }
        }

        public Mono<T> onErrorReturn(T fallback) {
            if (error != null) {
                System.out.println("  [onErrorReturn] Error '" + error.getMessage() + "' → fallback='" + fallback + "'");
                return Mono.just(fallback);
            }
            return this;
        }

        public void subscribe(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete) {
            System.out.println("  subscribe() llamado...");
            if (error != null) {
                System.out.println("  → señal: onError");
                onError.accept(error);
                return;
            }
            if (!isEmpty) {
                System.out.println("  → señal: onNext");
                onNext.accept(value);
            } else {
                System.out.println("  → (vacío, sin onNext)");
            }
            System.out.println("  → señal: onComplete");
            onComplete.run();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Mono.just(\"usuario-42\") ===\n");
        Mono.just("usuario-42")
            .map(u -> "Bienvenido, " + u)
            .subscribe(
                v  -> System.out.println("  onNext: " + v),
                e  -> System.out.println("  onError: " + e.getMessage()),
                () -> System.out.println("  onComplete")
            );

        System.out.println();
        System.out.println("=== Mono.empty() ===\n");
        Mono.<String>empty()
            .subscribe(
                v  -> System.out.println("  onNext: " + v),
                e  -> System.out.println("  onError: " + e.getMessage()),
                () -> System.out.println("  onComplete (sin emitir valor)")
            );

        System.out.println();
        System.out.println("=== Mono.error(\"no encontrado\") sin fallback ===\n");
        Mono.<String>error(new RuntimeException("no encontrado"))
            .subscribe(
                v  -> System.out.println("  onNext: " + v),
                e  -> System.out.println("  onError: " + e.getMessage()),
                () -> System.out.println("  onComplete")
            );

        System.out.println();
        System.out.println("=== Mono.error + onErrorReturn(\"anonimo\") ===\n");
        Mono.<String>error(new RuntimeException("no encontrado"))
            .onErrorReturn("anonimo")
            .subscribe(
                v  -> System.out.println("  onNext (recuperado): " + v),
                e  -> System.out.println("  onError: " + e.getMessage()),
                () -> System.out.println("  onComplete")
            );

        System.out.println();
        System.out.println("=== Diferencias de flujo ===");
        System.out.println("Éxito:         onSubscribe → onNext(valor) → onComplete");
        System.out.println("Vacío:         onSubscribe → onComplete (sin onNext)");
        System.out.println("Error:         onSubscribe → onError(t)    (sin onComplete)");
        System.out.println("onErrorReturn: convierte onError en onNext(fallback) → onComplete");
    }
}
