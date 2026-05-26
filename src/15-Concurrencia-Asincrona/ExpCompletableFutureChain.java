import java.util.List;
import java.util.concurrent.*;

public class ExpCompletableFutureChain {

    public static void main(String[] args) throws Exception {

        ExecutorService pool = Executors.newFixedThreadPool(4);

        // thenApply: transforma el resultado (Function<T,R>) — síncrono sobre el mismo hilo
        CompletableFuture<String> upper = CompletableFuture
                .supplyAsync(() -> "usuario:42", pool)
                .thenApply(String::toUpperCase);
        System.out.println("thenApply:   " + upper.get());

        // thenAccept: consume el resultado (Consumer<T>) — no devuelve valor
        CompletableFuture<Void> printed = CompletableFuture
                .supplyAsync(() -> "hola", pool)
                .thenAccept(s -> System.out.println("thenAccept:  " + s));
        printed.get();

        // thenCompose vs thenApply:
        //   thenApply(f) donde f: T -> CompletableFuture<R>  => CF<CF<R>>  (anidado, malo)
        //   thenCompose(f) donde f: T -> CompletableFuture<R> => CF<R>     (aplanado, correcto)
        CompletableFuture<List<String>> ordenes = CompletableFuture
                .supplyAsync(() -> fetchUser(42), pool)
                .thenCompose(user -> fetchOrders(user, pool));   // fetchOrders también es async
        System.out.println("thenCompose: órdenes de " + ordenes.get());

        // thenCombine: dos futuros independientes, combina cuando ambos terminan
        CompletableFuture<Double> precioA = CompletableFuture.supplyAsync(() -> { dormir(50); return 9.99;  }, pool);
        CompletableFuture<Double> precioB = CompletableFuture.supplyAsync(() -> { dormir(80); return 4.50;  }, pool);
        double total = precioA.thenCombine(precioB, Double::sum).get();
        System.out.printf("thenCombine: total precios = %.2f%n", total);

        // thenRun: efecto lateral sin acceso al resultado (Runnable)
        CompletableFuture<Void> sideEffect = CompletableFuture
                .supplyAsync(() -> "trabajo hecho", pool)
                .thenRun(() -> System.out.println("thenRun:     log de auditoría escrito"));
        sideEffect.get();

        // Pipeline completo: fetch user → fetch orders (dependiente) → fetch prices (paralelo) → combinar
        System.out.println("\n=== Pipeline completo ===");

        CompletableFuture<String> resumen = CompletableFuture
                .supplyAsync(() -> fetchUser(7), pool)                          // 1. usuario
                .thenCompose(user -> fetchOrders(user, pool))                   // 2. órdenes (depende del usuario)
                .thenCompose(orders -> {
                    // 3. precios de cada orden en paralelo
                    CompletableFuture<Double> p1 = CompletableFuture.supplyAsync(() -> { dormir(30); return 12.0; }, pool);
                    CompletableFuture<Double> p2 = CompletableFuture.supplyAsync(() -> { dormir(40); return  8.5; }, pool);
                    return p1.thenCombine(p2, Double::sum)
                             .thenApply(sum -> orders + " | total: " + sum + "€");
                });

        System.out.println("Resultado: " + resumen.get());

        pool.shutdown();
    }

    static String fetchUser(int id) {
        dormir(60);
        return "user-" + id;
    }

    // Devuelve un CF — por eso usamos thenCompose en lugar de thenApply
    static CompletableFuture<List<String>> fetchOrders(String user, ExecutorService pool) {
        return CompletableFuture.supplyAsync(() -> {
            dormir(70);
            return List.of("order-1", "order-2");
        }, pool);
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
