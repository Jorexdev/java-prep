import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio1 {
    public static void main(String[] args) throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(5);
        int tareasProducer = 10;
        int numProductores = 2;
        int numConsumidores = 3;
        int totalTareas = tareasProducer * numProductores;
        AtomicInteger procesadas = new AtomicInteger(0);
        CountDownLatch producersDone = new CountDownLatch(numProductores);
        CountDownLatch consumersDone = new CountDownLatch(numConsumidores);

        // Productores
        for (int p = 1; p <= numProductores; p++) {
            final int pid = p;
            new Thread(() -> {
                try {
                    for (int i = 1; i <= tareasProducer; i++) {
                        String tarea = "tarea-P" + pid + "-" + i;
                        queue.put(tarea); // bloquea si llena
                        System.out.println("[Producer-" + pid + "] puso: " + tarea +
                                           " | queue size: " + queue.size());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producersDone.countDown();
                }
            }, "Producer-" + p).start();
        }

        // Consumidores
        for (int c = 1; c <= numConsumidores; c++) {
            final int cid = c;
            new Thread(() -> {
                try {
                    while (true) {
                        int actual = procesadas.get();
                        if (actual >= totalTareas) break;
                        String tarea = queue.poll(200, java.util.concurrent.TimeUnit.MILLISECONDS);
                        if (tarea != null) {
                            int cnt = procesadas.incrementAndGet();
                            System.out.println("  [Consumer-" + cid + "] proceso: " + tarea +
                                               " (" + cnt + "/" + totalTareas + ")");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    consumersDone.countDown();
                }
            }, "Consumer-" + c).start();
        }

        producersDone.await();
        consumersDone.await();

        System.out.println("\n=== Resultado ===");
        System.out.println("Tareas procesadas: " + procesadas.get() + "/" + totalTareas +
                           (procesadas.get() == totalTareas ? " [OK]" : " [FALLO]"));
    }
}
