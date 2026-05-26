import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

// Simula el sistema de eventos de Spring: ApplicationEventPublisher / @EventListener.
// Permite desacoplar productores de consumidores sin dependencia directa.
public class ExpEventPublisher {

    // ── Eventos ───────────────────────────────────────────────────────────────

    // Equivalente a extender ApplicationEvent
    static class UserRegisteredEvent {
        final String userId;
        final String email;
        final Instant occurredAt = Instant.now();

        UserRegisteredEvent(String userId, String email) {
            this.userId = userId;
            this.email  = email;
        }
    }

    static class OrderPlacedEvent {
        final String orderId;
        final double amount;

        OrderPlacedEvent(String orderId, double amount) {
            this.orderId = orderId;
            this.amount  = amount;
        }
    }

    // ── Bus de eventos ────────────────────────────────────────────────────────

    // Equivalente a ApplicationEventPublisher + ApplicationContext
    static class EventBus {
        // Mapa tipo → lista de listeners (en Spring: tipo del parámetro de @EventListener)
        private final Map<Class<?>, List<Consumer<Object>>> listeners = new HashMap<>();
        private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(2);

        @SuppressWarnings("unchecked")
        <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
            listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                     .add(e -> listener.accept((T) e));
        }

        // Publicación síncrona — equivale a ApplicationEventPublisher.publishEvent()
        void publish(Object event) {
            List<Consumer<Object>> handlers = listeners.getOrDefault(event.getClass(), List.of());
            System.out.println("[EventBus] publicando " + event.getClass().getSimpleName()
                + " → " + handlers.size() + " listener(s)");
            for (Consumer<Object> handler : handlers) {
                handler.accept(event);
            }
        }

        // Publicación asíncrona — equivale a @EventListener + @Async en Spring
        void publishAsync(Object event) {
            asyncExecutor.submit(() -> publish(event));
        }

        void shutdown() throws InterruptedException {
            asyncExecutor.shutdown();
            asyncExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    // @Component
    static class EmailListener {
        // @EventListener
        void onUserRegistered(UserRegisteredEvent event) {
            System.out.println("  [EmailListener] Enviando bienvenida a " + event.email);
        }
    }

    // @Component
    static class AuditListener {
        // @EventListener
        void onUserRegistered(UserRegisteredEvent event) {
            System.out.println("  [AuditListener] Registro auditado: userId=" + event.userId
                + " at=" + event.occurredAt);
        }

        // @EventListener
        void onOrderPlaced(OrderPlacedEvent event) {
            System.out.println("  [AuditListener] Pedido auditado: " + event.orderId
                + " por $" + event.amount);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        EventBus bus = new EventBus();

        EmailListener emailListener = new EmailListener();
        AuditListener auditListener = new AuditListener();

        // Registro de listeners — Spring hace esto automáticamente con @EventListener
        bus.subscribe(UserRegisteredEvent.class, emailListener::onUserRegistered);
        bus.subscribe(UserRegisteredEvent.class, auditListener::onUserRegistered);
        bus.subscribe(OrderPlacedEvent.class,    auditListener::onOrderPlaced);

        System.out.println("=== Publicación síncrona ===");
        bus.publish(new UserRegisteredEvent("u-42", "jorex@example.com"));

        System.out.println("\n=== Publicación asíncrona (@Async) ===");
        // Los listeners se ejecutan en otro hilo; el publicador no bloquea
        bus.publishAsync(new OrderPlacedEvent("ord-99", 149.90));

        bus.shutdown();
        System.out.println("\n=== Ventaja ===");
        System.out.println("El componente que registra al usuario no sabe nada de emails ni auditoría.");
        System.out.println("Nuevos listeners = cero cambios en el publicador.");
    }
}
