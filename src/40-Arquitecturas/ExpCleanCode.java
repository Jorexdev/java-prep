import java.util.List;
import java.util.ArrayList;

/**
 * Clean Code en Java: naming, funciones pequeñas, Law of Demeter y Feature Envy.
 * Cada sección muestra violación ("antes") y corrección ("después").
 */
public class ExpCleanCode {

    // ═══════════════════════════════════════════════════════════════
    // 1. NAMING — variables, métodos y clases descriptivos
    // ═══════════════════════════════════════════════════════════════

    // ANTES: nombres crípticos
    static class DataProcessor {
        int d; // elapsed time in days — ¿qué es d?
        String s; // user status

        void proc(List<String> l) {
            for (String x : l) {
                if (x.length() > 0) System.out.println(x);
            }
        }
    }

    // DESPUÉS: nombres que se explican solos
    static class InvoiceProcessor {
        int elapsedDaysInBillingCycle;
        String accountStatus;

        void printNonEmptyInvoices(List<String> invoices) {
            for (String invoice : invoices) {
                if (!invoice.isEmpty()) System.out.println(invoice);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. FUNCIONES PEQUEÑAS CON UNA SOLA RESPONSABILIDAD
    // ═══════════════════════════════════════════════════════════════

    // ANTES: un método que hace demasiado
    static class OrderServiceBad {
        void processOrder(String orderId, String userId, List<String> items) {
            // valida, calcula, persiste y envía email — 4 responsabilidades
            if (orderId == null || userId == null || items.isEmpty()) {
                throw new IllegalArgumentException("Datos inválidos");
            }
            double total = items.size() * 9.99;
            System.out.println("Guardando pedido " + orderId + " total=" + total);
            System.out.println("Enviando email a usuario " + userId);
        }
    }

    // DESPUÉS: cada método hace una sola cosa
    static class OrderServiceGood {
        void processOrder(String orderId, String userId, List<String> items) {
            validateOrder(orderId, userId, items);
            double total = calculateTotal(items);
            persist(orderId, total);
            notifyUser(userId, orderId);
        }

        private void validateOrder(String orderId, String userId, List<String> items) {
            if (orderId == null || userId == null || items.isEmpty())
                throw new IllegalArgumentException("Datos de pedido inválidos");
        }

        private double calculateTotal(List<String> items) {
            return items.size() * 9.99;
        }

        private void persist(String orderId, double total) {
            System.out.println("Persistiendo pedido " + orderId + " total=" + total);
        }

        private void notifyUser(String userId, String orderId) {
            System.out.println("Notificando al usuario " + userId + " sobre pedido " + orderId);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. COMENTARIOS — redundantes vs útiles
    // ═══════════════════════════════════════════════════════════════

    static class CommentExamples {
        // ANTES: comentario que repite el código
        // int age = user.getAge(); // obtiene la edad del usuario

        // DESPUÉS: sin comentario — el nombre lo explica
        // int userAge = user.getAge();

        // Comentario ÚTIL: explica el WHY, no el WHAT
        // Usamos 37 ms de margen para cubrir la latencia máxima del broker Kafka en producción.
        static final int KAFKA_POLL_TIMEOUT_MS = 37;

        // Comentario ÚTIL: alerta de complejidad no obvia
        // ATENCIÓN: el orden de los filtros importa — applySecurity debe ir antes que applyCache
        // porque el caché puede servir respuestas a peticiones no autenticadas.
        void applyMiddleware() {
            applySecurity();
            applyCache();
        }

        private void applySecurity() { System.out.println("[security]"); }
        private void applyCache()    { System.out.println("[cache]");    }
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. LAW OF DEMETER — no hablar con extraños
    // ═══════════════════════════════════════════════════════════════

    static class Address {
        private String city;
        Address(String city) { this.city = city; }
        String getCity() { return city; }
    }

    static class Customer {
        private Address address;
        Customer(Address address) { this.address = address; }
        Address getAddress() { return address; }
        // Método intermediario para evitar la cadena
        String getCity() { return address.getCity(); }
    }

    static class ShippingServiceBad {
        // ANTES: train wreck — accede a internos de internos
        String getCityBad(Customer customer) {
            return customer.getAddress().getCity(); // viola Law of Demeter
        }
    }

    static class ShippingServiceGood {
        // DESPUÉS: delega al objeto directo
        String getCityGood(Customer customer) {
            return customer.getCity(); // customer es el único "amigo"
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 5. FEATURE ENVY — una clase que envidia los datos de otra
    // ═══════════════════════════════════════════════════════════════

    static class OrderItem {
        double price;
        int quantity;
        double taxRate;
        OrderItem(double price, int quantity, double taxRate) {
            this.price = price; this.quantity = quantity; this.taxRate = taxRate;
        }
    }

    // ANTES: OrderCalculator envidia los datos de OrderItem
    static class OrderCalculatorBad {
        double calculateTotal(OrderItem item) {
            // usa solo datos de item — la lógica debería estar en OrderItem
            double subtotal = item.price * item.quantity;
            return subtotal + subtotal * item.taxRate;
        }
    }

    // DESPUÉS: la lógica vive donde están los datos
    static class OrderItemGood {
        private final double price;
        private final int quantity;
        private final double taxRate;

        OrderItemGood(double price, int quantity, double taxRate) {
            this.price = price; this.quantity = quantity; this.taxRate = taxRate;
        }

        double calculateTotal() {
            double subtotal = price * quantity;
            return subtotal + subtotal * taxRate;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        System.out.println("=== Clean Code: Naming ===");
        InvoiceProcessor ip = new InvoiceProcessor();
        ip.elapsedDaysInBillingCycle = 30;
        ip.accountStatus = "ACTIVE";
        List<String> invoices = List.of("INV-001", "", "INV-003");
        ip.printNonEmptyInvoices(invoices);

        System.out.println("\n=== Clean Code: Funciones pequeñas ===");
        new OrderServiceGood().processOrder("ORD-1", "USR-42", List.of("item-a", "item-b"));

        System.out.println("\n=== Clean Code: Comentarios útiles ===");
        new CommentExamples().applyMiddleware();

        System.out.println("\n=== Clean Code: Law of Demeter ===");
        Customer customer = new Customer(new Address("Madrid"));
        System.out.println("Ciudad (bien): " + new ShippingServiceGood().getCityGood(customer));

        System.out.println("\n=== Clean Code: Feature Envy corregido ===");
        OrderItemGood item = new OrderItemGood(10.0, 3, 0.21);
        System.out.printf("Total con IVA: %.2f%n", item.calculateTotal());
    }
}
