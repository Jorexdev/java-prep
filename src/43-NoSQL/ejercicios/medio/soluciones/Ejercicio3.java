import java.util.*;
import java.util.function.Consumer;

/**
 * Ejercicio 3 (Medio) — Redis pub/sub simplificado
 * Demuestra suscripción a canales y publicación de mensajes.
 */
public class Ejercicio3 {

    static class RedisPubSub {
        private final Map<String, List<Consumer<String>>> subscribers = new HashMap<>();

        void subscribe(String channel, Consumer<String> handler) {
            subscribers.computeIfAbsent(channel, k -> new ArrayList<>()).add(handler);
        }

        int publish(String channel, String message) {
            List<Consumer<String>> handlers = subscribers.getOrDefault(channel, Collections.emptyList());
            handlers.forEach(h -> h.accept(message));
            return handlers.size();  // número de suscriptores que recibieron el mensaje
        }
    }

    public static void main(String[] args) {
        RedisPubSub pubsub = new RedisPubSub();

        // 3 suscriptores en "noticias"
        pubsub.subscribe("noticias", msg -> System.out.println("  [Lector-A/noticias] " + msg));
        pubsub.subscribe("noticias", msg -> System.out.println("  [Lector-B/noticias] " + msg));
        pubsub.subscribe("noticias", msg -> System.out.println("  [Lector-C/noticias] " + msg));

        // 2 suscriptores en "alertas"
        pubsub.subscribe("alertas", msg -> System.out.println("  [Monitor-1/alertas] ⚠ " + msg));
        pubsub.subscribe("alertas", msg -> System.out.println("  [Monitor-2/alertas] ⚠ " + msg));

        System.out.println("Publicando en 'noticias':");
        int recibieron = pubsub.publish("noticias", "Java 21 lanzado con Virtual Threads");
        System.out.println("  → " + recibieron + " suscriptores notificados");

        System.out.println("\nPublicando en 'alertas':");
        recibieron = pubsub.publish("alertas", "CPU al 95% en producción");
        System.out.println("  → " + recibieron + " suscriptores notificados");

        System.out.println("\nPublicando en 'desconocido' (sin suscriptores):");
        recibieron = pubsub.publish("desconocido", "mensaje ignorado");
        System.out.println("  → " + recibieron + " suscriptores notificados");
    }
}
