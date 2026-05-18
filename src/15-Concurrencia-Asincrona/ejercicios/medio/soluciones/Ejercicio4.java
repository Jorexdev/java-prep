import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Ejercicio4 {
    static CompletableFuture<String> servicioLento(long ms) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "respuesta tras " + ms + "ms";
        });
    }

    public static void main(String[] args) throws Exception {
        try {
            String r = servicioLento(500).orTimeout(300, TimeUnit.MILLISECONDS).get();
            System.out.println("orTimeout OK: " + r);
        } catch (Exception e) {
            System.out.println("orTimeout: " + e.getCause().getClass().getSimpleName());
        }

        String r2 = servicioLento(500)
            .completeOnTimeout("respuesta-cache", 300, TimeUnit.MILLISECONDS)
            .get();
        System.out.println("completeOnTimeout: " + r2);
    }
}
