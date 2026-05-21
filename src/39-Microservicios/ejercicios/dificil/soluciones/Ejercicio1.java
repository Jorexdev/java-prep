import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio1 {

    enum EventState { PENDING, PUBLISHED }

    static class Pedido {
        final int id;
        final String producto;
        final double precio;

        Pedido(int id, String producto, double precio) {
            this.id = id;
            this.producto = producto;
            this.precio = precio;
        }
    }

    static class OutboxEvent {
        final int id;
        final String type;
        final String payload;
        volatile EventState state = EventState.PENDING;

        OutboxEvent(int id, String type, String payload) {
            this.id = id;
            this.type = type;
            this.payload = payload;
        }
    }

    static class Database {
        final Map<Integer, Pedido> pedidos = new HashMap<>();
        final List<OutboxEvent> outbox = new ArrayList<>();
    }

    static class PedidoService {
        private final Database db;
        private final AtomicInteger pedidoSeq = new AtomicInteger(1);
        private final AtomicInteger eventSeq = new AtomicInteger(1);

        PedidoService(Database db) {
            this.db = db;
        }

        synchronized void crear(String producto, double precio) {
            int pedidoId = pedidoSeq.getAndIncrement();
            Pedido pedido = new Pedido(pedidoId, producto, precio);
            db.pedidos.put(pedidoId, pedido);

            OutboxEvent event = new OutboxEvent(
                eventSeq.getAndIncrement(),
                "PedidoCreado",
                "{\"pedidoId\":" + pedidoId + ",\"producto\":\"" + producto + "\",\"precio\":" + precio + "}"
            );
            db.outbox.add(event);
            System.out.println("[PedidoService] Pedido #" + pedidoId + " creado con evento outbox #" + event.id);
        }
    }

    static class OutboxRelay implements Runnable {
        private final Database db;
        private final List<String> publishedEvents;
        private final CountDownLatch latch;

        OutboxRelay(Database db, List<String> publishedEvents, CountDownLatch latch) {
            this.db = db;
            this.publishedEvents = publishedEvents;
            this.latch = latch;
        }

        @Override
        public void run() {
            while (latch.getCount() > 0) {
                processOutbox();
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }
            processOutbox();
        }

        private void processOutbox() {
            List<OutboxEvent> pending;
            synchronized (db) {
                pending = new ArrayList<>(db.outbox);
            }
            for (OutboxEvent event : pending) {
                if (event.state == EventState.PENDING) {
                    publishedEvents.add(event.type + ":" + event.payload);
                    event.state = EventState.PUBLISHED;
                    System.out.println("[OutboxRelay] Publicado evento #" + event.id + " → " + event.type);
                    latch.countDown();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Database db = new Database();
        PedidoService pedidoService = new PedidoService(db);
        List<String> publishedEvents = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(5);

        String[][] pedidos = {
            {"Laptop", "999.99"},
            {"Mouse", "29.99"},
            {"Teclado", "79.99"},
            {"Monitor", "349.99"},
            {"Auriculares", "149.99"}
        };

        for (String[] p : pedidos) {
            pedidoService.crear(p[0], Double.parseDouble(p[1]));
        }

        Thread relayThread = new Thread(new OutboxRelay(db, publishedEvents, latch));
        relayThread.setDaemon(true);
        relayThread.start();

        boolean completed = latch.await(5, TimeUnit.SECONDS);

        System.out.println("\n=== Verificación ===");
        System.out.println("Eventos publicados: " + publishedEvents.size());
        publishedEvents.forEach(e -> System.out.println("  " + e));
        System.out.println("Correcto (count=5): " + (publishedEvents.size() == 5));
        System.out.println("Latch completado: " + completed);
    }
}
