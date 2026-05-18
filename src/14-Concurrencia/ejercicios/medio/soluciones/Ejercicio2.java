import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio2 {
    static final Semaphore pool = new Semaphore(3, true);
    static final AtomicInteger activas = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        Thread[] hilos = new Thread[8];
        for (int i = 0; i < 8; i++) {
            final int id = i + 1;
            hilos[i] = new Thread(() -> {
                try {
                    System.out.println("Hilo-" + id + " esperando conexión...");
                    pool.acquire();
                    int n = activas.incrementAndGet();
                    System.out.println("Hilo-" + id + " CONECTADO (activas=" + n + ")");
                    Thread.sleep(100);
                    activas.decrementAndGet();
                    pool.release();
                    System.out.println("Hilo-" + id + " liberó conexión");
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        for (Thread h : hilos) h.start();
        for (Thread h : hilos) h.join();
    }
}
