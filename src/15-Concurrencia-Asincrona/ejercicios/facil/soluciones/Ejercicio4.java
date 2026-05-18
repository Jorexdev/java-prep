import java.util.concurrent.CompletableFuture;

public class Ejercicio4 {
    public static void main(String[] args) throws Exception {
        CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "{\"usuario\":\"jorge\",\"score\":42}";
            })
            .thenApply(json -> "Parseado: " + json.replace("{", "(").replace("}", ")"))
            .thenAccept(resultado -> System.out.println("Resultado: " + resultado))
            .get();
        System.out.println("Pipeline completado");
    }
}
