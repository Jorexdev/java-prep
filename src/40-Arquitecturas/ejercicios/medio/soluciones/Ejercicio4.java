import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ejercicio4 {

    static class DomainEvent {
        final String type;
        final String aggregateId;

        DomainEvent(String type, String aggregateId) {
            this.type = type;
            this.aggregateId = aggregateId;
        }
    }

    interface EventHandler {
        void handle(DomainEvent e);
    }

    interface EventBus {
        void subscribe(String type, EventHandler handler);
        void publish(DomainEvent event);
    }

    static class SyncEventBus implements EventBus {
        private final Map<String, List<EventHandler>> handlers = new HashMap<>();

        @Override
        public void subscribe(String type, EventHandler handler) {
            handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
        }

        @Override
        public void publish(DomainEvent event) {
            List<EventHandler> list = handlers.getOrDefault(event.type, List.of());
            for (EventHandler h : list) {
                h.handle(event);
            }
        }
    }

    static class AsyncEventBus implements EventBus {
        private final Map<String, List<EventHandler>> handlers = new HashMap<>();
        private final ExecutorService executor = Executors.newCachedThreadPool();

        @Override
        public void subscribe(String type, EventHandler handler) {
            handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
        }

        @Override
        public void publish(DomainEvent event) {
            List<EventHandler> list = handlers.getOrDefault(event.type, List.of());
            for (EventHandler h : list) {
                executor.submit(() -> h.handle(event));
            }
        }

        void shutdown() {
            executor.shutdown();
        }
    }

    static EventHandler notificacionHandler() {
        return e -> System.out.println("  [NOTIF] Pedido creado: " + e.aggregateId
            + " (thread: " + Thread.currentThread().getName() + ")");
    }

    static EventHandler handlerLento() {
        return e -> {
            try {
                System.out.println("  [LENTO] Procesando " + e.aggregateId + "...");
                Thread.sleep(200);
                System.out.println("  [LENTO] Listo " + e.aggregateId);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };
    }

    static class PedidoService {
        private final EventBus eventBus;

        PedidoService(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        void crearPedido(String pedidoId) {
            System.out.println("crearPedido(" + pedidoId + ") iniciado");
            eventBus.publish(new DomainEvent("PedidoCreado", pedidoId));
            System.out.println("crearPedido(" + pedidoId + ") retornó");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== SyncEventBus ===");
        SyncEventBus syncBus = new SyncEventBus();
        syncBus.subscribe("PedidoCreado", notificacionHandler());
        syncBus.subscribe("PedidoCreado", handlerLento());
        PedidoService syncService = new PedidoService(syncBus);

        long t0 = System.currentTimeMillis();
        syncService.crearPedido("pedido-1");
        long syncMs = System.currentTimeMillis() - t0;
        System.out.println("Tiempo total (sync): " + syncMs + "ms (esperado >=200ms)\n");

        System.out.println("=== AsyncEventBus ===");
        AsyncEventBus asyncBus = new AsyncEventBus();
        asyncBus.subscribe("PedidoCreado", notificacionHandler());
        asyncBus.subscribe("PedidoCreado", handlerLento());
        PedidoService asyncService = new PedidoService(asyncBus);

        long t1 = System.currentTimeMillis();
        asyncService.crearPedido("pedido-2");
        long asyncMs = System.currentTimeMillis() - t1;
        System.out.println("Tiempo hasta retorno (async): " + asyncMs + "ms (esperado <50ms)");

        Thread.sleep(300);
        asyncBus.shutdown();
        System.out.println("\nAsync completado en background.");
    }
}
