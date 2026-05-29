import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

// Scheduler propio con Virtual Threads + publishOn/subscribeOn
public class Ejercicio1 {

    // ======================= SCHEDULER =======================
    interface Scheduler {
        void execute(Runnable task);
        String nombre();
    }

    // SchedulerIO: pool ilimitado de Virtual Threads (ideal para I/O)
    static class SchedulerIO implements Scheduler {
        private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        @Override
        public void execute(Runnable task) { executor.submit(task); }

        @Override
        public String nombre() { return "SchedulerIO[VT]"; }
    }

    // SchedulerCompute: pool acotado de Virtual Threads = núcleos disponibles
    static class SchedulerCompute implements Scheduler {
        private final int cores = Runtime.getRuntime().availableProcessors();
        private final ExecutorService executor = Executors.newFixedThreadPool(
            cores,
            Thread.ofVirtual().factory()
        );

        @Override
        public void execute(Runnable task) { executor.submit(task); }

        @Override
        public String nombre() { return "SchedulerCompute[" + cores + " VT]"; }
    }

    // ======================= REACTIVE PIPELINE =======================
    static class ReactivePipeline<T> {
        private final List<T> source;
        private Scheduler publishScheduler = null;
        private Scheduler subscribeScheduler = null;
        private final List<Function<Object, Object>> operators = new ArrayList<>();

        ReactivePipeline(List<T> source) {
            this.source = source;
        }

        @SafeVarargs
        static <T> ReactivePipeline<T> fromList(T... items) {
            return new ReactivePipeline<>(List.of(items));
        }

        // publishOn: cambia el hilo de los operadores siguientes (downstream)
        ReactivePipeline<T> publishOn(Scheduler scheduler) {
            this.publishScheduler = scheduler;
            return this;
        }

        // subscribeOn: cambia el hilo donde corre el origen del stream (upstream)
        ReactivePipeline<T> subscribeOn(Scheduler scheduler) {
            this.subscribeScheduler = scheduler;
            return this;
        }

        @SuppressWarnings("unchecked")
        <R> ReactivePipeline<R> map(Function<T, R> fn) {
            operators.add(item -> fn.apply((T) item));
            return (ReactivePipeline<R>) this;
        }

        void subscribe(Consumer<T> onNext, Runnable onComplete) throws Exception {
            // La suscripción y producción corren en subscribeScheduler (si está definido)
            Runnable produccion = () -> {
                System.out.println("[subscribe] Producción en: " + Thread.currentThread().getName()
                    + " | isVirtual=" + Thread.currentThread().isVirtual());

                for (T item : source) {
                    Object current = item;

                    // Aplicar operadores en publishScheduler (si está definido)
                    if (publishScheduler != null) {
                        final Object finalCurrent = current;
                        final Object[] resultHolder = {null};
                        final RuntimeException[] errorHolder = {null};
                        Object lock = new Object();

                        publishScheduler.execute(() -> {
                            System.out.println("  [publishOn:" + publishScheduler.nombre() + "] item=" + finalCurrent
                                + " en: " + Thread.currentThread().getName());
                            try {
                                Object res = finalCurrent;
                                for (Function<Object, Object> op : operators) {
                                    res = op.apply(res);
                                }
                                synchronized (lock) { resultHolder[0] = res; lock.notifyAll(); }
                            } catch (Exception e) {
                                synchronized (lock) { errorHolder[0] = new RuntimeException(e); lock.notifyAll(); }
                            }
                        });

                        synchronized (lock) {
                            while (resultHolder[0] == null && errorHolder[0] == null) {
                                try { lock.wait(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                            }
                        }

                        if (errorHolder[0] != null) throw errorHolder[0];
                        current = resultHolder[0];
                    } else {
                        for (Function<Object, Object> op : operators) {
                            current = op.apply(current);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    T result = (T) current;
                    System.out.println("  [onNext] " + result + " | hilo=" + Thread.currentThread().getName());
                    onNext.accept(result);
                }
                onComplete.run();
            };

            if (subscribeScheduler != null) {
                System.out.println("[subscribeOn:" + subscribeScheduler.nombre() + "] Origen en scheduler de I/O");
                Object lock = new Object();
                boolean[] done = {false};

                subscribeScheduler.execute(() -> {
                    try { produccion.run(); }
                    catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
                    finally { synchronized (lock) { done[0] = true; lock.notifyAll(); } }
                });

                synchronized (lock) {
                    while (!done[0]) {
                        try { lock.wait(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                }
            } else {
                try { produccion.run(); }
                catch (RuntimeException e) { throw e; }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Schedulers propios con Virtual Threads ===\n");

        Scheduler io      = new SchedulerIO();
        Scheduler compute = new SchedulerCompute();

        System.out.println("SchedulerIO:      " + io.nombre());
        System.out.println("SchedulerCompute: " + compute.nombre());
        System.out.println();

        System.out.println("--- Pipeline con subscribeOn(IO) + publishOn(Compute) ---\n");

        ReactivePipeline.fromList("dato-1", "dato-2", "dato-3")
            .subscribeOn(io)      // el origen corre en SchedulerIO
            .map(s -> {
                System.out.println("  [map] transformando '" + s + "' en: " + Thread.currentThread().getName());
                return s.toUpperCase() + "-procesado";
            })
            .publishOn(compute)   // los operadores siguientes corren en SchedulerCompute
            .subscribe(
                v -> System.out.println("  [result] " + v),
                () -> System.out.println("\n[onComplete] Pipeline finalizado.")
            );

        System.out.println();
        System.out.println("=== Diferencias clave ===");
        System.out.println("subscribeOn: afecta desde el origen hacia arriba (dónde corre la producción).");
        System.out.println("publishOn:   afecta desde donde se coloca hacia abajo (dónde corren los operadores).");
        System.out.println("Solo el subscribeOn más cercano al origen tiene efecto si hay varios.");
        System.out.println("Los publishOn sí se pueden encadenar para cambiar de scheduler en mitad del pipeline.");
    }
}
