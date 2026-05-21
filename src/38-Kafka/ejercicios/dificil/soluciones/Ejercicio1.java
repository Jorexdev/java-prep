import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio1 {

    enum OutboxStatus { PENDING, PUBLISHED }

    static class Pedido {
        final int id;
        final String producto;

        Pedido(int id, String producto) {
            this.id = id;
            this.producto = producto;
        }

        @Override
        public String toString() {
            return "Pedido{id=" + id + ", producto='" + producto + "'}";
        }
    }

    static class OutboxEvent {
        final int id;
        final String payload;
        OutboxStatus status;

        OutboxEvent(int id, String payload) {
            this.id = id;
            this.payload = payload;
            this.status = OutboxStatus.PENDING;
        }
    }

    static class Database {
        final Map<Integer, Pedido> pedidos = new HashMap<>();
        final List<OutboxEvent> outbox = new ArrayList<>();
    }

    static class PedidoService {
        private final Database db;
        private final AtomicInteger pedidoIdSeq = new AtomicInteger(1);
        private final AtomicInteger eventIdSeq = new AtomicInteger(1);

        PedidoService(Database db) {
            this.db = db;
        }

        synchronized Pedido crear(String producto) {
            int pedidoId = pedidoIdSeq.getAndIncrement();
            Pedido pedido = new Pedido(pedidoId, producto);
            db.pedidos.put(pedidoId, pedido);
            OutboxEvent event = new OutboxEvent(eventIdSeq.getAndIncrement(), "PedidoCreado:" + pedido);
            db.outbox.add(event);
            System.out.println("[TX] creado " + pedido + " + outbox event pendiente");
            return pedido;
        }
    }

    static class OutboxPublisher implements Runnable {
        private final Database db;
        private final List<String> publishedEvents;
        volatile boolean running = true;

        OutboxPublisher(Database db, List<String> publishedEvents) {
            this.db = db;
            this.publishedEvents = publishedEvents;
        }

        @Override
        public void run() {
            while (running) {
                synchronized (db) {
                    for (OutboxEvent event : db.outbox) {
                        if (event.status == OutboxStatus.PENDING) {
                            publishedEvents.add(event.payload);
                            event.status = OutboxStatus.PUBLISHED;
                            System.out.println("[PUBLISHER] publicado: " + event.payload);
                        }
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Database db = new Database();
        PedidoService service = new PedidoService(db);
        List<String> publishedEvents = new ArrayList<>();

        OutboxPublisher publisher = new OutboxPublisher(db, publishedEvents);
        Thread publisherThread = new Thread(publisher);
        publisherThread.start();

        String[] productos = {"laptop", "teclado", "monitor", "raton"};
        for (String producto : productos) {
            service.crear(producto);
        }

        Thread.sleep(300);
        publisher.running = false;
        publisherThread.join();

        System.out.println("\n[RESULTADO] pedidos creados: " + db.pedidos.size());
        System.out.println("[RESULTADO] eventos publicados: " + publishedEvents.size());
        boolean allPublished = db.outbox.stream().allMatch(e -> e.status == OutboxStatus.PUBLISHED);
        System.out.println("[VERIFICACIÓN] todos publicados: " + allPublished);
    }
}
