public class Ejercicio3 {
    static volatile boolean corriendo = true;

    public static void main(String[] args) throws Exception {
        Thread lector = new Thread(() -> {
            long iteraciones = 0;
            while (corriendo) iteraciones++;
            System.out.println("Hilo parado tras " + iteraciones + " iteraciones");
        });
        lector.start();
        Thread.sleep(50);
        corriendo = false;
        lector.join();
        System.out.println("Main: hilo terminado");
    }
}
