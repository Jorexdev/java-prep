import java.util.*;

/**
 * Simulación de Clean Architecture con Java puro.
 *
 * Capas (de más interna a más externa):
 *  1. Entities (reglas de negocio universales)
 *  2. Use Cases (reglas de negocio de la aplicación)
 *  3. Interface Adapters (controllers, presenters, repo implementations)
 *  4. Frameworks & Drivers (BD, Web, etc.)
 *
 * Regla de dependencia: el código fuente solo puede apuntar hacia dentro.
 * Los círculos exteriores NUNCA son conocidos por los interiores.
 */
public class ExpCleanArchitecture {

    // ═══════════════════════════════════════════════════════════════
    // CAPA 1: ENTITIES — reglas de negocio que no dependen de nada externo
    // ═══════════════════════════════════════════════════════════════

    // Entity: tiene identidad propia e invariantes de negocio
    static class Order {
        private final String orderId;
        private final String customerId;
        private final List<OrderItem> items = new ArrayList<>();
        private String status = "DRAFT";

        Order(String orderId, String customerId) {
            Objects.requireNonNull(orderId, "orderId requerido");
            Objects.requireNonNull(customerId, "customerId requerido");
            this.orderId = orderId;
            this.customerId = customerId;
        }

        // Invariante: solo se pueden añadir items en estado DRAFT
        void addItem(String productId, int quantity, double unitPrice) {
            if (!"DRAFT".equals(status)) {
                throw new IllegalStateException("No se pueden añadir items a un pedido " + status);
            }
            if (quantity <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva");
            if (unitPrice < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
            items.add(new OrderItem(productId, quantity, unitPrice));
        }

        // Invariante: solo se puede confirmar un pedido con items
        void confirm() {
            if (items.isEmpty()) throw new IllegalStateException("No se puede confirmar un pedido vacío");
            if (!"DRAFT".equals(status)) throw new IllegalStateException("El pedido ya fue procesado");
            this.status = "CONFIRMED";
        }

        double total() {
            return items.stream().mapToDouble(i -> i.quantity() * i.unitPrice()).sum();
        }

        String orderId()    { return orderId; }
        String customerId() { return customerId; }
        String status()     { return status; }
        List<OrderItem> items() { return Collections.unmodifiableList(items); }

        @Override
        public String toString() {
            return String.format("Order{id='%s', customer='%s', items=%d, total=%.2f, status='%s'}",
                    orderId, customerId, items.size(), total(), status);
        }
    }

    record OrderItem(String productId, int quantity, double unitPrice) {}

    // ═══════════════════════════════════════════════════════════════
    // CAPA 2: USE CASES — orquesta entidades, define puertos
    // ═══════════════════════════════════════════════════════════════

    // Puerto de salida (Output Port): interfaz que el use case DEFINE
    // Las implementaciones (capa 3) dependen de esta interfaz, no al revés
    interface OrderRepository {
        void save(Order order);
        Optional<Order> findById(String orderId);
        List<Order> findByCustomer(String customerId);
    }

    // Puerto de salida: notificaciones
    interface OrderNotifier {
        void notifyConfirmed(Order order);
    }

    // Puerto de entrada (Input Port): interfaz del use case (opcional pero recomendado)
    interface CreateOrderUseCase {
        Order execute(String customerId, List<OrderItem> items);
    }

    interface ConfirmOrderUseCase {
        Order execute(String orderId);
    }

    // Implementación del use case: solo depende de interfaces internas
    static class CreateOrderInteractor implements CreateOrderUseCase {
        private final OrderRepository repository;
        private int counter = 1;

        CreateOrderInteractor(OrderRepository repository) {
            this.repository = repository;
        }

        @Override
        public Order execute(String customerId, List<OrderItem> items) {
            String orderId = "ORD-" + String.format("%03d", counter++);
            Order order = new Order(orderId, customerId);
            items.forEach(item -> order.addItem(item.productId(), item.quantity(), item.unitPrice()));
            repository.save(order);
            System.out.printf("  [CreateOrder] Pedido creado: %s%n", order);
            return order;
        }
    }

    static class ConfirmOrderInteractor implements ConfirmOrderUseCase {
        private final OrderRepository repository;
        private final OrderNotifier notifier;

        ConfirmOrderInteractor(OrderRepository repository, OrderNotifier notifier) {
            this.repository = repository;
            this.notifier = notifier;
        }

        @Override
        public Order execute(String orderId) {
            Order order = repository.findById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Order no encontrada: " + orderId));
            order.confirm();
            repository.save(order);
            notifier.notifyConfirmed(order);
            System.out.printf("  [ConfirmOrder] Pedido confirmado: %s%n", order);
            return order;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CAPA 3: INTERFACE ADAPTERS — controllers, presenters, repo impl
    // ═══════════════════════════════════════════════════════════════

    // Adaptador de entrada: Controller HTTP simulado
    static class OrderController {
        private final CreateOrderUseCase createOrder;
        private final ConfirmOrderUseCase confirmOrder;

        OrderController(CreateOrderUseCase createOrder, ConfirmOrderUseCase confirmOrder) {
            this.createOrder = createOrder;
            this.confirmOrder = confirmOrder;
        }

        // Simula: POST /orders
        String handleCreate(String customerId, List<OrderItem> items) {
            System.out.printf("%n  [OrderController] POST /orders {customer='%s', items=%d}%n",
                    customerId, items.size());
            Order order = createOrder.execute(customerId, items);
            return String.format("{\"orderId\":\"%s\",\"status\":\"%s\",\"total\":%.2f}",
                    order.orderId(), order.status(), order.total());
        }

        // Simula: PUT /orders/{id}/confirm
        String handleConfirm(String orderId) {
            System.out.printf("%n  [OrderController] PUT /orders/%s/confirm%n", orderId);
            Order order = confirmOrder.execute(orderId);
            return String.format("{\"orderId\":\"%s\",\"status\":\"%s\"}", order.orderId(), order.status());
        }
    }

    // Adaptador de salida: Repositorio en memoria (simula JPA/JDBC)
    // Esta clase DEPENDE de OrderRepository (capa 2), no al revés → Dependency Rule OK
    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<String, Order> store = new LinkedHashMap<>();

        @Override
        public void save(Order order) {
            store.put(order.orderId(), order);
            System.out.printf("  [InMemoryRepo] Guardado: %s%n", order.orderId());
        }

        @Override
        public Optional<Order> findById(String orderId) {
            return Optional.ofNullable(store.get(orderId));
        }

        @Override
        public List<Order> findByCustomer(String customerId) {
            return store.values().stream()
                    .filter(o -> o.customerId().equals(customerId))
                    .toList();
        }
    }

    // Adaptador de salida simulado para tests — Mock sin framework
    static class MockOrderRepository implements OrderRepository {
        final List<Order> savedOrders = new ArrayList<>();

        @Override public void save(Order order) { savedOrders.add(order); }
        @Override public Optional<Order> findById(String orderId) {
            return savedOrders.stream().filter(o -> o.orderId().equals(orderId)).findFirst();
        }
        @Override public List<Order> findByCustomer(String customerId) {
            return savedOrders.stream().filter(o -> o.customerId().equals(customerId)).toList();
        }
    }

    // Adaptador de salida: notificador (en producción podría ser email/Kafka)
    static class ConsoleOrderNotifier implements OrderNotifier {
        @Override
        public void notifyConfirmed(Order order) {
            System.out.printf("  [Notifier] Email enviado a customer='%s': pedido '%s' confirmado (%.2f€)%n",
                    order.customerId(), order.orderId(), order.total());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN: demuestra la arquitectura y los tests sin framework
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  CLEAN ARCHITECTURE — Java puro");
        System.out.println("═".repeat(65));

        // ── Demo 1: flujo completo con adaptadores reales ──────────────
        System.out.println("\n── Demo 1: flujo completo ──");

        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        ConsoleOrderNotifier notifier = new ConsoleOrderNotifier();
        CreateOrderUseCase createUC = new CreateOrderInteractor(repo);
        ConfirmOrderUseCase confirmUC = new ConfirmOrderInteractor(repo, notifier);
        OrderController controller = new OrderController(createUC, confirmUC);

        List<OrderItem> items = List.of(
                new OrderItem("laptop", 1, 999.99),
                new OrderItem("mouse",  2, 29.99)
        );

        String createResp = controller.handleCreate("customer-42", items);
        System.out.printf("  → HTTP Response: %s%n", createResp);

        String confirmResp = controller.handleConfirm("ORD-001");
        System.out.printf("  → HTTP Response: %s%n", confirmResp);

        // ── Demo 2: test del use case SIN framework, SIN Spring, SIN BD ─
        System.out.println("\n── Demo 2: test unitario del use case (mock repo, sin framework) ──");

        MockOrderRepository mockRepo = new MockOrderRepository();
        OrderNotifier mockNotifier = order ->
                System.out.printf("  [MockNotifier] notificado: %s%n", order.orderId());

        CreateOrderUseCase createUC2 = new CreateOrderInteractor(mockRepo);
        ConfirmOrderUseCase confirmUC2 = new ConfirmOrderInteractor(mockRepo, mockNotifier);

        Order created = createUC2.execute("test-customer", List.of(
                new OrderItem("teclado", 1, 79.99)
        ));
        System.out.printf("  Creado: %s%n", created);

        Order confirmed = confirmUC2.execute(created.orderId());
        System.out.printf("  Confirmado: status='%s'%n", confirmed.status());

        // Verificar con assertions manuales
        assert "CONFIRMED".equals(confirmed.status()) : "El pedido debe estar CONFIRMED";
        assert mockRepo.savedOrders.size() == 2 : "Deben haberse guardado 2 veces (create + confirm)";
        System.out.println("  ✓ Assertions pasadas — test unitario sin ningún framework");

        // ── Demo 3: invariantes de la entidad ─────────────────────────
        System.out.println("\n── Demo 3: invariantes de la entidad ──");

        Order order = new Order("ORD-TEST", "cust-1");
        order.addItem("producto-x", 2, 10.0);

        try {
            order.confirm();
            System.out.println("  ✓ Pedido confirmado correctamente");
            order.addItem("producto-y", 1, 5.0); // debe lanzar excepción
        } catch (IllegalStateException e) {
            System.out.printf("  ✓ Invariante aplicada: %s%n", e.getMessage());
        }

        try {
            Order vacio = new Order("ORD-EMPTY", "cust-2");
            vacio.confirm(); // debe lanzar excepción
        } catch (IllegalStateException e) {
            System.out.printf("  ✓ Invariante aplicada: %s%n", e.getMessage());
        }

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN CLEAN ARCHITECTURE");
        System.out.println("═".repeat(65));
        System.out.println("  Entities   → reglas de negocio puras, no dependen de nada");
        System.out.println("  Use Cases  → orquestan entidades, definen interfaces (ports)");
        System.out.println("  Adapters   → implementan los ports, conocen el dominio");
        System.out.println("  Frameworks → capas exteriores (Spring, JPA, HTTP), cambiables");
        System.out.println("  Regla clave: las dependencias solo apuntan hacia el centro");
        System.out.println("  Beneficio principal: el dominio es testeable sin ningún framework");
        System.out.println("═".repeat(65));
    }
}
