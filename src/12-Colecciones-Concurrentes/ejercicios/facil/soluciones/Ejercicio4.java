import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ejercicio4 {
    public static void main(String[] args) throws InterruptedException {
        CopyOnWriteArrayList<String> observers = new CopyOnWriteArrayList<>();
        observers.add("Observer-1");
        observers.add("Observer-2");
        observers.add("Observer-3");
        observers.add("Observer-4");
        observers.add("Observer-5");

        AtomicBoolean error = new AtomicBoolean(false);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(4); // 3 lectores + 1 escritor

        // 3 threads que iteran
        for (int i = 0; i < 3; i++) {
            final int readerId = i + 1;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (String obs : observers) {
                        Thread.sleep(1);
                        // iterar sin excepcion
                    }
                    System.out.println("Lector-" + readerId + " termino sin ConcurrentModificationException");
                } catch (java.util.ConcurrentModificationException e) {
                    error.set(true);
                    System.out.println("Lector-" + readerId + " ERROR: ConcurrentModificationException");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // 1 thread escritor
        new Thread(() -> {
            try {
                startLatch.await();
                Thread.sleep(2); // dejar que los lectores empiecen
                observers.add("Observer-6-nuevo");
                System.out.println("Escritor: Observer-6 anadido. Tamano total: " + observers.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        startLatch.countDown();
        doneLatch.await();

        System.out.println();
        System.out.println("=== Resultado ===");
        System.out.println("Observers finales: " + observers.size());
        System.out.println("ConcurrentModificationException: " + (error.get() ? "SI (fallo)" : "NO (correcto)"));
        System.out.println("CopyOnWriteArrayList permite iteracion segura mientras se modifica.");
    }
}
