import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Hot publisher: subscribers que se unen tarde solo ven eventos futuros
public class Ejercicio1 {

    static class HotPublisher<T> {
        private final List<Consumer<T>> subscribers = new ArrayList<>();
        private int totalEmitidos = 0;

        public void subscribe(String nombre, Consumer<T> onEvent) {
            subscribers.add(onEvent);
            System.out.println("[" + nombre + "] Suscrito en t=" + totalEmitidos
                + " (recibirá solo eventos a partir de este punto)");
        }

        public void emit(T item) {
            totalEmitidos++;
            System.out.println("[Publisher] Emitiendo evento #" + totalEmitidos + ": " + item
                + " → " + subscribers.size() + " subscriber(s)");
            for (Consumer<T> sub : subscribers) {
                sub.accept(item);
            }
        }

        public int getTotalEmitidos() {
            return totalEmitidos;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Hot Publisher ===\n");

        HotPublisher<String> publisher = new HotPublisher<>();

        // Contadores de eventos recibidos por cada subscriber
        int[] contSub1 = {0};
        int[] contSub2 = {0};

        // Sub-1 se suscribe ANTES de los primeros 3 eventos
        publisher.subscribe("Sub-1", e -> {
            contSub1[0]++;
            System.out.println("  [Sub-1] recibido: " + e);
        });

        System.out.println();
        System.out.println("--- Emitiendo primeros 3 eventos (solo Sub-1 está suscrito) ---");
        publisher.emit("evento-A");
        publisher.emit("evento-B");
        publisher.emit("evento-C");

        System.out.println();
        // Sub-2 se suscribe DESPUÉS de los primeros 3 eventos
        publisher.subscribe("Sub-2", e -> {
            contSub2[0]++;
            System.out.println("  [Sub-2] recibido: " + e);
        });

        System.out.println();
        System.out.println("--- Emitiendo 3 eventos más (Sub-1 y Sub-2 suscritos) ---");
        publisher.emit("evento-D");
        publisher.emit("evento-E");
        publisher.emit("evento-F");

        System.out.println();
        System.out.println("=== Resumen ===");
        System.out.println("Sub-1 recibió: " + contSub1[0] + " eventos (A, B, C, D, E, F)");
        System.out.println("Sub-2 recibió: " + contSub2[0] + " eventos (solo D, E, F)");
        System.out.println();
        System.out.println("=== Hot vs Cold ===");
        System.out.println("Hot publisher: comparte la misma fuente de eventos entre todos los subscribers.");
        System.out.println("  Suscribirse tarde = perderte los eventos pasados.");
        System.out.println("  Ejemplo real: radio, WebSocket broadcast, sensor de temperatura.");
        System.out.println();
        System.out.println("Cold publisher: cada subscriber recibe la secuencia completa desde el inicio.");
        System.out.println("  Suscribirse tarde = igual que suscribirse pronto, misma experiencia.");
        System.out.println("  Ejemplo real: Flux.just(), lectura de fichero, consulta HTTP.");
    }
}
