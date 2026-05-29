import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

// Backpressure: subscriber lento (1 item/s) y publisher rápido (1 item/20ms)
// Gestión con buffer de capacidad 5; overflow → DROP con aviso
public class Ejercicio2 {

    static class SlowSubscriber {
        private final int processDelayMs;
        private int received = 0;
        private int dropped = 0;
        private final Deque<Integer> buffer = new ArrayDeque<>();
        private final int bufferCapacity;

        SlowSubscriber(int processDelayMs, int bufferCapacity) {
            this.processDelayMs = processDelayMs;
            this.bufferCapacity = bufferCapacity;
        }

        // Llamado por el publisher para ofrecer un item
        synchronized void offer(int item) {
            if (buffer.size() < bufferCapacity) {
                buffer.addLast(item);
                System.out.println("  [Buffer] Item " + item + " → buffer (size=" + buffer.size() + "/" + bufferCapacity + ")");
            } else {
                dropped++;
                System.out.println("  [DROP] Item " + item + " descartado. Buffer lleno. Total dropped=" + dropped);
            }
        }

        // Procesar un item del buffer simulando trabajo lento
        void processNext() throws InterruptedException {
            Integer item;
            synchronized (this) {
                item = buffer.pollFirst();
            }
            if (item != null) {
                Thread.sleep(processDelayMs); // procesamiento lento
                received++;
                System.out.println("  [Subscriber] Procesado: " + item + " (recibidos=" + received + ")");
            }
        }

        int getReceived() { return received; }
        int getDropped()  { return dropped; }
        boolean bufferEmpty() { synchronized (this) { return buffer.isEmpty(); } }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Backpressure: Producer rápido (20ms/item) vs Subscriber lento (200ms/item) ===\n");

        int numItems       = 10;
        int produceDelayMs = 20;   // 20ms entre cada item del producer
        int consumeDelayMs = 200;  // 200ms por item del consumer
        int bufferCapacity = 5;

        SlowSubscriber subscriber = new SlowSubscriber(consumeDelayMs, bufferCapacity);

        long start = System.currentTimeMillis();

        // Thread del producer: emite un item cada 20ms
        Thread producer = Thread.ofVirtual().start(() -> {
            for (int i = 1; i <= numItems; i++) {
                System.out.println("[Producer] Emitiendo item " + i);
                subscriber.offer(i);
                try { Thread.sleep(produceDelayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            System.out.println("[Producer] Completado. " + numItems + " items emitidos.");
        });

        // Thread del consumer: procesa un item cada 200ms
        Thread consumer = Thread.ofVirtual().start(() -> {
            try {
                // Procesar mientras el producer no haya terminado o haya items en buffer
                while (!producer.getState().equals(Thread.State.TERMINATED) || !subscriber.bufferEmpty()) {
                    subscriber.processNext();
                    if (subscriber.bufferEmpty() && producer.isAlive()) {
                        Thread.sleep(10); // pequeña espera si buffer vacío
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.join();
        consumer.join(3000); // esperar máximo 3s al consumer

        long elapsed = System.currentTimeMillis() - start;

        System.out.println();
        System.out.println("=== Resultados ===");
        System.out.printf("Items producidos : %d%n", numItems);
        System.out.printf("Items recibidos  : %d%n", subscriber.getReceived());
        System.out.printf("Items descartados: %d%n", subscriber.getDropped());
        System.out.printf("Tiempo total     : %dms%n", elapsed);
        System.out.println();
        System.out.println("El subscriber lento (200ms/item) no pudo seguir el ritmo del producer (20ms/item).");
        System.out.println("El buffer de capacidad " + bufferCapacity + " absorbió algunos items antes de desbordarse.");
        System.out.println("La estrategia DROP descartó los que no cabían.");
    }
}
