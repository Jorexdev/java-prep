import java.util.List;

public class Ejercicio6 {

    enum EstadoPedido { PENDIENTE, CONFIRMADO, CANCELADO }

    static class Pedido {
        final String id;
        final EstadoPedido estado;
        final double total;
        final List<String> items;

        Pedido(String id, EstadoPedido estado, double total, List<String> items) {
            this.id = id;
            this.estado = estado;
            this.total = total;
            this.items = List.copyOf(items);
        }
    }

    // --- Custom assertion estilo AssertJ (sin la librería real) ---

    static class PedidoAssert {
        private final Pedido actual;

        private PedidoAssert(Pedido actual) { this.actual = actual; }

        static PedidoAssert assertThat(Pedido pedido) {
            if (pedido == null) throw new AssertionError("El pedido no puede ser null");
            return new PedidoAssert(pedido);
        }

        PedidoAssert estaConfirmado() {
            if (actual.estado != EstadoPedido.CONFIRMADO) {
                throw new AssertionError(
                    "Se esperaba estado CONFIRMADO pero fue: " + actual.estado);
            }
            return this;
        }

        PedidoAssert tieneTotal(double esperado) {
            if (Math.abs(actual.total - esperado) > 0.001) {
                throw new AssertionError(
                    "Se esperaba total=" + esperado + " pero fue: " + actual.total);
            }
            return this;
        }

        PedidoAssert contieneItem(String item) {
            if (!actual.items.contains(item)) {
                throw new AssertionError(
                    "Se esperaba que el pedido contuviese '" + item
                    + "' pero los items son: " + actual.items);
            }
            return this;
        }

        PedidoAssert tieneNumeroDeItems(int n) {
            if (actual.items.size() != n) {
                throw new AssertionError(
                    "Se esperaban " + n + " items pero hay: " + actual.items.size());
            }
            return this;
        }
    }

    // --- Mini test runner ---

    static void test(String nombre, Runnable assertion) {
        try {
            assertion.run();
            System.out.println("PASS  " + nombre);
        } catch (AssertionError e) {
            System.out.println("FAIL  " + nombre + " → " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Pedido pedidoConfirmado = new Pedido(
            "ORD-001",
            EstadoPedido.CONFIRMADO,
            149.97,
            List.of("Teclado", "Ratón", "Alfombrilla")
        );

        Pedido pedidoPendiente = new Pedido(
            "ORD-002",
            EstadoPedido.PENDIENTE,
            49.99,
            List.of("Monitor")
        );

        System.out.println("=== Batería de aserciones PedidoAssert ===\n");

        test("pedido confirmado tiene estado CONFIRMADO",
            () -> PedidoAssert.assertThat(pedidoConfirmado).estaConfirmado());

        test("pedido confirmado tiene total 149.97",
            () -> PedidoAssert.assertThat(pedidoConfirmado).tieneTotal(149.97));

        test("pedido confirmado contiene 'Ratón'",
            () -> PedidoAssert.assertThat(pedidoConfirmado).contieneItem("Ratón"));

        test("pedido confirmado tiene 3 items",
            () -> PedidoAssert.assertThat(pedidoConfirmado).tieneNumeroDeItems(3));

        // Estas dos deben fallar intencionadamente
        test("[FALLO ESPERADO] pedido pendiente está confirmado",
            () -> PedidoAssert.assertThat(pedidoPendiente).estaConfirmado());

        test("[FALLO ESPERADO] pedido pendiente contiene 'Teclado'",
            () -> PedidoAssert.assertThat(pedidoPendiente).contieneItem("Teclado"));

        // Encadenamiento fluido
        System.out.println("\n=== Encadenamiento fluido ===");
        test("pedido confirmado: encadenado confirmado + total + item",
            () -> PedidoAssert.assertThat(pedidoConfirmado)
                .estaConfirmado()
                .tieneTotal(149.97)
                .contieneItem("Teclado")
                .tieneNumeroDeItems(3));
    }
}
