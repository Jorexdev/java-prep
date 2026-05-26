import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class ExpAsyncCombinators {

    public static void main(String[] args) throws Exception {

        // Virtual threads como executor: un hilo virtual por tarea, sin pool fijo
        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

        // ── allOf: espera a que TODOS terminen ──────────────────────────────
        System.out.println("=== allOf: fan-out a 3 servicios ===");

        CompletableFuture<String> s1 = callService("inventario",  60, exec);
        CompletableFuture<String> s2 = callService("precios",     80, exec);
        CompletableFuture<String> s3 = callService("disponible", 40, exec);

        // allOf devuelve CF<Void> — no puede devolver los resultados directamente
        // patrón estándar: guardar referencias, luego llamar .join() en cada una
        long t0 = System.currentTimeMillis();
        CompletableFuture.allOf(s1, s2, s3).join();
        long elapsed = System.currentTimeMillis() - t0;

        List<String> results = Stream.of(s1, s2, s3).map(CompletableFuture::join).collect(Collectors.toList());
        System.out.println("Resultados: " + results);
        System.out.println("Tiempo total: " + elapsed + " ms (esperado ≈ 80 ms, no 180 ms)");

        // Variante funcional: construir la lista de CFs dinámicamente y recoger resultados
        System.out.println("\n=== allOf dinámico ===");
        List<String> servicios = List.of("A", "B", "C", "D");
        List<CompletableFuture<String>> cfs = servicios.stream()
                .map(name -> callService(name, 50, exec))
                .collect(Collectors.toList());

        CompletableFuture<List<String>> allResults = CompletableFuture
                .allOf(cfs.toArray(new CompletableFuture[0]))
                .thenApply(v -> cfs.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        System.out.println("Todos: " + allResults.get());

        // ── anyOf: carrera — primero en terminar gana ───────────────────────
        System.out.println("\n=== anyOf: race entre réplicas ===");

        // Simula 3 réplicas de base de datos con latencias distintas
        CompletableFuture<String> replica1 = callService("replica-EU",  120, exec);
        CompletableFuture<String> replica2 = callService("replica-US",   30, exec);  // la más rápida
        CompletableFuture<String> replica3 = callService("replica-AP",   90, exec);

        long t1 = System.currentTimeMillis();
        // anyOf devuelve CF<Object> — castear al tipo conocido
        String fastest = (String) CompletableFuture.anyOf(replica1, replica2, replica3).get();
        System.out.println("Primera en responder: " + fastest);
        System.out.println("Tiempo hasta primera: " + (System.currentTimeMillis() - t1) + " ms");

        exec.shutdown();
    }

    static CompletableFuture<String> callService(String name, long latencyMs, ExecutorService exec) {
        return CompletableFuture.supplyAsync(() -> {
            dormir(latencyMs);
            return name + "[ok," + latencyMs + "ms]";
        }, exec);
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
