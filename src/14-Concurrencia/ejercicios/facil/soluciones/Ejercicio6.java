import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio6 {
    public static void main(String[] args) throws Exception {
        AtomicInteger contador = new AtomicInteger(0);
        Thread[] hilos = new Thread[10];
        for (int i = 0; i < 10; i++)
            hilos[i] = new Thread(() -> { for (int j = 0; j < 1000; j++) contador.incrementAndGet(); });
        for (Thread h : hilos) h.start();
        for (Thread h : hilos) h.join();
        System.out.println("AtomicInteger: " + contador.get() + " (esperado 10000)");
    }
}
