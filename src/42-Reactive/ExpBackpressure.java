import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

// Simulación de backpressure: subscriber solicita N items, producer respeta el ritmo
// Estrategias de overflow: BUFFER, DROP, ERROR
public class ExpBackpressure {

    // ======================= SUBSCRIPTION =======================
    interface Subscription {
        void request(long n);
        void cancel();
    }

    // ======================= ESTRATEGIAS =======================
    enum OverflowStrategy { BUFFER, DROP, ERROR }

    // ======================= PUBLISHER CON BACKPRESSURE =======================
    static class BackpressurePublisher {
        private final int[] items;
        private final OverflowStrategy strategy;
        private final int bufferCapacity;

        BackpressurePublisher(int[] items, OverflowStrategy strategy, int bufferCapacity) {
            this.items = items;
            this.strategy = strategy;
            this.bufferCapacity = bufferCapacity;
        }

        void subscribe(BackpressureSubscriber subscriber) {
            // Buffer de items pendientes de entrega
            Deque<Integer> buffer = new ArrayDeque<>();
            AtomicLong requested = new AtomicLong(0);
            AtomicInteger dropped = new AtomicInteger(0);
            AtomicInteger errors = new AtomicInteger(0);
            boolean[] cancelled = {false};

            Subscription subscription = new Subscription() {
                @Override
                public void request(long n) {
                    requested.addAndGet(n);
                    // Entregar items del buffer según lo solicitado
                    drainBuffer(subscriber, buffer, requested, cancelled);
                }

                @Override
                public void cancel() {
                    cancelled[0] = true;
                    System.out.println("  [Subscription] Cancelada");
                }
            };

            subscriber.onSubscribe(subscription);

            // Producción de items: respetar la estrategia según el estado del buffer
            for (int item : items) {
                if (cancelled[0]) break;

                if (requested.get() > 0) {
                    // El subscriber está esperando: entregar directamente
                    requested.decrementAndGet();
                    subscriber.onNext(item);
                } else {
                    // El subscriber no ha pedido más: aplicar estrategia de overflow
                    switch (strategy) {
                        case BUFFER -> {
                            if (buffer.size() < bufferCapacity) {
                                buffer.addLast(item);
                                System.out.println("  [Buffer] Item " + item + " en buffer (size=" + buffer.size() + ")");
                            } else {
                                System.out.println("  [Buffer OVERFLOW] Item " + item + " DROPPED (buffer lleno=" + bufferCapacity + ")");
                                dropped.incrementAndGet();
                            }
                        }
                        case DROP -> {
                            dropped.incrementAndGet();
                            System.out.println("  [DROP] Item " + item + " descartado (total dropped=" + dropped.get() + ")");
                        }
                        case ERROR -> {
                            errors.incrementAndGet();
                            subscriber.onError(new IllegalStateException(
                                "Overflow: item " + item + " no puede ser procesado (estrategia=ERROR)"));
                            return;
                        }
                    }
                }
            }

            // Vaciar buffer restante si hay demanda pendiente
            drainBuffer(subscriber, buffer, requested, cancelled);

            if (!cancelled[0]) {
                subscriber.onComplete(dropped.get());
            }
        }

        private void drainBuffer(BackpressureSubscriber sub, Deque<Integer> buffer,
                                  AtomicLong requested, boolean[] cancelled) {
            while (!cancelled[0] && requested.get() > 0 && !buffer.isEmpty()) {
                requested.decrementAndGet();
                sub.onNext(buffer.pollFirst());
            }
        }
    }

    // ======================= SUBSCRIBER CON BACKPRESSURE =======================
    static class BackpressureSubscriber {
        private final String nombre;
        private final int batchSize; // cuántos items solicitar por vez
        private Subscription subscription;
        private int receivedCount = 0;

        BackpressureSubscriber(String nombre, int batchSize) {
            this.nombre = nombre;
            this.batchSize = batchSize;
        }

        void onSubscribe(Subscription s) {
            this.subscription = s;
            System.out.println("[" + nombre + "] Suscrito. Solicitando primer batch de " + batchSize);
            s.request(batchSize); // solicitar primer lote
        }

        void onNext(int item) {
            receivedCount++;
            System.out.println("[" + nombre + "] onNext(" + item + ") — recibidos=" + receivedCount);

            // Cuando se han consumido todos del batch, pedir el siguiente
            if (receivedCount % batchSize == 0) {
                System.out.println("[" + nombre + "] Batch de " + batchSize + " completado. Pidiendo más...");
                subscription.request(batchSize);
            }
        }

        void onError(Throwable t) {
            System.out.println("[" + nombre + "] ERROR: " + t.getMessage());
        }

        void onComplete(int dropped) {
            System.out.println("[" + nombre + "] COMPLETADO. Total recibidos=" + receivedCount + ", dropped=" + dropped);
        }
    }

    public static void main(String[] args) {

        int[] items = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        // ---- Estrategia BUFFER ----
        System.out.println("=== Estrategia BUFFER (capacidad=3) ===");
        System.out.println("Producer emite 10 items, Subscriber pide en batches de 2\n");

        BackpressurePublisher bufferPub =
            new BackpressurePublisher(items, OverflowStrategy.BUFFER, 3);
        BackpressureSubscriber bufferSub =
            new BackpressureSubscriber("Sub-BUFFER", 2);
        bufferPub.subscribe(bufferSub);

        System.out.println();

        // ---- Estrategia DROP ----
        System.out.println("=== Estrategia DROP ===");
        System.out.println("Producer emite 10 items, Subscriber pide solo 3 (el resto se descarta)\n");

        BackpressurePublisher dropPub =
            new BackpressurePublisher(items, OverflowStrategy.DROP, 0);
        BackpressureSubscriber dropSub =
            new BackpressureSubscriber("Sub-DROP", 3) {
                @Override
                void onNext(int item) {
                    // Sobrescribir para NO pedir más después del batch inicial
                    System.out.println("[Sub-DROP] onNext(" + item + ")");
                }
                @Override
                void onComplete(int dropped) {
                    System.out.println("[Sub-DROP] COMPLETADO. Items dropped=" + dropped);
                }
            };
        dropPub.subscribe(dropSub);

        System.out.println();

        // ---- Estrategia ERROR ----
        System.out.println("=== Estrategia ERROR ===");
        System.out.println("Publisher emite rápido, Subscriber solicita 0 extra → ERROR en overflow\n");

        BackpressurePublisher errorPub =
            new BackpressurePublisher(new int[]{100, 200, 300}, OverflowStrategy.ERROR, 0);
        // Este subscriber no pide más después del primer item
        errorPub.subscribe(new BackpressureSubscriber("Sub-ERROR", 1) {
            @Override
            void onNext(int item) {
                System.out.println("[Sub-ERROR] onNext(" + item + ") — NO pide más");
                // Intencionalmente NO llama request() para provocar overflow en el siguiente
            }
        });

        System.out.println();
        System.out.println("=== Resumen de estrategias ===");
        System.out.println("BUFFER  — almacena items hasta capacidad máxima, luego descarta");
        System.out.println("DROP    — descarta inmediatamente lo que no cabe");
        System.out.println("ERROR   — propaga OverflowException al subscriber");
        System.out.println("LATEST  — (no simulado) solo guarda el item más reciente");
    }
}
