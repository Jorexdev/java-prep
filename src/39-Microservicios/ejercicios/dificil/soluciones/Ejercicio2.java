import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Ejercicio2 {

    static class Event {
        final String type;
        final Map<String, Object> data;

        Event(String type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
        }
    }

    static class EventBus {
        private final Map<String, List<Consumer<Event>>> subscribers = new HashMap<>();

        void subscribe(String eventType, Consumer<Event> handler) {
            subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
        }

        void publish(Event event) {
            System.out.println("[EventBus] → " + event.type + " " + event.data);
            List<Consumer<Event>> handlers = subscribers.getOrDefault(event.type, Collections.emptyList());
            for (Consumer<Event> handler : handlers) {
                handler.accept(event);
            }
        }
    }

    static class PedidoService {
        private final EventBus bus;

        PedidoService(EventBus bus) {
            this.bus = bus;
            bus.subscribe("StockAgotado", this::onCancelacion);
            bus.subscribe("PagoRechazado", this::onCancelacion);
        }

        void crear(int pedidoId, String producto) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("pedidoId", pedidoId);
            data.put("producto", producto);
            bus.publish(new Event("PedidoCreado", data));
        }

        private void onCancelacion(Event event) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("pedidoId", event.data.get("pedidoId"));
            data.put("motivo", event.type);
            bus.publish(new Event("PedidoCancelado", data));
        }
    }

    static class InventarioService {
        private final EventBus bus;
        private final Set<Integer> agotados;

        InventarioService(EventBus bus, Set<Integer> agotados) {
            this.bus = bus;
            this.agotados = agotados;
            bus.subscribe("PedidoCreado", this::onPedidoCreado);
        }

        private void onPedidoCreado(Event event) {
            int pedidoId = (int) event.data.get("pedidoId");
            Map<String, Object> data = new LinkedHashMap<>(event.data);
            if (agotados.contains(pedidoId)) {
                bus.publish(new Event("StockAgotado", data));
            } else {
                bus.publish(new Event("StockReservado", data));
            }
        }
    }

    static class PagoService {
        private final EventBus bus;
        private final Set<Integer> rechazados;

        PagoService(EventBus bus, Set<Integer> rechazados) {
            this.bus = bus;
            this.rechazados = rechazados;
            bus.subscribe("StockReservado", this::onStockReservado);
        }

        private void onStockReservado(Event event) {
            int pedidoId = (int) event.data.get("pedidoId");
            Map<String, Object> data = new LinkedHashMap<>(event.data);
            if (rechazados.contains(pedidoId)) {
                bus.publish(new Event("PagoRechazado", data));
            } else {
                bus.publish(new Event("PagoAprobado", data));
            }
        }
    }

    static class EnvioService {
        private final EventBus bus;

        EnvioService(EventBus bus) {
            this.bus = bus;
            bus.subscribe("PagoAprobado", this::onPagoAprobado);
        }

        private void onPagoAprobado(Event event) {
            Map<String, Object> data = new LinkedHashMap<>(event.data);
            data.put("trackingId", "TRK-" + event.data.get("pedidoId"));
            bus.publish(new Event("EnvioCreado", data));
        }
    }

    static class NotificacionService {
        NotificacionService(EventBus bus) {
            bus.subscribe("EnvioCreado", e ->
                System.out.println("[Notificacion] Pedido #" + e.data.get("pedidoId")
                    + " enviado. Tracking: " + e.data.get("trackingId")));
            bus.subscribe("PedidoCancelado", e ->
                System.out.println("[Notificacion] Pedido #" + e.data.get("pedidoId")
                    + " CANCELADO. Motivo: " + e.data.get("motivo")));
        }
    }

    static void buildServices(EventBus bus, Set<Integer> agotados, Set<Integer> rechazados) {
        new PedidoService(bus);
        new InventarioService(bus, agotados);
        new PagoService(bus, rechazados);
        new EnvioService(bus);
        new NotificacionService(bus);
    }

    public static void main(String[] args) {
        System.out.println("=== Flujo exitoso (pedido #1) ===");
        {
            EventBus bus = new EventBus();
            buildServices(bus, new HashSet<>(), new HashSet<>());
            new PedidoService(bus) {{ crear(1, "Laptop"); }};
        }

        System.out.println("\n=== Fallo por stock agotado (pedido #2) ===");
        {
            EventBus bus = new EventBus();
            buildServices(bus, Set.of(2), new HashSet<>());
            new PedidoService(bus) {{ crear(2, "GPU"); }};
        }

        System.out.println("\n=== Fallo por pago rechazado (pedido #3) ===");
        {
            EventBus bus = new EventBus();
            buildServices(bus, new HashSet<>(), Set.of(3));
            new PedidoService(bus) {{ crear(3, "Monitor"); }};
        }
    }
}
