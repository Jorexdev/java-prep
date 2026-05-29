import java.util.*;

public class Ejercicio5 {

    // --- EventBus pub/sub en memoria ---

    @FunctionalInterface
    interface EventHandler {
        void handle(Object event);
    }

    static class EventBus {
        private final Map<Class<?>, List<EventHandler>> handlers = new HashMap<>();

        <T> void subscribe(Class<T> eventType, EventHandler handler) {
            handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
        }

        void publish(Object event) {
            List<EventHandler> subs = handlers.getOrDefault(event.getClass(), List.of());
            for (EventHandler h : subs) {
                h.handle(event);
            }
        }
    }

    // --- Eventos del dominio ---

    record PedidoCreado   (String pedidoId, String producto, int cantidad)  {}
    record StockReservado (String pedidoId)                                  {}
    record StockInsuficiente(String pedidoId, String motivo)                 {}
    record PagoAprobado   (String pedidoId)                                  {}
    record PagoRechazado  (String pedidoId, String motivo)                   {}
    record EnvioCreado    (String pedidoId)                                  {}

    // --- Eventos de compensación ---

    record PedidoCancelado (String pedidoId, String motivo)  {}
    record StockLiberado   (String pedidoId)                 {}
    record ReembolsoEmitido(String pedidoId)                 {}

    // --- Servicios ---

    static class PedidoService {
        private final EventBus bus;
        private final Set<String> pedidosActivos = new LinkedHashSet<>();

        PedidoService(EventBus bus) {
            this.bus = bus;
            // Compensaciones
            bus.subscribe(PedidoCancelado.class, e -> {
                PedidoCancelado ev = (PedidoCancelado) e;
                if (pedidosActivos.remove(ev.pedidoId())) {
                    System.out.println("[PedidoService] Pedido " + ev.pedidoId()
                        + " cancelado — motivo: " + ev.motivo());
                }
            });
        }

        void crearPedido(String pedidoId, String producto, int cantidad) {
            pedidosActivos.add(pedidoId);
            System.out.println("[PedidoService] Pedido creado: " + pedidoId
                + " (" + producto + " x" + cantidad + ")");
            bus.publish(new PedidoCreado(pedidoId, producto, cantidad));
        }
    }

    static class InventarioService {
        private final EventBus bus;
        private final Map<String, Integer> stock = new HashMap<>();
        private final Set<String> reservas = new HashSet<>();
        // simulación: qué pedidos deben fallar
        private final Set<String> pedidosConStockInsuficiente;

        InventarioService(EventBus bus, Set<String> pedidosConStockInsuficiente) {
            this.bus = bus;
            this.pedidosConStockInsuficiente = pedidosConStockInsuficiente;
            stock.put("laptops", 5);

            bus.subscribe(PedidoCreado.class, e -> {
                PedidoCreado ev = (PedidoCreado) e;
                if (pedidosConStockInsuficiente.contains(ev.pedidoId())) {
                    System.out.println("[InventarioService] Stock insuficiente para " + ev.pedidoId());
                    bus.publish(new StockInsuficiente(ev.pedidoId(), "sin stock disponible"));
                    bus.publish(new PedidoCancelado(ev.pedidoId(), "stock insuficiente"));
                } else {
                    reservas.add(ev.pedidoId());
                    System.out.println("[InventarioService] Stock reservado para " + ev.pedidoId());
                    bus.publish(new StockReservado(ev.pedidoId()));
                }
            });

            // Compensación
            bus.subscribe(ReembolsoEmitido.class, e -> {
                ReembolsoEmitido ev = (ReembolsoEmitido) e;
                if (reservas.remove(ev.pedidoId())) {
                    System.out.println("[InventarioService] Stock liberado para " + ev.pedidoId()
                        + " (tras reembolso)");
                    bus.publish(new StockLiberado(ev.pedidoId()));
                }
            });
        }
    }

    static class PagoService {
        private final EventBus bus;
        // simulación: qué pedidos deben fallar el pago
        private final Set<String> pedidosConPagoRechazado;

        PagoService(EventBus bus, Set<String> pedidosConPagoRechazado) {
            this.bus = bus;
            this.pedidosConPagoRechazado = pedidosConPagoRechazado;

            bus.subscribe(StockReservado.class, e -> {
                StockReservado ev = (StockReservado) e;
                if (pedidosConPagoRechazado.contains(ev.pedidoId())) {
                    System.out.println("[PagoService] Pago rechazado para " + ev.pedidoId());
                    bus.publish(new PagoRechazado(ev.pedidoId(), "fondos insuficientes"));
                    // Compensación: liberar stock y cancelar pedido
                    bus.publish(new ReembolsoEmitido(ev.pedidoId()));
                    bus.publish(new PedidoCancelado(ev.pedidoId(), "pago rechazado"));
                } else {
                    System.out.println("[PagoService] Pago aprobado para " + ev.pedidoId());
                    bus.publish(new PagoAprobado(ev.pedidoId()));
                }
            });
        }
    }

    static class EnvioService {
        private final EventBus bus;

        EnvioService(EventBus bus) {
            this.bus = bus;
            bus.subscribe(PagoAprobado.class, e -> {
                PagoAprobado ev = (PagoAprobado) e;
                System.out.println("[EnvioService] Envío creado para " + ev.pedidoId());
                bus.publish(new EnvioCreado(ev.pedidoId()));
            });
        }
    }

    static class NotificacionService {
        NotificacionService(EventBus bus) {
            bus.subscribe(EnvioCreado.class, e -> {
                System.out.println("[NotificacionService] Compra exitosa — pedido "
                    + ((EnvioCreado) e).pedidoId() + " en camino!");
            });
            bus.subscribe(PedidoCancelado.class, e -> {
                PedidoCancelado ev = (PedidoCancelado) e;
                System.out.println("[NotificacionService] Pedido " + ev.pedidoId()
                    + " cancelado — " + ev.motivo());
            });
        }
    }

    public static void main(String[] args) {
        System.out.println("======== FLUJO 1: Compra exitosa ========");
        {
            EventBus bus = new EventBus();
            PedidoService pedidos = new PedidoService(bus);
            new InventarioService(bus, Set.of());
            new PagoService(bus, Set.of());
            new EnvioService(bus);
            new NotificacionService(bus);
            pedidos.crearPedido("PED-001", "Laptop Pro", 1);
        }

        System.out.println("\n======== FLUJO 2: Fallo en PagoService (compensación) ========");
        {
            EventBus bus = new EventBus();
            PedidoService pedidos = new PedidoService(bus);
            new InventarioService(bus, Set.of());
            new PagoService(bus, Set.of("PED-002")); // PED-002 falla el pago
            new EnvioService(bus);
            new NotificacionService(bus);
            pedidos.crearPedido("PED-002", "Laptop Pro", 1);
        }

        System.out.println("\n======== FLUJO 3: Fallo en InventarioService (compensación) ========");
        {
            EventBus bus = new EventBus();
            PedidoService pedidos = new PedidoService(bus);
            new InventarioService(bus, Set.of("PED-003")); // PED-003 sin stock
            new PagoService(bus, Set.of());
            new EnvioService(bus);
            new NotificacionService(bus);
            pedidos.crearPedido("PED-003", "Laptop Pro", 10);
        }
    }
}
