import java.util.concurrent.*;

public class ExpCompletableFutureErrors {

    public static void main(String[] args) throws Exception {

        ExecutorService pool = Executors.newFixedThreadPool(4);

        // exceptionally: recupera de un error devolviendo un valor por defecto
        // solo se ejecuta si hubo excepción; si no, el valor original pasa intacto
        String r1 = CompletableFuture
                .<String>supplyAsync(() -> { throw new RuntimeException("timeout"); }, pool)
                .exceptionally(ex -> "default-user")
                .get();
        System.out.println("exceptionally: " + r1);

        // handle: se ejecuta siempre (éxito O error), recibe (resultado, excepción)
        // permite inspeccionar ambos casos en un solo callback
        String r2 = CompletableFuture
                .supplyAsync(() -> { dormir(30); return "datos ok"; }, pool)
                .handle((result, ex) -> ex != null ? "recuperado: " + ex.getMessage() : result.toUpperCase())
                .get();
        System.out.println("handle (éxito): " + r2);

        String r3 = CompletableFuture
                .<String>supplyAsync(() -> { throw new RuntimeException("db down"); }, pool)
                .handle((result, ex) -> ex != null ? "recuperado: " + ex.getMessage() : result)
                .get();
        System.out.println("handle (error): " + r3);

        // whenComplete: side-effect en ambos casos, NO cambia el resultado ni lo recupera
        // útil para logging/métricas sin alterar el flujo
        CompletableFuture<String> cf = CompletableFuture
                .<String>supplyAsync(() -> { throw new RuntimeException("fallo red"); }, pool)
                .whenComplete((result, ex) -> {
                    if (ex != null) System.out.println("whenComplete: log error → " + ex.getMessage());
                    else            System.out.println("whenComplete: log ok → " + result);
                });
        // whenComplete no recupera: el CF sigue en estado de error — capturamos con exceptionally
        String r4 = cf.exceptionally(ex -> "fallback").get();
        System.out.println("valor final tras whenComplete+exceptionally: " + r4);

        // completeExceptionally: fuerza a un CF externo a fallar desde otro hilo
        CompletableFuture<String> manual = new CompletableFuture<>();
        pool.submit(() -> {
            dormir(40);
            manual.completeExceptionally(new IllegalStateException("cancelado externamente"));
        });
        String r5 = manual.exceptionally(ex -> "atrapado: " + ex.getMessage()).get();
        System.out.println("completeExceptionally: " + r5);

        // Las excepciones se propagan por la cadena hasta que alguien las atrapa
        System.out.println("\n=== Pipeline con fallo en paso 2 ===");

        // Caso A: recuperación al final de la cadena
        String rA = CompletableFuture
                .supplyAsync(() -> paso1(), pool)
                .thenApply(s -> paso2Falla(s))      // lanza excepción
                .thenApply(s -> paso3(s))           // se salta si hubo error
                .exceptionally(ex -> "recuperado al final: " + ex.getMessage())
                .get();
        System.out.println("Caso A: " + rA);

        // Caso B: recuperación intermedia — paso3 sí se ejecuta con el valor de recovery
        String rB = CompletableFuture
                .supplyAsync(() -> paso1(), pool)
                .thenApply(s -> paso2Falla(s))
                .exceptionally(ex -> "recovery")    // recupera aquí
                .thenApply(s -> paso3(s))           // ahora sí se ejecuta
                .get();
        System.out.println("Caso B: " + rB);

        pool.shutdown();
    }

    static String paso1()           { dormir(20); return "p1"; }
    static String paso2Falla(String s) { throw new RuntimeException("fallo en paso2 con: " + s); }
    static String paso3(String s)   { return s + "→p3"; }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
