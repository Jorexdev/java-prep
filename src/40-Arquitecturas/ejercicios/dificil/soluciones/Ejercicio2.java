import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio2 {

    // --- Events ---

    interface DomainEvent {}
    record ProductoAñadido(String id, String nombre, int stock) implements DomainEvent {}
    record StockAjustado(String id, int delta) implements DomainEvent {}
    record ProductoEliminado(String id) implements DomainEvent {}

    // --- Commands ---

    interface Command {}
    record AñadirProducto(String id, String nombre, int stock) implements Command {}
    record AjustarStock(String id, int delta) implements Command {}
    record EliminarProducto(String id) implements Command {}

    // --- Event Store ---

    record StoredEvent(String aggregateId, int version, DomainEvent event) {}

    static class EventStore {
        private final List<StoredEvent> events = new ArrayList<>();
        private final Map<String, Integer> versions = new HashMap<>();

        void append(String aggregateId, DomainEvent event) {
            int version = versions.merge(aggregateId, 1, Integer::sum);
            events.add(new StoredEvent(aggregateId, version, event));
        }

        List<DomainEvent> loadAll() {
            return events.stream().map(StoredEvent::event).toList();
        }
    }

    // --- Write side: Inventario ---

    static class Inventario {
        private final Map<String, Integer> stockPorProducto = new HashMap<>();

        void apply(DomainEvent event) {
            switch (event) {
                case ProductoAñadido e  -> stockPorProducto.put(e.id(), e.stock());
                case StockAjustado e    -> stockPorProducto.merge(e.id(), e.delta(), Integer::sum);
                case ProductoEliminado e -> stockPorProducto.remove(e.id());
                default -> {}
            }
        }

        int stock(String id) {
            return stockPorProducto.getOrDefault(id, 0);
        }
    }

    // --- Read side: proyección ---

    static class InventarioView {
        final Map<String, Integer> stockPorProducto = new HashMap<>();

        void on(DomainEvent event) {
            switch (event) {
                case ProductoAñadido e  -> stockPorProducto.put(e.id(), e.stock());
                case StockAjustado e    -> stockPorProducto.merge(e.id(), e.delta(), Integer::sum);
                case ProductoEliminado e -> stockPorProducto.remove(e.id());
                default -> {}
            }
        }

        void reconstruir(List<DomainEvent> events) {
            stockPorProducto.clear();
            events.forEach(this::on);
        }

        @Override
        public String toString() {
            return "InventarioView" + stockPorProducto;
        }
    }

    // --- Projection updater ---

    static class ProjectionUpdater {
        private final InventarioView view;

        ProjectionUpdater(InventarioView view) {
            this.view = view;
        }

        void update(DomainEvent event) {
            view.on(event);
        }
    }

    // --- Command handler ---

    static class CommandHandler {
        private final Inventario inventario;
        private final EventStore eventStore;
        private final ProjectionUpdater updater;

        CommandHandler(Inventario inventario, EventStore eventStore, ProjectionUpdater updater) {
            this.inventario = inventario;
            this.eventStore = eventStore;
            this.updater = updater;
        }

        void handle(Command cmd) {
            DomainEvent event = switch (cmd) {
                case AñadirProducto c   -> new ProductoAñadido(c.id(), c.nombre(), c.stock());
                case AjustarStock c     -> new StockAjustado(c.id(), c.delta());
                case EliminarProducto c -> new ProductoEliminado(c.id());
                default -> throw new IllegalArgumentException("Unknown command");
            };

            inventario.apply(event);
            eventStore.append(getAggregateId(cmd), event);
            updater.update(event);

            System.out.println("CMD handled: " + cmd);
        }

        private String getAggregateId(Command cmd) {
            return switch (cmd) {
                case AñadirProducto c   -> c.id();
                case AjustarStock c     -> c.id();
                case EliminarProducto c -> c.id();
                default -> "inventario";
            };
        }
    }

    public static void main(String[] args) {
        Inventario inventario = new Inventario();
        EventStore eventStore = new EventStore();
        InventarioView view = new InventarioView();
        ProjectionUpdater updater = new ProjectionUpdater(view);
        CommandHandler handler = new CommandHandler(inventario, eventStore, updater);

        System.out.println("--- 8 Commands ---");
        handler.handle(new AñadirProducto("p1", "Teclado", 50));
        handler.handle(new AñadirProducto("p2", "Ratón", 30));
        handler.handle(new AñadirProducto("p3", "Monitor", 10));
        handler.handle(new AjustarStock("p1", -5));
        handler.handle(new AjustarStock("p2", 10));
        handler.handle(new EliminarProducto("p3"));
        handler.handle(new AñadirProducto("p4", "Auriculares", 20));
        handler.handle(new AjustarStock("p4", -3));

        System.out.println("\n--- Proyección actual ---");
        System.out.println(view);

        System.out.println("\n--- Reconstruyendo proyección desde 0 ---");
        InventarioView rebuilt = new InventarioView();
        rebuilt.reconstruir(eventStore.loadAll());
        System.out.println(rebuilt);

        System.out.println("\n--- Consistencia ---");
        System.out.println("Proyecciones iguales: " + view.stockPorProducto.equals(rebuilt.stockPorProducto));
    }
}
