import java.util.concurrent.LinkedBlockingQueue;

public class Ejercicio1 {
    static final String POISON = "STOP";

    public static void main(String[] args) throws Exception {
        LinkedBlockingQueue<String> cola = new LinkedBlockingQueue<>(10);
        int numConsumers = 3;

        Thread[] productores = new Thread[2];
        for (int i = 0; i < 2; i++) {
            final int id = i + 1;
            productores[i] = new Thread(() -> {
                try {
                    for (int j = 1; j <= 5; j++) {
                        String item = "P" + id + "-item" + j;
                        cola.put(item);
                        System.out.println("Producido: " + item);
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }

        Thread[] consumidores = new Thread[numConsumers];
        for (int i = 0; i < numConsumers; i++) {
            final int id = i + 1;
            consumidores[i] = new Thread(() -> {
                try {
                    while (true) {
                        String item = cola.take();
                        if (POISON.equals(item)) { cola.put(POISON); break; }
                        System.out.println("Consumido por C" + id + ": " + item);
                        Thread.sleep(80);
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }

        for (Thread p : productores)  p.start();
        for (Thread c : consumidores) c.start();
        for (Thread p : productores)  p.join();
        cola.put(POISON);
        for (Thread c : consumidores) c.join();
        System.out.println("Pipeline completado");
    }
}
