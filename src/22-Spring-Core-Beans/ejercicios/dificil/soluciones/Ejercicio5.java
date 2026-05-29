import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

// Event system simulando @EventListener y ApplicationEventPublisher de Spring

public class Ejercicio5 {

    // ====== Modelo de eventos ======

    static abstract class ApplicationEvent {
        private final Object source;
        private final long timestamp;

        ApplicationEvent(Object source) {
            this.source = source;
            this.timestamp = System.currentTimeMillis();
        }

        Object getSource() { return source; }
        long getTimestamp() { return timestamp; }
        String getType() { return getClass().getSimpleName(); }
    }

    // Eventos concretos
    static class ContextRefreshedEvent extends ApplicationEvent {
        private final int beanCount;
        ContextRefreshedEvent(Object source, int beanCount) {
            super(source);
            this.beanCount = beanCount;
        }
        int getBeanCount() { return beanCount; }
    }

    static class UserCreatedEvent extends ApplicationEvent {
        private final String username;
        private final String email;
        UserCreatedEvent(Object source, String username, String email) {
            super(source);
            this.username = username;
            this.email = email;
        }
        String getUsername() { return username; }
        String getEmail() { return email; }
    }

    static class OrderPlacedEvent extends ApplicationEvent {
        private final String orderId;
        private final double amount;
        OrderPlacedEvent(Object source, String orderId, double amount) {
            super(source);
            this.orderId = orderId;
            this.amount = amount;
        }
        String getOrderId() { return orderId; }
        double getAmount() { return amount; }
    }

    static class SystemShutdownEvent extends ApplicationEvent {
        private final String reason;
        SystemShutdownEvent(Object source, String reason) {
            super(source);
            this.reason = reason;
        }
        String getReason() { return reason; }
    }

    // ====== Listener tipado ======

    // En Spring: @EventListener(UserCreatedEvent.class)
    // Aqui: listener con tipo parametrizado + Consumer
    static class TypedListener<T extends ApplicationEvent> {
        private final Class<T> eventType;
        private final String name;
        private final Consumer<T> handler;
        private int receivedCount = 0;

        TypedListener(Class<T> eventType, String name, Consumer<T> handler) {
            this.eventType = eventType;
            this.name = name;
            this.handler = handler;
        }

        @SuppressWarnings("unchecked")
        boolean onEvent(ApplicationEvent event) {
            if (eventType.isInstance(event)) {
                receivedCount++;
                handler.accept((T) event);
                return true;
            }
            return false;
        }

        String getName() { return name; }
        int getReceivedCount() { return receivedCount; }
    }

    // ====== Publisher ======

    static class ApplicationEventPublisher {
        private final List<TypedListener<?>> listeners = new CopyOnWriteArrayList<>();
        private int totalPublished = 0;

        <T extends ApplicationEvent> void register(TypedListener<T> listener) {
            listeners.add(listener);
            System.out.printf("  [Publisher] listener registrado: %s (escucha: %s)%n",
                    listener.getName(), listener.eventType.getSimpleName());
        }

        void publish(ApplicationEvent event) {
            totalPublished++;
            System.out.printf("%n  [Publisher] publicando %s (fuente: %s)%n",
                    event.getType(), event.getSource());
            int handled = 0;
            for (TypedListener<?> listener : listeners) {
                if (listener.onEvent(event)) handled++;
            }
            System.out.printf("  [Publisher] %d listener(s) procesaron el evento%n", handled);
        }

        void printStats() {
            System.out.println();
            System.out.println("  --- Estadisticas del Publisher ---");
            System.out.printf("  Eventos publicados: %d%n", totalPublished);
            System.out.println("  Conteo por listener:");
            listeners.forEach(l -> System.out.printf("    %-30s recibio: %d evento(s)%n",
                    l.getName(), l.getReceivedCount()));
        }
    }

    // ====== Beans / servicios que usan el publisher ======

    static class EmailNotificationService {
        void handleUserCreated(UserCreatedEvent e) {
            System.out.printf("    [EmailSvc] Enviando email de bienvenida a %s (%s)%n",
                    e.getUsername(), e.getEmail());
        }

        void handleShutdown(SystemShutdownEvent e) {
            System.out.printf("    [EmailSvc] Notificando shutdown al equipo: %s%n", e.getReason());
        }
    }

    static class AuditService {
        private final List<String> auditLog = new ArrayList<>();

        void handleAny(ApplicationEvent e) {
            String entry = String.format("[%d] %s from %s",
                    e.getTimestamp(), e.getType(), e.getSource());
            auditLog.add(entry);
            System.out.printf("    [Audit] registrado: %s%n", entry);
        }

        List<String> getLog() { return auditLog; }
    }

    static class BillingService {
        void handleOrderPlaced(OrderPlacedEvent e) {
            System.out.printf("    [Billing] factura generada: orden=%s, importe=%.2f EUR%n",
                    e.getOrderId(), e.getAmount());
        }
    }

    static class MetricsService {
        private int userCreatedCount = 0;
        private int orderCount = 0;
        private double totalRevenue = 0;

        void handleUserCreated(UserCreatedEvent e) {
            userCreatedCount++;
            System.out.printf("    [Metrics] usuarios creados: %d%n", userCreatedCount);
        }

        void handleOrderPlaced(OrderPlacedEvent e) {
            orderCount++;
            totalRevenue += e.getAmount();
            System.out.printf("    [Metrics] ordenes: %d | ingresos totales: %.2f%n",
                    orderCount, totalRevenue);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Event System: @EventListener simulado con ApplicationEventPublisher ===");
        System.out.println();

        ApplicationEventPublisher publisher = new ApplicationEventPublisher();

        EmailNotificationService emailSvc   = new EmailNotificationService();
        AuditService             auditSvc   = new AuditService();
        BillingService           billingSvc = new BillingService();
        MetricsService           metricsSvc = new MetricsService();

        // Registro de listeners tipados (en Spring: @EventListener en metodo de un @Component)
        System.out.println("[ Registro de listeners ]");
        publisher.register(new TypedListener<>(UserCreatedEvent.class,
                "EmailSvc.handleUserCreated",  emailSvc::handleUserCreated));
        publisher.register(new TypedListener<>(SystemShutdownEvent.class,
                "EmailSvc.handleShutdown",     emailSvc::handleShutdown));
        publisher.register(new TypedListener<>(ApplicationEvent.class,
                "AuditSvc.handleAny",          auditSvc::handleAny));
        publisher.register(new TypedListener<>(OrderPlacedEvent.class,
                "BillingSvc.handleOrder",      billingSvc::handleOrderPlaced));
        publisher.register(new TypedListener<>(UserCreatedEvent.class,
                "MetricsSvc.handleUser",       metricsSvc::handleUserCreated));
        publisher.register(new TypedListener<>(OrderPlacedEvent.class,
                "MetricsSvc.handleOrder",      metricsSvc::handleOrderPlaced));

        // Publicar eventos
        System.out.println();
        System.out.println("[ Publicacion de eventos ]");

        Object ctx = "ApplicationContext";
        publisher.publish(new ContextRefreshedEvent(ctx, 12));
        publisher.publish(new UserCreatedEvent("UserService", "alice", "alice@example.com"));
        publisher.publish(new UserCreatedEvent("UserService", "bob", "bob@example.com"));
        publisher.publish(new OrderPlacedEvent("OrderService", "ORD-001", 149.99));
        publisher.publish(new OrderPlacedEvent("OrderService", "ORD-002", 299.50));
        publisher.publish(new SystemShutdownEvent(ctx, "mantenimiento programado"));

        publisher.printStats();

        System.out.println();
        System.out.println("[ Audit log completo ]");
        auditSvc.getLog().forEach(e -> System.out.println("  " + e));

        System.out.println();
        System.out.println("=== Conclusion ===");
        System.out.println("Los listeners reciben SOLO los eventos del tipo que registraron.");
        System.out.println("AuditService usa ApplicationEvent como base: recibe TODOS los eventos.");
        System.out.println("En Spring: @EventListener(UserCreatedEvent.class) en cualquier @Component.");
        System.out.println("Publisher desacopla al emisor del receptor: ningun servicio conoce a los otros.");
    }
}
