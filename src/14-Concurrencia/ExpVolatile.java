public class ExpVolatile {

    // volatile garantiza que todos los hilos lean el valor actualizado desde memoria principal,
    // no desde su caché local. Sin volatile, el worker podría nunca ver el cambio de stop().
    private volatile boolean running = true;

    public void stop() { running = false; }  // hilo externo escribe aquí

    public void doWork() {
        while (running) {                           // hilo worker lee la variable volatile
            System.out.println("Corriendo...");
        }
        System.out.println("Hilo detenido");        // visible gracias a volatile
    }

    public static void main(String[] args) throws InterruptedException {
        ExpVolatile demo = new ExpVolatile();

        Thread worker = new Thread(demo::doWork);
        worker.start();

        Thread.sleep(10);   // el worker trabaja un poco
        demo.stop();        // otro hilo escribe false en la variable volatile
        worker.join();      // el worker ve el cambio y termina — sin volatile podría no verlo nunca
    }
}
