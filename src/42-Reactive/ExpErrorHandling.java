import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// Manejo de errores en pipelines reactivos:
// onErrorReturn, onErrorResume, retry con backoff, doOnError
public class ExpErrorHandling {

    // ======================= RESULTADO REACTIVO =======================
    // Representa un elemento que puede ser valor o error
    sealed interface Result<T> {
        record Ok<T>(T value) implements Result<T> {}
        record Err<T>(Throwable error) implements Result<T> {}
    }

    // ======================= REACTIVE PIPELINE CON ERROR HANDLING =======================
    static class ReactPipeline<T> {
        private final List<Result<T>> items;

        ReactPipeline(List<Result<T>> items) {
            this.items = items;
        }

        @SafeVarargs
        static <T> ReactPipeline<T> of(T... values) {
            List<Result<T>> items = new ArrayList<>();
            for (T v : values) items.add(new Result.Ok<>(v));
            return new ReactPipeline<>(items);
        }

        // Permite inyectar un error en posición específica para demos
        static <T> ReactPipeline<T> withError(List<Object> mixed) {
            List<Result<T>> items = new ArrayList<>();
            for (Object o : mixed) {
                if (o instanceof Throwable t) {
                    items.add(new Result.Err<>(t));
                } else {
                    @SuppressWarnings("unchecked")
                    T val = (T) o;
                    items.add(new Result.Ok<>(val));
                }
            }
            return new ReactPipeline<>(items);
        }

        // map: si hay error en el item, propaga el error
        <R> ReactPipeline<R> map(Function<T, R> fn) {
            List<Result<R>> result = new ArrayList<>();
            for (Result<T> item : items) {
                if (item instanceof Result.Ok<T> ok) {
                    try {
                        result.add(new Result.Ok<>(fn.apply(ok.value())));
                    } catch (Exception e) {
                        result.add(new Result.Err<>(e));
                    }
                } else if (item instanceof Result.Err<T> err) {
                    result.add(new Result.Err<>(err.error()));
                }
            }
            return new ReactPipeline<>(result);
        }

        // doOnError: efecto secundario al encontrar error, sin alterar el flujo
        ReactPipeline<T> doOnError(Consumer<Throwable> action) {
            for (Result<T> item : items) {
                if (item instanceof Result.Err<T> err) {
                    action.accept(err.error());
                }
            }
            return this;
        }

        // onErrorReturn: reemplazar error por un valor por defecto
        ReactPipeline<T> onErrorReturn(T fallback) {
            List<Result<T>> result = new ArrayList<>();
            for (Result<T> item : items) {
                if (item instanceof Result.Err) {
                    result.add(new Result.Ok<>(fallback));
                } else {
                    result.add(item);
                }
            }
            return new ReactPipeline<>(result);
        }

        // onErrorResume: reemplazar error con resultado de una función de recuperación
        ReactPipeline<T> onErrorResume(Function<Throwable, T> fn) {
            List<Result<T>> result = new ArrayList<>();
            for (Result<T> item : items) {
                if (item instanceof Result.Err<T> err) {
                    try {
                        result.add(new Result.Ok<>(fn.apply(err.error())));
                    } catch (Exception e2) {
                        result.add(new Result.Err<>(e2));
                    }
                } else {
                    result.add(item);
                }
            }
            return new ReactPipeline<>(result);
        }

        void subscribe(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete) {
            for (Result<T> item : items) {
                if (item instanceof Result.Ok<T> ok) {
                    onNext.accept(ok.value());
                } else if (item instanceof Result.Err<T> err) {
                    onError.accept(err.error());
                    return; // error termina el stream
                }
            }
            onComplete.run();
        }
    }

    // ======================= RETRY CON BACKOFF =======================
    // Simula retry con backoff exponencial: espera 100ms, 200ms, 400ms...
    static class ServicioInestable {
        private int intentos = 0;
        private final int fallosAntes; // número de fallos antes de éxito

        ServicioInestable(int fallosAntes) {
            this.fallosAntes = fallosAntes;
        }

        String llamar() throws Exception {
            intentos++;
            if (intentos <= fallosAntes) {
                System.out.println("  [Servicio] Intento " + intentos + " → FALLO");
                throw new RuntimeException("Servicio no disponible (intento " + intentos + ")");
            }
            System.out.println("  [Servicio] Intento " + intentos + " → EXITO");
            return "respuesta-ok";
        }
    }

    static String retryConBackoff(ServicioInestable servicio, int maxRetries) throws Exception {
        long espera = 100; // ms inicial
        for (int i = 0; i <= maxRetries; i++) {
            try {
                return servicio.llamar();
            } catch (Exception e) {
                if (i == maxRetries) throw e; // último intento fallido → propagar
                System.out.println("  [Retry] Esperando " + espera + "ms antes del reintento " + (i + 2) + "...");
                Thread.sleep(espera);
                espera *= 2; // backoff exponencial
            }
        }
        throw new RuntimeException("Nunca debería llegar aquí");
    }

    public static void main(String[] args) throws Exception {

        System.out.println("=== doOnError (efecto secundario sin cambiar flujo) ===\n");

        List<Object> datos = new ArrayList<>();
        datos.add(10);
        datos.add(0); // provocará división por cero en map
        datos.add(5);

        ReactPipeline.<Integer>withError(datos)
            .map(n -> {
                if (n == 0) throw new ArithmeticException("Division por cero");
                return 100 / n;
            })
            .doOnError(e -> System.out.println("  [LOG] Error detectado: " + e.getMessage()))
            .subscribe(
                v  -> System.out.println("  onNext: " + v),
                e  -> System.out.println("  onError: " + e.getMessage()),
                () -> System.out.println("  onComplete")
            );

        System.out.println();
        System.out.println("=== onErrorReturn (valor de fallback) ===\n");

        ReactPipeline.of(10, 5, 0, 2)
            .map(n -> {
                if (n == 0) throw new ArithmeticException("Division por cero");
                return 100 / n;
            })
            .onErrorReturn(-1) // si hay error, emitir -1 y continuar
            .subscribe(
                v  -> System.out.println("  onNext: " + v),
                e  -> System.out.println("  onError: " + e.getMessage()),
                () -> System.out.println("  onComplete")
            );

        System.out.println();
        System.out.println("=== onErrorResume (fallback con lógica) ===\n");

        ReactPipeline.of("42", "abc", "7")
            .map(s -> {
                int val = Integer.parseInt(s); // "abc" lanza NumberFormatException
                return val * 2;
            })
            .onErrorResume(e -> {
                System.out.println("  [Recover] Error: " + e.getMessage() + " → usando 0");
                return 0;
            })
            .subscribe(
                v  -> System.out.println("  onNext: " + v),
                e  -> System.out.println("  onError: " + e.getMessage()),
                () -> System.out.println("  onComplete")
            );

        System.out.println();
        System.out.println("=== retry con backoff exponencial ===\n");
        System.out.println("Servicio que falla 3 veces antes de responder:");

        try {
            String resultado = retryConBackoff(new ServicioInestable(3), 5);
            System.out.println("  Resultado final: " + resultado);
        } catch (Exception e) {
            System.out.println("  Error irrecuperable: " + e.getMessage());
        }

        System.out.println();
        System.out.println("Servicio que siempre falla (maxRetries=2):");
        try {
            retryConBackoff(new ServicioInestable(10), 2);
        } catch (Exception e) {
            System.out.println("  Error irrecuperable propagado: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Resumen de operadores de error ===");
        System.out.println("doOnError(fn)       — efecto secundario (log), no altera el stream");
        System.out.println("onErrorReturn(val)  — emite valor por defecto, stream continúa/completa");
        System.out.println("onErrorResume(fn)   — fallback con lógica compleja");
        System.out.println("retry(n)            — vuelve a suscribirse hasta n veces");
        System.out.println("retryWhen(fn)       — retry con backoff exponencial personalizado");
    }
}
