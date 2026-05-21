import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio2 {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<Integer, String> cache = new ConcurrentHashMap<>();
        String[] nombres = {"Alice", "Bob", "Carol"};
        AtomicInteger insertCount = new AtomicInteger(0);

        CountDownLatch ready = new CountDownLatch(3);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            final String nombre = nombres[i];
            new Thread(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                String anterior = cache.putIfAbsent(1, nombre);
                if (anterior == null) {
                    // fue el primero en insertar
                    insertCount.incrementAndGet();
                    System.out.println(nombre + " -> insercion exitosa");
                } else {
                    System.out.println(nombre + " -> ya existia: " + anterior);
                }
                done.countDown();
            }).start();
        }

        ready.await();
        start.countDown();
        done.await();

        System.out.println();
        System.out.println("=== Resultado final ===");
        System.out.println("Usuario id=1 en cache: " + cache.get(1));
        System.out.println("Inserciones exitosas: " + insertCount.get() + " (esperado: 1)");
    }
}
