import java.util.concurrent.CompletableFuture;

public class Ejercicio5 {
    record Pedido(String id, String datos) {}

    static CompletableFuture<Pedido> validar(Pedido p) {
        return CompletableFuture.supplyAsync(() -> {
            if (p.datos().isBlank()) throw new RuntimeException("Datos vacíos");
            System.out.println("Validado: " + p.id()); return p;
        });
    }

    static CompletableFuture<Pedido> enriquecer(Pedido p) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Enriquecido: " + p.id());
            return new Pedido(p.id(), p.datos() + " [enriquecido]");
        });
    }

    static CompletableFuture<Pedido> persistir(Pedido p) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Persistido: " + p.id()); return p;
        });
    }

    static CompletableFuture<Void> notificar(Pedido p) {
        return CompletableFuture.runAsync(() -> System.out.println("Notificado: " + p.id()));
    }

    public static void main(String[] args) throws Exception {
        Pedido pedidoOk  = new Pedido("P001", "laptop x2");
        Pedido pedidoMal = new Pedido("P002", "");

        for (Pedido pedido : new Pedido[]{pedidoOk, pedidoMal}) {
            System.out.println("--- Procesando " + pedido.id() + " ---");
            validar(pedido)
                .handle((p, ex) -> ex != null ? new Pedido(pedido.id(), "INVALID") : p)
                .thenCompose(Ejercicio5::enriquecer)
                .thenCompose(Ejercicio5::persistir)
                .thenCompose(Ejercicio5::notificar)
                .get();
        }
    }
}
