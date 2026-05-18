import java.util.Optional;

public class Ejercicio3 {

    static class Pedido {
        private final String usuario;
        private final int    productoStock;
        private final double precio;

        Pedido(String usuario, int productoStock, double precio) {
            this.usuario        = usuario;
            this.productoStock  = productoStock;
            this.precio         = precio;
        }

        String getUsuario()       { return usuario; }
        int    getProductoStock() { return productoStock; }
        double getPrecio()        { return precio; }
    }

    // Retorna Optional<String> con el primer error, o empty() si todo está bien
    static Optional<String> validarPedido(Pedido p) {
        if (p.getUsuario() == null || p.getUsuario().isBlank()) {
            return Optional.of("Error: el usuario no puede ser nulo o vacío");
        }
        if (p.getProductoStock() <= 0) {
            return Optional.of("Error: el producto no tiene stock disponible");
        }
        if (p.getPrecio() < 0) {
            return Optional.of("Error: el precio no puede ser negativo");
        }
        return Optional.empty(); // pedido válido
    }

    static void procesarPedido(Pedido p) {
        Optional<String> error = validarPedido(p);
        error.ifPresentOrElse(
                msg -> System.out.println("Pedido rechazado — " + msg),
                ()  -> System.out.println("Pedido aceptado para usuario: " + p.getUsuario())
        );
    }

    public static void main(String[] args) {
        // Pedido válido
        procesarPedido(new Pedido("Jorge", 5, 99.99));

        // Usuario nulo
        procesarPedido(new Pedido(null, 5, 99.99));

        // Sin stock
        procesarPedido(new Pedido("Ana", 0, 49.99));

        // Precio negativo
        procesarPedido(new Pedido("Luis", 3, -10.0));

        // Varios errores — solo se reporta el primero (early-return)
        procesarPedido(new Pedido(null, 0, -5.0));
    }
}
