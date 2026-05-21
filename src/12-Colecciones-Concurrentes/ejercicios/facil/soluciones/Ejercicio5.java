import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio5 {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        int total = 20;
        AtomicInteger consumidos = new AtomicInteger(0);

        Thread producer = new Thread(() -> {
            for (int i = 1; i <= total; i++) {
                queue.offer(i);
                System.out.println("  [producer] anadido: " + i);
                try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });

        Thread consumer = new Thread(() -> {
            int count = 0;
            while (count < total) {
                Integer item = queue.poll();
                if (item != null) {
                    count++;
                    consumidos.incrementAndGet();
                    System.out.println("  [consumer] consumido: " + item);
                }
                // sin synchronized, sin bloqueo
            }
        });

        System.out.println("=== ConcurrentLinkedQueue producer-consumer ===");
        consumer.start();
        producer.start();

        producer.join();
        consumer.join();

        System.out.println();
        System.out.println("Total consumidos: " + consumidos.get() + (consumidos.get() == total ? " [OK]" : " [FALLO]"));
    }
}
