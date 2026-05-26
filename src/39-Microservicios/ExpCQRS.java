import java.util.*;
import java.util.function.*;

/**
 * Simulación de CQRS (Command Query Responsibility Segregation) con Java puro.
 *
 * Flujo:
 *  Command → CommandBus → CommandHandler → muta estado + emite evento
 *  Evento  → EventHandler → actualiza read model (proyección)
 *  Query   → QueryBus → QueryHandler → lee del read model
 *
 * La ventana de inconsistencia: entre el comando y la actualización
 * del read model existe un gap donde la query puede devolver datos obsoletos.
 */
public class ExpCQRS {

    // ─────────────────────────────────────────────
    // COMANDOS — intención de mutar estado
    // ─────────────────────────────────────────────

    sealed interface Command permits CreateOrderCommand, ConfirmOrderCommand, CancelOrderCommand {}

    record CreateOrderCommand(String orderId, String product, int quantity) implements Command {}
    record ConfirmOrderCommand(String orderId) implements Command {}
    record CancelOrderCommand(String orderId, String reason) implements Command {}

    // ─────────────────────────────────────────────
    // EVENTOS DE DOMINIO — hechos ocurridos
    // ─────────────────────────────────────────────

    sealed interface DomainEvent permits OrderCreated, OrderConfirmed, OrderCancelled {}

    record OrderCreated(String orderId, String product, int quantity) implements DomainEvent {}
    record OrderConfirmed(String orderId) implements DomainEvent {}
    record OrderCancelled(String orderId, String reason) implements DomainEvent {}

    // ─────────────────────────────────────────────
    // WRITE MODEL — el aggregate
    // ─────────────────────────────────────────────

    static class Order {
        private final String orderId;
        private String product;
        private int quantity;
        private String status;

        Order(String orderId, String product, int quantity) {
            this.orderId = orderId;
            this.product = product;
            this.quantity = quantity;
            this.status = "PENDING";
        }

        void confirm() {
            if (!"PENDING".equals(status)) throw new IllegalStateException("Solo se puede confirmar un pedido PENDING");
            this.status = "CONFIRMED";
        }

        void cancel(String reason) {
            if ("SHIPPED".equals(status)) throw new IllegalStateException("No se puede cancelar un pedido SHIPPED");
            this.status = "CANCELLED";
        }

        String orderId() { return orderId; }
        String status()  { return status; }

        @Override
        public String toString() {
            return String.format("Order{id='%s', product='%s', qty=%d, status='%s'}",
                    orderId, product, quantity, status);
        }
    }

    // ─────────────────────────────────────────────
    // READ MODEL — proyección desnormalizada para queries
    // ─────────────────────────────────────────────

    // El read model puede tener campos calculados, joins pre-computados, etc.
    record OrderSummary(String orderId, String product, int quantity,
                        String status, String displayLabel) {}

    static class OrderReadModel {
        private final Map<String, OrderSummary> store = new LinkedHashMap<>();

        void upsert(OrderSummary summary) {
            store.put(summary.orderId(), summary);
            System.out.printf("  [ReadModel] Actualizado: %s → status='%s' label='%s'%n",
                    summary.orderId(), summary.status(), summary.displayLabel());
        }

        Optional<OrderSummary> findById(String orderId) {
            return Optional.ofNullable(store.get(orderId));
        }

        List<OrderSummary> findByStatus(String status) {
            return store.values().stream()
                    .filter(s -> s.status().equals(status))
                    .toList();
        }
    }

    // ─────────────────────────────────────────────
    // EVENT HANDLER — actualiza el read model con eventos
    // ─────────────────────────────────────────────

    static class OrderEventHandler {
        private final OrderReadModel readModel;

        OrderEventHandler(OrderReadModel readModel) {
            this.readModel = readModel;
        }

        void handle(DomainEvent event) {
            switch (event) {
                case OrderCreated e -> readModel.upsert(new OrderSummary(
                        e.orderId(), e.product(), e.quantity(),
                        "PENDING", "Pedido pendiente de confirmación"));
                case OrderConfirmed e -> {
                    // Actualizar solo el status en el read model
                    readModel.findById(e.orderId()).ifPresent(existing ->
                        readModel.upsert(new OrderSummary(
                                existing.orderId(), existing.product(), existing.quantity(),
                                "CONFIRMED", "Pedido confirmado y en proceso")));
                }
                case OrderCancelled e -> {
                    readModel.findById(e.orderId()).ifPresent(existing ->
                        readModel.upsert(new OrderSummary(
                                existing.orderId(), existing.product(), existing.quantity(),
                                "CANCELLED", "Pedido cancelado")));
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // COMMAND BUS
    // ─────────────────────────────────────────────

    static class CommandBus {
        // write store: orderId → Order
        private final Map<String, Order> writeStore = new LinkedHashMap<>();
        private final List<Consumer<DomainEvent>> eventListeners = new ArrayList<>();

        void registerEventListener(Consumer<DomainEvent> listener) {
            eventListeners.add(listener);
        }

        void dispatch(Command command) {
            System.out.printf("%n[CommandBus] Dispatching: %s%n", command.getClass().getSimpleName());
            List<DomainEvent> events = switch (command) {
                case CreateOrderCommand c -> handleCreate(c);
                case ConfirmOrderCommand c -> handleConfirm(c);
                case CancelOrderCommand c -> handleCancel(c);
            };

            // Publicar eventos a los listeners (Event Handlers actualizan el read model)
            events.forEach(e -> {
                System.out.printf("  [CommandBus] Emitiendo evento: %s%n", e.getClass().getSimpleName());
                eventListeners.forEach(l -> l.accept(e));
            });
        }

        private List<DomainEvent> handleCreate(CreateOrderCommand cmd) {
            Order order = new Order(cmd.orderId(), cmd.product(), cmd.quantity());
            writeStore.put(order.orderId(), order);
            System.out.printf("  [WriteModel] Creado: %s%n", order);
            return List.of(new OrderCreated(cmd.orderId(), cmd.product(), cmd.quantity()));
        }

        private List<DomainEvent> handleConfirm(ConfirmOrderCommand cmd) {
            Order order = writeStore.get(cmd.orderId());
            if (order == null) throw new NoSuchElementException("Order no encontrada: " + cmd.orderId());
            order.confirm();
            System.out.printf("  [WriteModel] Confirmado: %s%n", order);
            return List.of(new OrderConfirmed(cmd.orderId()));
        }

        private List<DomainEvent> handleCancel(CancelOrderCommand cmd) {
            Order order = writeStore.get(cmd.orderId());
            if (order == null) throw new NoSuchElementException("Order no encontrada: " + cmd.orderId());
            order.cancel(cmd.reason());
            System.out.printf("  [WriteModel] Cancelado: %s%n", order);
            return List.of(new OrderCancelled(cmd.orderId(), cmd.reason()));
        }

        Order getFromWriteModel(String orderId) {
            return writeStore.get(orderId);
        }
    }

    // ─────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────

    sealed interface Query permits GetOrderQuery, ListOrdersByStatusQuery {}

    record GetOrderQuery(String orderId) implements Query {}
    record ListOrdersByStatusQuery(String status) implements Query {}

    // ─────────────────────────────────────────────
    // QUERY BUS
    // ─────────────────────────────────────────────

    static class QueryBus {
        private final OrderReadModel readModel;

        QueryBus(OrderReadModel readModel) {
            this.readModel = readModel;
        }

        Object dispatch(Query query) {
            System.out.printf("%n[QueryBus] Dispatching: %s%n", query.getClass().getSimpleName());
            return switch (query) {
                case GetOrderQuery q -> {
                    Optional<OrderSummary> result = readModel.findById(q.orderId());
                    System.out.printf("  [ReadModel] Query result para '%s': %s%n",
                            q.orderId(), result.map(OrderSummary::toString).orElse("NOT FOUND"));
                    yield result.orElse(null);
                }
                case ListOrdersByStatusQuery q -> {
                    List<OrderSummary> results = readModel.findByStatus(q.status());
                    System.out.printf("  [ReadModel] Query status='%s': %d resultados → %s%n",
                            q.status(), results.size(),
                            results.stream().map(OrderSummary::orderId).toList());
                    yield results;
                }
            };
        }
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  CQRS — Command Query Responsibility Segregation");
        System.out.println("  Java puro, sin frameworks");
        System.out.println("═".repeat(65));

        // Setup
        OrderReadModel readModel = new OrderReadModel();
        OrderEventHandler eventHandler = new OrderEventHandler(readModel);
        CommandBus commandBus = new CommandBus();
        commandBus.registerEventListener(eventHandler::handle); // conectar write → read
        QueryBus queryBus = new QueryBus(readModel);

        // ── Demo: CreateOrderCommand ───────────────────────────────────
        System.out.println("\n══ DEMO 1: Crear un pedido ══");
        commandBus.dispatch(new CreateOrderCommand("ORD-001", "laptop", 2));

        System.out.println("\n  → Query inmediata después del comando:");
        queryBus.dispatch(new GetOrderQuery("ORD-001"));

        // ── Demo: ConfirmOrderCommand ──────────────────────────────────
        System.out.println("\n══ DEMO 2: Confirmar el pedido ══");
        commandBus.dispatch(new ConfirmOrderCommand("ORD-001"));
        queryBus.dispatch(new GetOrderQuery("ORD-001"));

        // ── Demo: múltiples pedidos y query por status ─────────────────
        System.out.println("\n══ DEMO 3: Múltiples pedidos ══");
        commandBus.dispatch(new CreateOrderCommand("ORD-002", "mouse", 5));
        commandBus.dispatch(new CreateOrderCommand("ORD-003", "teclado", 1));
        commandBus.dispatch(new CancelOrderCommand("ORD-003", "stock insuficiente"));

        System.out.println("\n  → Query: listar pedidos PENDING:");
        queryBus.dispatch(new ListOrdersByStatusQuery("PENDING"));

        System.out.println("\n  → Query: listar pedidos CANCELLED:");
        queryBus.dispatch(new ListOrdersByStatusQuery("CANCELLED"));

        // ── Demo: ventana de inconsistencia ───────────────────────────
        System.out.println("\n══ DEMO 4: Ventana de inconsistencia ══");
        System.out.println("  En producción el EventHandler puede ser asíncrono (via Kafka).");
        System.out.println("  Mientras el evento viaja por la red, write model y read model divergen:");
        System.out.println("  → Write model: ORD-001 = CONFIRMED");
        System.out.println("  → Read model puede aún mostrar PENDING durante milisegundos");
        System.out.println("  → Solución: el comando devuelve el estado esperado al cliente,");
        System.out.println("    y la UI aplica optimistic update sin esperar la query.");

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN CQRS");
        System.out.println("═".repeat(65));
        System.out.println("  Commands → mutan estado, emiten eventos → actualizan read model");
        System.out.println("  Queries  → leen del read model (sin tocar el write model)");
        System.out.println("  Beneficio: read model optimizado para cada caso de uso (sin JOINs)");
        System.out.println("  Coste: eventual consistency entre write y read model");
        System.out.println("═".repeat(65));
    }
}
