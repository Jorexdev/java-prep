import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Ejercicio5 {
    record Usuario(int id, String nombre) {}

    static CompletableFuture<Usuario> buscarUsuario(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return new Usuario(id, "Usuario-" + id);
        });
    }

    static CompletableFuture<List<String>> obtenerPedidos(Usuario u) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return List.of("Pedido-A de " + u.nombre(), "Pedido-B de " + u.nombre());
        });
    }

    public static void main(String[] args) throws Exception {
        List<String> pedidos = buscarUsuario(42)
            .thenCompose(Ejercicio5::obtenerPedidos)
            .get();
        pedidos.forEach(System.out::println);
    }
}
