import java.util.concurrent.CompletableFuture;

public class Ejercicio2 {
    static CompletableFuture<Double> obtenerPrecio(String producto) {
        return CompletableFuture.supplyAsync(() -> { try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } return 100.0; });
    }

    static CompletableFuture<Double> obtenerDescuento(String cliente) {
        return CompletableFuture.supplyAsync(() -> { try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } return 0.15; });
    }

    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        Double precioFinal = obtenerPrecio("Laptop")
            .thenCombine(obtenerDescuento("ClientePremium"), (precio, desc) -> precio * (1 - desc))
            .get();
        System.out.printf("Precio final: %.2f€ en %dms%n", precioFinal, System.currentTimeMillis() - t0);
    }
}
