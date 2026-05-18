import java.util.concurrent.CompletableFuture;

public class Ejercicio6 {
    static CompletableFuture<String> operacionQuePuedeFallar(boolean fallar) {
        return CompletableFuture.supplyAsync(() -> {
            if (fallar) throw new RuntimeException("Servicio no disponible");
            return "Resultado OK";
        });
    }

    public static void main(String[] args) throws Exception {
        String r1 = operacionQuePuedeFallar(false)
            .exceptionally(ex -> "Fallback: " + ex.getMessage())
            .whenComplete((r, ex) -> System.out.println("whenComplete: resultado=" + r + " error=" + ex))
            .get();
        System.out.println("Resultado: " + r1);

        String r2 = operacionQuePuedeFallar(true)
            .exceptionally(ex -> "Fallback: " + ex.getMessage())
            .whenComplete((r, ex) -> System.out.println("whenComplete: resultado=" + r + " error=" + ex))
            .get();
        System.out.println("Resultado: " + r2);
    }
}
