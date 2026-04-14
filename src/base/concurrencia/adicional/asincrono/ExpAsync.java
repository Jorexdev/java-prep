package base.concurrencia.adicional.asincrono;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ExpAsync {

    public static void main(String[] args) throws Exception {

        ExecutorService pool = Executors.newFixedThreadPool(4);

        // submit: ejecuta una Callable y devuelve un Future con el resultado
        // Future.get() bloquea hasta que la tarea termina
        Future<Integer> res = pool.submit(() -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return 42;
        });
        System.out.println("Future.get(): " + res.get());

        // invokeAll: ejecuta varias Callables y espera a que todas terminen
        List<Callable<String>> tareas = List.of(() -> "A", () -> "B", () -> "C");
        List<Future<String>> resultados = pool.invokeAll(tareas);
        System.out.println("invokeAll: " + resultados.stream()
                .map(f -> { try { return f.get(); } catch (Exception e) { return "err"; } })
                .collect(Collectors.toList()));

        // supplyAsync + thenApply + thenCompose: pipeline asíncrono encadenado
        // cada paso se ejecuta cuando el anterior termina
        CompletableFuture<String> cf = CompletableFuture
                .supplyAsync(() -> { dormir(80); return "java"; }, pool)
                .thenApply(String::toUpperCase)         // transforma el resultado
                .thenCompose(s ->                        // encadena otra tarea asíncrona
                        CompletableFuture.supplyAsync(() -> s + " ROCKS!", pool))
                .exceptionally(ex -> "RECUPERADO: " + ex.getMessage());

        System.out.println("CompletableFuture pipeline: " + cf.get());

        // thenCombine: combina dos tareas independientes cuando ambas terminan
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> { dormir(50); return 10; }, pool);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> { dormir(60); return 32; }, pool);
        System.out.println("thenCombine (suma): " + a.thenCombine(b, Integer::sum).get());

        // allOf: espera a que TODOS los futuros terminen
        // útil para lanzar N tareas en paralelo y esperar a todas
        CompletableFuture<Void> all = CompletableFuture.allOf(
                tarea("T1", 40),
                tarea("T2", 70),
                tarea("T3", 20)
        );
        all.join();

        pool.shutdown();
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    static CompletableFuture<Void> tarea(String nombre, long ms) {
        return CompletableFuture.runAsync(() -> {
            dormir(ms);
            System.out.println(nombre + " completada");
        });
    }
}
