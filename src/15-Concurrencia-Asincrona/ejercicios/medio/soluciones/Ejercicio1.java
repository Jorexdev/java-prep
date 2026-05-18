import java.util.concurrent.CompletableFuture;

public class Ejercicio1 {
    static CompletableFuture<String> llamadaHttp(String nombre, long ms) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return nombre + " respondió en " + ms + "ms";
        });
    }

    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        CompletableFuture.allOf(
            llamadaHttp("ServiceA", 200),
            llamadaHttp("ServiceB", 300),
            llamadaHttp("ServiceC", 150),
            llamadaHttp("ServiceD", 250)
        ).get();
        System.out.println("allOf completado en " + (System.currentTimeMillis() - t0) + "ms");

        long t1 = System.currentTimeMillis();
        Object primero = CompletableFuture.anyOf(
            llamadaHttp("Cache1", 300),
            llamadaHttp("Cache2", 100),
            llamadaHttp("Cache3", 200)
        ).get();
        System.out.println("anyOf: " + primero + " en " + (System.currentTimeMillis() - t1) + "ms");
    }
}
