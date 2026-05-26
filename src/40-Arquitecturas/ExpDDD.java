import java.util.*;

/**
 * Simulación de Domain-Driven Design (DDD) táctico con Java puro.
 *
 * Patrones demostrados:
 *  - Aggregate: Order con OrderItems, invariantes de negocio
 *  - Value Object: Money (immutable, igualdad por valor, no por identidad)
 *  - Domain Event: OrderPlaced emitido cuando se confirma el pedido
 *  - Repository interface: definida en el dominio, implementada fuera
 *  - Domain Service: PricingService (lógica que no pertenece a una sola entidad)
 */
public class ExpDDD {

    // ═══════════════════════════════════════════════════════════════
    // VALUE OBJECTS — immutables, igualdad por valor
    // ═══════════════════════════════════════════════════════════════

    // Money: no tiene identidad propia. 10€ == 10€ siempre.
    static final class Money {
        private final double amount;
        private final String currency;

        private Money(double amount, String currency) {
            if (amount < 0) throw new IllegalArgumentException("El importe no puede ser negativo");
            Objects.requireNonNull(currency, "currency requerida");
            this.amount = amount;
            this.currency = currency;
        }

        static Money of(double amount, String currency) {
            return new Money(amount, currency);
        }

        Money add(Money other) {
            assertSameCurrency(other);
            return new Money(this.amount + other.amount, this.currency);
        }

        Money multiply(int factor) {
            return new Money(this.amount * factor, this.currency);
        }

        boolean greaterThan(Money other) {
            assertSameCurrency(other);
            return this.amount > other.amount;
        }

        private void assertSameCurrency(Money other) {
            if (!this.currency.equals(other.currency)) {
                throw new IllegalArgumentException("No se pueden operar monedas distintas: "
                        + this.currency + " vs " + other.currency);
            }
        }

        double amount() { return amount; }
        String currency() { return currency; }

        // Value Object: igualdad por valor
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Money m)) return false;
            return Double.compare(amount, m.amount) == 0 && currency.equals(m.currency);
        }

        @Override
        public int hashCode() { return Objects.hash(amount, currency); }

        @Override
        public String toString() { return String.format("%.2f %s", amount, currency); }
    }

    record ProductId(String value) {
        ProductId { Objects.requireNonNull(value, "productId requerido"); }
        @Override public String toString() { return value; }
    }

    record OrderId(String value) {
        OrderId { Objects.requireNonNull(value, "orderId requerido"); }
        @Override public String toString() { return value; }
    }

    // ═══════════════════════════════════════════════════════════════
    // DOMAIN EVENTS — hechos del dominio, en tiempo pasado
    // ═══════════════════════════════════════════════════════════════

    sealed interface DomainEvent permits OrderPlaced, OrderItemAdded {}

    record OrderPlaced(OrderId orderId, String customerId, Money total) implements DomainEvent {}
    record OrderItemAdded(OrderId orderId, ProductId productId, int quantity) implements DomainEvent {}

    // ═══════════════════════════════════════════════════════════════
    // AGGREGATE ROOT: Order
    // Invariante: no se puede añadir items a un pedido confirmado
    // ═══════════════════════════════════════════════════════════════

    static class Order {
        private final OrderId orderId;
        private final String customerId;
        private final List<OrderItem> items = new ArrayList<>();
        private String status = "PENDING";
        // Los eventos se acumulan y se publican al persistir (outbox pattern)
        private final List<DomainEvent> domainEvents = new ArrayList<>();

        Order(OrderId orderId, String customerId) {
            this.orderId = orderId;
            this.customerId = customerId;
        }

        // Comando: añadir item al pedido
        void addItem(ProductId productId, int quantity, Money unitPrice) {
            // Invariante de negocio: no se puede modificar un pedido confirmado
            if (!"PENDING".equals(status)) {
                throw new IllegalStateException(
                        "No se pueden añadir items a un pedido con estado '" + status + "'");
            }
            if (quantity <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva");

            // Buscar si ya existe el item y sumar cantidad (regla de negocio)
            Optional<OrderItem> existing = items.stream()
                    .filter(i -> i.productId().equals(productId))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().addQuantity(quantity);
            } else {
                items.add(new OrderItem(productId, quantity, unitPrice));
            }

            domainEvents.add(new OrderItemAdded(orderId, productId, quantity));
            System.out.printf("  [Order %s] Item añadido: %s x%d @ %s%n",
                    orderId, productId, quantity, unitPrice);
        }

        // Comando: confirmar el pedido
        void place() {
            if (items.isEmpty()) throw new IllegalStateException("No se puede confirmar un pedido vacío");
            if (!"PENDING".equals(status)) throw new IllegalStateException("El pedido ya fue procesado");

            this.status = "PLACED";
            domainEvents.add(new OrderPlaced(orderId, customerId, total()));
            System.out.printf("  [Order %s] Pedido confirmado — total: %s%n", orderId, total());
        }

        Money total() {
            return items.stream()
                    .map(i -> i.unitPrice().multiply(i.quantity()))
                    .reduce(Money.of(0, "EUR"), Money::add);
        }

        OrderId orderId()     { return orderId; }
        String customerId()   { return customerId; }
        String status()       { return status; }
        List<OrderItem> items() { return Collections.unmodifiableList(items); }

        // Los eventos se extraen (y se limpian) al persistir
        List<DomainEvent> pullEvents() {
            List<DomainEvent> result = new ArrayList<>(domainEvents);
            domainEvents.clear();
            return result;
        }

        @Override
        public String toString() {
            return String.format("Order{id=%s, customer='%s', items=%d, total=%s, status='%s'}",
                    orderId, customerId, items.size(), total(), status);
        }
    }

    // OrderItem es parte interna del aggregate, no es un aggregate root
    static class OrderItem {
        private final ProductId productId;
        private int quantity;
        private final Money unitPrice;

        OrderItem(ProductId productId, int quantity, Money unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        void addQuantity(int extra) { this.quantity += extra; }

        ProductId productId() { return productId; }
        int quantity()        { return quantity; }
        Money unitPrice()     { return unitPrice; }
    }

    // ═══════════════════════════════════════════════════════════════
    // REPOSITORY — interfaz definida en el dominio
    // ═══════════════════════════════════════════════════════════════

    interface OrderRepository {
        void save(Order order);
        Optional<Order> findById(OrderId orderId);
    }

    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<String, Order> store = new LinkedHashMap<>();
        private final List<DomainEvent> publishedEvents = new ArrayList<>();

        @Override
        public void save(Order order) {
            store.put(order.orderId().value(), order);
            // Publicar eventos de dominio al persistir
            List<DomainEvent> events = order.pullEvents();
            publishedEvents.addAll(events);
            if (!events.isEmpty()) {
                events.forEach(e ->
                    System.out.printf("  [Repository] Evento publicado: %s%n",
                            e.getClass().getSimpleName()));
            }
        }

        @Override
        public Optional<Order> findById(OrderId orderId) {
            return Optional.ofNullable(store.get(orderId.value()));
        }

        List<DomainEvent> getPublishedEvents() { return Collections.unmodifiableList(publishedEvents); }
    }

    // ═══════════════════════════════════════════════════════════════
    // DOMAIN SERVICE — lógica que no pertenece a una sola entidad
    // ═══════════════════════════════════════════════════════════════

    // PricingService coordina múltiples entidades para calcular precio final
    // No encaja en Order ni en Product → Domain Service
    static class PricingService {
        private static final double BULK_DISCOUNT_THRESHOLD = 5;
        private static final double BULK_DISCOUNT_RATE = 0.10; // 10%

        // Aplica descuento por volumen: si hay items con cantidad > threshold
        Money calculateFinalPrice(Order order) {
            Money base = order.total();
            boolean hasBulkItem = order.items().stream()
                    .anyMatch(i -> i.quantity() >= BULK_DISCOUNT_THRESHOLD);

            if (hasBulkItem) {
                double discount = base.amount() * BULK_DISCOUNT_RATE;
                Money discounted = Money.of(base.amount() - discount, base.currency());
                System.out.printf("  [PricingService] Descuento por volumen aplicado: %s → %s%n",
                        base, discounted);
                return discounted;
            }
            System.out.printf("  [PricingService] Sin descuento: %s%n", base);
            return base;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EVENT HANDLER — otro aggregate reacciona al evento del primero
    // ═══════════════════════════════════════════════════════════════

    // InventoryAggregate reacciona a OrderPlaced reduciendo stock
    static class InventoryAggregate {
        private final Map<String, Integer> stock = new HashMap<>();

        InventoryAggregate() {
            stock.put("laptop",   10);
            stock.put("mouse",    50);
            stock.put("teclado",  25);
        }

        // Maneja el evento OrderPlaced: reservar stock
        void handleOrderPlaced(OrderPlaced event, Order order) {
            System.out.printf("%n  [InventoryAggregate] Procesando OrderPlaced para %s%n", event.orderId());
            for (OrderItem item : order.items()) {
                String productId = item.productId().value();
                int available = stock.getOrDefault(productId, 0);
                if (available < item.quantity()) {
                    System.out.printf("    ✗ Stock insuficiente para '%s': disponible=%d, solicitado=%d%n",
                            productId, available, item.quantity());
                } else {
                    stock.put(productId, available - item.quantity());
                    System.out.printf("    ✓ Stock reservado '%s': %d → %d%n",
                            productId, available, stock.get(productId));
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  DDD TÁCTICO — Java puro");
        System.out.println("═".repeat(65));

        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        PricingService pricing = new PricingService();
        InventoryAggregate inventory = new InventoryAggregate();

        // ── Demo 1: Value Object ───────────────────────────────────────
        System.out.println("\n── Demo 1: Value Object (Money) ──");
        Money precio1 = Money.of(10.0, "EUR");
        Money precio2 = Money.of(10.0, "EUR");
        Money precio3 = Money.of(20.0, "EUR");

        System.out.printf("  10€ == 10€: %b (value equality, no reference equality)%n",
                precio1.equals(precio2));
        System.out.printf("  suma: %s + %s = %s%n", precio1, precio2, precio1.add(precio2));
        System.out.printf("  multiplicar: %s x 3 = %s%n", precio1, precio1.multiply(3));

        // ── Demo 2: Aggregate con invariantes ─────────────────────────
        System.out.println("\n── Demo 2: Aggregate (Order) con invariantes de negocio ──");
        Order order = new Order(new OrderId("ORD-001"), "customer-42");

        order.addItem(new ProductId("laptop"), 1, Money.of(999.99, "EUR"));
        order.addItem(new ProductId("mouse"),  2, Money.of(29.99,  "EUR"));
        order.addItem(new ProductId("laptop"), 1, Money.of(999.99, "EUR")); // se suma al existente

        System.out.printf("  Total antes de confirmar: %s%n", order.total());

        // Confirmar el pedido
        repo.save(order); // guardamos antes de place para que los eventos ItemAdded se registren
        order.place();
        repo.save(order); // guardamos con el evento OrderPlaced

        // ── Intentar añadir item tras confirmar → invariante ──────────
        System.out.println("\n  Intentando añadir item tras confirmar:");
        try {
            order.addItem(new ProductId("teclado"), 1, Money.of(79.99, "EUR"));
        } catch (IllegalStateException e) {
            System.out.printf("  ✓ Invariante aplicada: %s%n", e.getMessage());
        }

        // ── Demo 3: Domain Service ─────────────────────────────────────
        System.out.println("\n── Demo 3: Domain Service (PricingService) ──");
        Order bulkOrder = new Order(new OrderId("ORD-002"), "customer-big");
        bulkOrder.addItem(new ProductId("mouse"), 10, Money.of(29.99, "EUR")); // >= 5 → descuento
        pricing.calculateFinalPrice(bulkOrder);

        Order smallOrder = new Order(new OrderId("ORD-003"), "customer-small");
        smallOrder.addItem(new ProductId("teclado"), 1, Money.of(79.99, "EUR")); // < 5 → sin descuento
        pricing.calculateFinalPrice(smallOrder);

        // ── Demo 4: Domain Events y comunicación entre aggregates ──────
        System.out.println("\n── Demo 4: Domain Events → comunicación entre aggregates ──");
        System.out.println("  Eventos publicados durante el flujo:");
        repo.getPublishedEvents().forEach(e ->
            System.out.printf("    → %s%n", e));

        // El evento OrderPlaced dispara lógica en InventoryAggregate
        repo.getPublishedEvents().stream()
                .filter(e -> e instanceof OrderPlaced)
                .map(e -> (OrderPlaced) e)
                .forEach(e -> {
                    repo.findById(e.orderId()).ifPresent(o -> inventory.handleOrderPlaced(e, o));
                });

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN DDD TÁCTICO");
        System.out.println("═".repeat(65));
        System.out.println("  Aggregate: cluster de objetos con una raíz, transacciones internas");
        System.out.println("  Value Object: inmutable, igualdad por valor (Money, Address, ProductId)");
        System.out.println("  Domain Event: hecho pasado que otros aggregates pueden escuchar");
        System.out.println("  Repository: interfaz en dominio, implementación fuera");
        System.out.println("  Domain Service: lógica que no encaja en un solo aggregate");
        System.out.println("═".repeat(65));
    }
}
