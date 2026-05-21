import java.util.HashMap;
import java.util.Map;

public class Ejercicio3 {

    // --- Contexto Ventas ---

    static class PedidoVentas {
        final String id;
        final int clienteId;
        final double total;

        PedidoVentas(String id, int clienteId, double total) {
            this.id = id;
            this.clienteId = clienteId;
            this.total = total;
        }

        @Override
        public String toString() {
            return "PedidoVentas{id='" + id + "', clienteId=" + clienteId + ", total=" + total + "}";
        }
    }

    // --- Contexto Facturación ---

    static class Factura {
        final String id;
        final String nif;
        final double importe;

        Factura(String id, String nif, double importe) {
            this.id = id;
            this.nif = nif;
            this.importe = importe;
        }

        @Override
        public String toString() {
            return "Factura{id='" + id + "', nif='" + nif + "', importe=" + importe + "}";
        }
    }

    // --- Anti-Corruption Layer ---

    interface ClienteInfoProvider {
        String getNif(int clienteId);
    }

    static class InMemoryClienteInfoProvider implements ClienteInfoProvider {
        private final Map<Integer, String> nifPorCliente = new HashMap<>();

        void registrar(int clienteId, String nif) {
            nifPorCliente.put(clienteId, nif);
        }

        @Override
        public String getNif(int clienteId) {
            String nif = nifPorCliente.get(clienteId);
            if (nif == null) throw new IllegalArgumentException("Cliente no encontrado: " + clienteId);
            return nif;
        }
    }

    static class AntiCorruptionLayer {
        private final ClienteInfoProvider clienteInfo;

        AntiCorruptionLayer(ClienteInfoProvider clienteInfo) {
            this.clienteInfo = clienteInfo;
        }

        String resolverNif(int clienteId) {
            return clienteInfo.getNif(clienteId);
        }
    }

    static class FacturacionService {
        private final AntiCorruptionLayer acl;
        private int facturaCounter = 1;

        FacturacionService(AntiCorruptionLayer acl) {
            this.acl = acl;
        }

        Factura facturar(PedidoVentas pedido) {
            String nif = acl.resolverNif(pedido.clienteId);
            Factura factura = new Factura("FAC-" + facturaCounter++, nif, pedido.total);
            System.out.println("Factura generada: " + factura);
            return factura;
        }
    }

    public static void main(String[] args) {
        InMemoryClienteInfoProvider clienteInfo = new InMemoryClienteInfoProvider();
        clienteInfo.registrar(10, "12345678A");
        clienteInfo.registrar(20, "87654321B");

        AntiCorruptionLayer acl = new AntiCorruptionLayer(clienteInfo);
        FacturacionService facturacion = new FacturacionService(acl);

        System.out.println("--- Creando pedidos en contexto Ventas ---");
        PedidoVentas p1 = new PedidoVentas("PED-001", 10, 250.0);
        PedidoVentas p2 = new PedidoVentas("PED-002", 20, 89.99);
        System.out.println("Pedido: " + p1);
        System.out.println("Pedido: " + p2);

        System.out.println("\n--- Contexto Facturación (solo conoce su modelo) ---");
        facturacion.facturar(p1);
        facturacion.facturar(p2);

        System.out.println("\n--- Cambio en modelo de Ventas (añadir campo descripción) ---");
        System.out.println("PedidoVentas ahora tiene campo 'descripcion' — Facturación no se entera");
        System.out.println("Solo hay que tocar AntiCorruptionLayer si cambia clienteId o total");

        System.out.println("\n--- Cliente inexistente ---");
        try {
            facturacion.facturar(new PedidoVentas("PED-003", 99, 50.0));
        } catch (IllegalArgumentException e) {
            System.out.println("ACL rechaza: " + e.getMessage());
        }
    }
}
