package base.concurrencia.tipos;

/*
    VOLATILE

    ¿Qué es?
    Modificador que garantiza visibilidad entre hilos.
    Cuando un hilo escribe en una variable volatile, todos los demás leen el valor actualizado
    directamente desde memoria principal, no desde su caché local.

    ¿Para qué sirve?
    Para compartir flags o estados simples entre hilos sin necesidad de sincronización completa.

    ¿Cuándo usarlo?
    - Para flags de parada (stop flags) que un hilo escribe y otros leen.
    - Cuando un solo hilo escribe y varios leen (sin operaciones compuestas).

    ¿Cuándo NO usarlo?
    - Si necesitas operaciones compuestas como x++ (leer + incrementar + escribir).
      Para eso usa AtomicInteger o synchronized.
    - Si necesitas exclusión mutua, no solo visibilidad.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre volatile y synchronized?
    - ¿Por qué x++ no es seguro aunque x sea volatile?
    - ¿Qué es el Java Memory Model y cómo se relaciona con volatile?
*/
public class ExpVolatile {

    /*
        Sin volatile, el hilo worker podría cachear el valor de "running" en su
        registro local y nunca ver el cambio que hace el hilo principal.
        Con volatile, la escritura de stop() es visible inmediatamente.
    */
    private volatile boolean running = true;

    public void stop() { running = false; }

    public void doWork() {
        while (running) {
            System.out.println("Corriendo...");
        }
        System.out.println("Hilo detenido");
    }

    public static void main(String[] args) throws InterruptedException {
        ExpVolatile demo = new ExpVolatile();

        Thread worker = new Thread(demo::doWork);
        worker.start();

        Thread.sleep(10);   // el worker trabaja un poco
        demo.stop();        // otro hilo escribe en la variable volatile
        worker.join();      // el worker ve el cambio y termina
    }
}
