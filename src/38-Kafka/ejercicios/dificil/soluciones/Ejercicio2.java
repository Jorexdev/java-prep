import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Ejercicio2 {

    interface EventHandler {
        void handle(String eventType, Map<String, String> data);
    }

    static class EventBus {
        private final Map<String, List<EventHandler>> handlers = new HashMap<>();

        void subscribe(String topic, EventHandler handler) {
            handlers.computeIfAbsent(topic, k -> new ArrayList<>()).add(handler);
        }

        void publish(String topic, Map<String, String> data) {
            System.out.println("[BUS] publicando '" + topic + "' data=" + data);
            List<EventHandler> list = handlers.getOrDefault(topic, List.of());
            for (EventHandler h : list) {
                h.handle(topic, data);
            }
        }
    }

    static class PedidoService {
        private final EventBus bus;

        PedidoService(EventBus bus) {
            this.bus = bus;
            bus.subscribe("StockInsuficiente", (type, data) -> {
                System.out.println("[PedidoService] recibido StockInsuficiente → compensando pedido " + data.get("pedidoId"));
                bus.publish("PedidoCancelado", Map.of("pedidoId", data.get("pedidoId"), "motivo", "stock insuficiente"));
            });
            bus.subscribe("PagoRechazado", (type, data) -> {
                System.out.println("[PedidoService] recibido PagoRechazado → compensando pedido " + data.get("pedidoId"));
                bus.publish("PedidoCancelado", Map.of("pedidoId", data.get("pedidoId"), "motivo", "pago rechazado"));
            });
        }

        void crearPedido(String pedidoId, String producto) {
            System.out.println("[PedidoService] creando pedido " + pedidoId);
            bus.publish("PedidoCreado", Map.of("pedidoId", pedidoId, "producto", producto));
        }
    }

    static class InventarioService {
        private final EventBus bus;
        private final List<String> sinStock;

        InventarioService(EventBus bus, List<String> sinStock) {
            this.bus = bus;
            this.sinStock = sinStock;
            bus.subscribe("PedidoCreado", (type, data) -> {
                String pedidoId = data.get("pedidoId");
                String producto = data.get("producto");
                if (sinStock.contains(producto)) {
                    System.out.println("[InventarioService] sin stock para " + producto);
                    bus.publish("StockInsuficiente", Map.of("pedidoId", pedidoId, "producto", producto));
                } else {
                    System.out.println("[InventarioService] stock reservado para " + producto);
                    bus.publish("StockReservado", Map.of("pedidoId", pedidoId, "producto", producto));
                }
            });
        }
    }

    static class PagoService {
        private final EventBus bus;
        private final List<String> pedidosSinFondos;

        PagoService(EventBus bus, List<String> pedidosSinFondos) {
            this.bus = bus;
            this.pedidosSinFondos = pedidosSinFondos;
            bus.subscribe("StockReservado", (type, data) -> {
                String pedidoId = data.get("pedidoId");
                if (pedidosSinFondos.contains(pedidoId)) {
                    System.out.println("[PagoService] pago rechazado para pedido " + pedidoId);
                    bus.publish("PagoRechazado", Map.of("pedidoId", pedidoId));
                } else {
                    System.out.println("[PagoService] pago aprobado para pedido " + pedidoId);
                    bus.publish("PagoAprobado", Map.of("pedidoId", pedidoId));
                }
            });
        }
    }

    static class EnvioService {
        private final EventBus bus;

        EnvioService(EventBus bus) {
            this.bus = bus;
            bus.subscribe("PagoAprobado", (type, data) -> {
                String pedidoId = data.get("pedidoId");
                System.out.println("[EnvioService] creando envío para pedido " + pedidoId);
                bus.publish("EnvioCreado", Map.of("pedidoId", pedidoId, "tracking", "TRK-" + pedidoId));
            });
        }
    }

    static EventBus buildBus(List<String> sinStock, List<String> sinFondos) {
        EventBus bus = new EventBus();
        new PedidoService(bus);
        new InventarioService(bus, sinStock);
        new PagoService(bus, sinFondos);
        new EnvioService(bus);
        return bus;
    }

    public static void main(String[] args) {
        System.out.println("=== FLUJO 1: éxito completo ===");
        EventBus bus1 = buildBus(List.of(), List.of());
        new PedidoService(bus1).crearPedido("P-001", "laptop");

        System.out.println("\n=== FLUJO 2: fallo en stock ===");
        EventBus bus2 = buildBus(List.of("silla"), List.of());
        new PedidoService(bus2).crearPedido("P-002", "silla");

        System.out.println("\n=== FLUJO 3: fallo en pago (con compensación) ===");
        EventBus bus3 = buildBus(List.of(), List.of("P-003"));
        new PedidoService(bus3).crearPedido("P-003", "monitor");
    }
}
