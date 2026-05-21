import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio4 {

    static class DomainEvent {
        final String type;
        final String aggregateId;
        final Object payload;
        final Instant occurredOn;

        DomainEvent(String type, String aggregateId, Object payload, Instant occurredOn) {
            this.type = type;
            this.aggregateId = aggregateId;
            this.payload = payload;
            this.occurredOn = occurredOn;
        }
    }

    interface EventHandler {
        void handle(DomainEvent e);
    }

    static class EventPublisher {
        private final Map<String, List<EventHandler>> handlers = new HashMap<>();

        void subscribe(String type, EventHandler handler) {
            handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
        }

        void publish(DomainEvent event) {
            List<EventHandler> list = handlers.getOrDefault(event.type, List.of());
            for (EventHandler h : list) {
                h.handle(event);
            }
        }
    }

    public static void main(String[] args) {
        EventPublisher publisher = new EventPublisher();

        publisher.subscribe("PedidoCreado", e ->
            System.out.println("[EMAIL] Pedido creado para aggregate " + e.aggregateId
                + " — datos: " + e.payload));

        publisher.subscribe("PedidoCreado", e ->
            System.out.println("[STOCK] Reservando stock para pedido " + e.aggregateId));

        publisher.subscribe("PedidoCancelado", e ->
            System.out.println("[EMAIL] Pedido cancelado: " + e.aggregateId
                + " — motivo: " + e.payload));

        publisher.subscribe("PedidoCancelado", e ->
            System.out.println("[STOCK] Liberando stock de pedido " + e.aggregateId));

        System.out.println("-- Publicando PedidoCreado --");
        publisher.publish(new DomainEvent("PedidoCreado", "pedido-42",
            "producto=Teclado, cantidad=2", Instant.now()));

        System.out.println("\n-- Publicando PedidoCancelado --");
        publisher.publish(new DomainEvent("PedidoCancelado", "pedido-41",
            "sin stock", Instant.now()));

        System.out.println("\n-- Publicando evento sin handlers --");
        publisher.publish(new DomainEvent("PedidoEnviado", "pedido-40",
            null, Instant.now()));
        System.out.println("(ningún handler registrado para PedidoEnviado)");
    }
}
