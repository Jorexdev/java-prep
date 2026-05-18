public class Ejercicio6 {

    static class GestorInventario {
        boolean verificarStock(String producto, int cantidad) {
            System.out.println("Inventario: verificando " + cantidad + "x " + producto);
            return true;
        }
        void reservar(String producto, int cantidad) {
            System.out.println("Inventario: reservando " + cantidad + "x " + producto);
        }
    }

    static class ProcesadorPago {
        boolean cobrar(double importe) {
            System.out.printf("Pago: cobrando %.2f€%n", importe);
            return true;
        }
    }

    static class ServicioEnvio {
        String crearEnvio(String producto, int cantidad) {
            String tracking = "TRK-" + (System.currentTimeMillis() % 10000);
            System.out.println("Envio: creado " + tracking + " para " + cantidad + "x " + producto);
            return tracking;
        }
    }

    static class NotificadorEmail {
        void enviar(String email, String mensaje) {
            System.out.println("Email a " + email + ": " + mensaje);
        }
    }

    static class FachadaPedidos {
        private final GestorInventario inventario = new GestorInventario();
        private final ProcesadorPago pago         = new ProcesadorPago();
        private final ServicioEnvio envio         = new ServicioEnvio();
        private final NotificadorEmail notif      = new NotificadorEmail();

        void realizarPedido(String producto, int cantidad, String email) {
            System.out.println("=== Procesando pedido ===");
            if (!inventario.verificarStock(producto, cantidad)) { System.out.println("Sin stock"); return; }
            if (!pago.cobrar(cantidad * 49.99))                 { System.out.println("Pago rechazado"); return; }
            inventario.reservar(producto, cantidad);
            String tracking = envio.crearEnvio(producto, cantidad);
            notif.enviar(email, "Pedido confirmado. Seguimiento: " + tracking);
            System.out.println("=== Pedido completado ===");
        }
    }

    public static void main(String[] args) {
        new FachadaPedidos().realizarPedido("Laptop", 1, "cliente@example.com");
    }
}
