import java.util.concurrent.CountDownLatch;

public class Ejercicio3 {
    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(3);

        String[] servicios = {"BaseDatos", "Cache", "Config"};
        int[] tiempos = {300, 150, 200};

        for (int i = 0; i < 3; i++) {
            final String nombre = servicios[i];
            final int tiempo = tiempos[i];
            new Thread(() -> {
                try {
                    System.out.println(nombre + ": iniciando...");
                    Thread.sleep(tiempo);
                    System.out.println(nombre + ": LISTO");
                    latch.countDown();
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }).start();
        }

        System.out.println("Main: esperando servicios...");
        latch.await();
        System.out.println("Main: todos los servicios listos — aplicación arrancada");
    }
}
