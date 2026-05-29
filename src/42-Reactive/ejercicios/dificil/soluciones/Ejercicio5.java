import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

// Sistema pub/sub con múltiples topics, wildcards y routing thread-safe
public class Ejercicio5 {

    // ======================= EVENTO =======================
    record Evento(String topic, Object payload) {}

    // ======================= SUBSCRIBER =======================
    record Subscriber(String patron, Predicate<String> matcher, BiConsumer<String, Object> handler) {

        // Comprobar si el topic de un evento casa con el patrón de este subscriber
        boolean matches(String topic) {
            return matcher.test(topic);
        }
    }

    // ======================= EVENT BUS =======================
    static class EventBus {
        // CopyOnWriteArrayList para thread-safety en lecturas concurrentes con pocas escrituras
        private final CopyOnWriteArrayList<Subscriber> subscribers = new CopyOnWriteArrayList<>();

        // Registrar subscriber con patrón (soporta wildcard "*" al final: "logs.*")
        void subscribe(String nombre, String patron, BiConsumer<String, Object> handler) {
            Predicate<String> matcher = buildMatcher(patron);
            subscribers.add(new Subscriber(patron, matcher, handler));
            System.out.println("[EventBus] Subscriber '" + nombre + "' registrado para patrón: '" + patron + "'");
        }

        // Publicar un evento: enrutar a todos los subscribers que coincidan con el topic
        synchronized void publish(String topic, Object payload) {
            List<String> destinatarios = new ArrayList<>();
            for (Subscriber sub : subscribers) {
                if (sub.matches(topic)) {
                    sub.handler().accept(topic, payload);
                    destinatarios.add(sub.patron());
                }
            }
            if (destinatarios.isEmpty()) {
                System.out.println("[EventBus] topic='" + topic + "' → sin subscribers");
            }
        }

        // Construir Predicate a partir de un patrón con wildcard "*" al final
        private static Predicate<String> buildMatcher(String patron) {
            if (patron.endsWith(".*")) {
                // "logs.*" → topic debe empezar con "logs."
                String prefix = patron.substring(0, patron.length() - 1); // "logs."
                return topic -> topic.startsWith(prefix);
            } else if (patron.equals("*")) {
                // Wildcard total: casa con cualquier topic
                return topic -> true;
            } else {
                // Coincidencia exacta
                return topic -> topic.equals(patron);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== EventBus con routing por topic y wildcards ===\n");

        EventBus bus = new EventBus();

        // Registrar 4 subscribers con distintos patrones
        bus.subscribe("UserCreatedHandler", "user.created",
            (topic, payload) -> System.out.println("  [UserCreatedHandler] topic=" + topic + " payload=" + payload));

        bus.subscribe("UserEventHandler", "user.*",
            (topic, payload) -> System.out.println("  [UserEventHandler]   topic=" + topic + " payload=" + payload));

        bus.subscribe("LogsHandler", "logs.*",
            (topic, payload) -> System.out.println("  [LogsHandler]        topic=" + topic + " payload=" + payload));

        bus.subscribe("AllEventsAuditor", "*",
            (topic, payload) -> System.out.println("  [AllEventsAuditor]   topic=" + topic + " payload=" + payload));

        System.out.println();

        // Publicar eventos de distintos tipos
        List<Evento> eventos = List.of(
            new Evento("user.created",  "{id:1, nombre:\"Ana\"}"),
            new Evento("user.deleted",  "{id:2}"),
            new Evento("logs.error",    "NullPointerException en UserService"),
            new Evento("logs.info",     "Servidor iniciado en puerto 8080"),
            new Evento("payment.done",  "{importe:99.99, moneda:\"EUR\"}")
        );

        for (Evento evento : eventos) {
            System.out.println("\n[Publish] topic='" + evento.topic() + "'");
            bus.publish(evento.topic(), evento.payload());
        }

        System.out.println();
        System.out.println("=== Resumen de routing ===");
        System.out.println("user.created  → UserCreatedHandler (exacto) + UserEventHandler (user.*) + AllEventsAuditor (*)");
        System.out.println("user.deleted  → UserEventHandler (user.*) + AllEventsAuditor (*)");
        System.out.println("logs.error    → LogsHandler (logs.*) + AllEventsAuditor (*)");
        System.out.println("logs.info     → LogsHandler (logs.*) + AllEventsAuditor (*)");
        System.out.println("payment.done  → AllEventsAuditor (*) solo");

        System.out.println();
        System.out.println("=== Thread-safety ===");
        System.out.println("CopyOnWriteArrayList: iteración thread-safe para lectura concurrente.");
        System.out.println("publish() es synchronized para evitar condiciones de carrera al publicar.");
        System.out.println("En producción: usar RxJava Subject o Reactor Sinks.Many (thread-safe por diseño).");

        // Demo thread-safety: múltiples publishers concurrentes
        System.out.println();
        System.out.println("--- Demo: 5 publishers concurrentes ---");
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            int n = i;
            threads.add(Thread.ofVirtual().start(() ->
                bus.publish("logs.info", "Mensaje concurrente #" + n + " desde hilo=" + Thread.currentThread().getName())
            ));
        }
        for (Thread t : threads) t.join();
        System.out.println("--- Todos los publishers concurrentes completados sin condiciones de carrera ---");
    }
}
