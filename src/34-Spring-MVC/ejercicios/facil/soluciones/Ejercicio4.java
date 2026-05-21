// @RestController
// @RequestMapping("/api")
public class Ejercicio4 {

    static class CrearPedidoRequest {
        String producto;
        int cantidad;

        CrearPedidoRequest(String producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }
    }

    // @PostMapping("/pedidos")
    static String crearPedido(/* @RequestBody */ CrearPedidoRequest req) {
        if (req.producto == null || req.producto.isBlank()) {
            throw new IllegalArgumentException("El producto no puede estar vacío");
        }
        if (req.cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        return "Pedido creado: " + req.producto + " x" + req.cantidad;
    }

    public static void main(String[] args) {
        try {
            System.out.println(crearPedido(new CrearPedidoRequest("Teclado", 3)));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            System.out.println(crearPedido(new CrearPedidoRequest("Ratón", 0)));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            System.out.println(crearPedido(new CrearPedidoRequest("  ", 2)));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
