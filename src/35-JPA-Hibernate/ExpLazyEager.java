import java.util.ArrayList;
import java.util.List;

// Simula FetchType.LAZY vs EAGER y el problema N+1.
// Cada "SELECT" se imprime y se cuenta para hacer visible el coste real.

// ── Modelos ───────────────────────────────────────────────────────────────────

class OrderItem {
    private final Long id;
    private final String producto;
    private final int cantidad;

    OrderItem(Long id, String producto, int cantidad) {
        this.id        = id;
        this.producto  = producto;
        this.cantidad  = cantidad;
    }

    public Long   getId()       { return id; }
    public String getProducto() { return producto; }
    public int    getCantidad() { return cantidad; }

    @Override
    public String toString() { return producto + "x" + cantidad; }
}

// @Entity
class Order {
    private final Long id;
    private final String cliente;
    // @OneToMany(fetch = FetchType.LAZY)  ← por defecto en JPA para colecciones
    private List<OrderItem> items;  // null = proxy no inicializado (LAZY)

    Order(Long id, String cliente) {
        this.id      = id;
        this.cliente = cliente;
        this.items   = null;
    }

    public Long             getId()      { return id; }
    public String           getCliente() { return cliente; }
    public List<OrderItem>  getItems()   { return items; }
    public void             setItems(List<OrderItem> items) { this.items = items; }

    @Override
    public String toString() { return "Order{id=" + id + ", cliente='" + cliente + "'}"; }
}

// ── Base de datos en memoria con contador de queries ──────────────────────────

class OrderDatabase {

    private static final List<Order> ORDERS_TABLE = List.of(
        new Order(1L, "María López"),
        new Order(2L, "Pedro Ruiz"),
        new Order(3L, "Laura Sanz"),
        new Order(4L, "Carlos Mora"),
        new Order(5L, "Elena Vega")
    );

    private static final List<OrderItem> ITEMS_TABLE = List.of(
        new OrderItem(10L, "Laptop",    1), // order 1
        new OrderItem(11L, "Ratón",     2), // order 1
        new OrderItem(12L, "Teclado",   1), // order 2
        new OrderItem(13L, "Monitor",   1), // order 3
        new OrderItem(14L, "Auriculares",1),// order 3
        new OrderItem(15L, "USB Hub",   3), // order 4
        new OrderItem(16L, "Webcam",    1), // order 5
        new OrderItem(17L, "Micrófono", 1)  // order 5
    );

    // Mapeo orden→items por id (1→[10,11], 2→[12], ...)
    private static final java.util.Map<Long, List<Long>> ORDER_ITEMS_MAP = java.util.Map.of(
        1L, List.of(10L, 11L),
        2L, List.of(12L),
        3L, List.of(13L, 14L),
        4L, List.of(15L),
        5L, List.of(16L, 17L)
    );

    private int queryCount = 0;

    // SELECT * FROM orders
    public List<Order> findAllOrders() {
        queryCount++;
        System.out.println("  [SQL " + queryCount + "] SELECT * FROM orders");
        return ORDERS_TABLE.stream()
            .map(o -> new Order(o.getId(), o.getCliente()))
            .toList();
    }

    // SELECT * FROM order_items WHERE order_id = ? (lazy load individual)
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        queryCount++;
        System.out.println("  [SQL " + queryCount + "] SELECT * FROM order_items WHERE order_id = " + orderId);
        List<Long> itemIds = ORDER_ITEMS_MAP.getOrDefault(orderId, List.of());
        return ITEMS_TABLE.stream()
            .filter(i -> itemIds.contains(i.getId()))
            .toList();
    }

    // SELECT o.*, i.* FROM orders o JOIN order_items i ON i.order_id = o.id
    // En JPA: "SELECT DISTINCT o FROM Order o JOIN FETCH o.items"
    public List<Order> findAllOrdersWithItemsFetch() {
        queryCount++;
        System.out.println("  [SQL " + queryCount + "] SELECT o.*, i.*");
        System.out.println("              FROM orders o JOIN order_items i ON i.order_id = o.id");

        java.util.Map<Long, Order> map = new java.util.LinkedHashMap<>();
        for (Order o : ORDERS_TABLE) {
            Order copy = new Order(o.getId(), o.getCliente());
            copy.setItems(new ArrayList<>());
            map.put(o.getId(), copy);
        }
        for (OrderItem item : ITEMS_TABLE) {
            // Determinar a qué orden pertenece el item
            ORDER_ITEMS_MAP.forEach((orderId, itemIds) -> {
                if (itemIds.contains(item.getId())) {
                    map.get(orderId).getItems().add(item);
                }
            });
        }
        return new ArrayList<>(map.values());
    }

    public int  getQueryCount()  { return queryCount; }
    public void resetQueryCount() { queryCount = 0; }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpLazyEager {
    public static void main(String[] args) {

        OrderDatabase db = new OrderDatabase();

        System.out.println("=== Simulación FetchType.LAZY vs EAGER y N+1 ===\n");

        // ─── ESCENARIO 1: EAGER (items cargados junto con la orden) ──────────
        System.out.println("[ EAGER — items cargados inmediatamente al cargar la orden ]");
        System.out.println("JPA equivalente: @OneToMany(fetch = FetchType.EAGER)\n");
        System.out.println("SQL generado:");

        // EAGER equivale a hacer el JOIN directamente
        List<Order> ordersEager = db.findAllOrdersWithItemsFetch();
        int queriesEager = db.getQueryCount();

        System.out.println("\nResultado:");
        for (Order o : ordersEager) {
            System.out.println("  " + o + " → " + o.getItems());
        }
        System.out.println("Queries totales: " + queriesEager + " (1 sola query con JOIN)");

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── ESCENARIO 2: LAZY sin precaución → N+1 ──────────────────────────
        db.resetQueryCount();

        System.out.println("[ LAZY sin JOIN FETCH — problema N+1 ]");
        System.out.println("JPA equivalente:");
        System.out.println("  List<Order> orders = em.createQuery(\"SELECT o FROM Order o\").getResultList();");
        System.out.println("  for (Order o : orders) { o.getItems().size(); } // ← trigger lazy\n");
        System.out.println("SQL generado:");

        List<Order> ordersLazy = db.findAllOrders();   // 1 query
        int totalItems = 0;
        for (Order o : ordersLazy) {
            List<OrderItem> items = db.findItemsByOrderId(o.getId()); // 1 query por orden
            o.setItems(items);
            totalItems += items.size();
        }

        System.out.println("\nResultado:");
        for (Order o : ordersLazy) {
            System.out.println("  " + o + " → " + o.getItems());
        }
        System.out.println("Queries totales: " + db.getQueryCount()
            + " (1 inicial + " + ordersLazy.size() + " lazy = N+1 = "
            + db.getQueryCount() + ")");

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── ESCENARIO 3: JOIN FETCH — solución al N+1 ───────────────────────
        db.resetQueryCount();

        System.out.println("[ JOIN FETCH — solución al N+1 ]");
        System.out.println("JPA equivalente:");
        System.out.println("  em.createQuery(");
        System.out.println("      \"SELECT DISTINCT o FROM Order o JOIN FETCH o.items\").getResultList();\n");
        System.out.println("SQL generado:");

        List<Order> ordersFetch = db.findAllOrdersWithItemsFetch();

        System.out.println("\nResultado:");
        for (Order o : ordersFetch) {
            System.out.println("  " + o + " → " + o.getItems());
        }
        System.out.println("Queries totales: " + db.getQueryCount() + " (JOIN en una sola query)");

        // ─── Comparativa ─────────────────────────────────────────────────────
        System.out.println("\n" + "─".repeat(60));
        System.out.println("\n[ COMPARATIVA ]");
        System.out.printf("  %-30s %s%n", "Estrategia", "Queries");
        System.out.printf("  %-30s %s%n", "-".repeat(30), "-------");
        System.out.printf("  %-30s %d%n", "EAGER (FetchType.EAGER)",   queriesEager);
        System.out.printf("  %-30s %d%n", "LAZY sin JOIN FETCH (N+1)", 1 + ordersLazy.size());
        System.out.printf("  %-30s %d%n", "LAZY + JOIN FETCH",         1);
        System.out.println("\nNota: con 100 órdenes, N+1 ejecutaría 101 queries.");
        System.out.println("Alternativas: @BatchSize(size=25), @EntityGraph en Spring Data.");
    }
}
