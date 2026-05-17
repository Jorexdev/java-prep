public class ExpSynchronized {

    private int contador = 0;

    // método sincronizado: el lock es "this"
    // quita synchronized y ejecuta — el resultado será distinto de 200.000 (race condition)
    public synchronized void incMetodo() {
        contador++;
    }

    // bloque sincronizado: equivalente al método, pero más granular
    // útil si solo una parte del método necesita protección
    public void incBloque() {
        synchronized (this) {
            contador++;
        }
    }

    public int getContador() { return contador; }

    public static void main(String[] args) throws InterruptedException {
        ExpSynchronized demo = new ExpSynchronized();

        // dos hilos incrementan el contador 100.000 veces cada uno
        Runnable tarea = () -> {
            for (int i = 0; i < 100_000; i++) {
                demo.incMetodo();
            }
        };

        Thread t1 = new Thread(tarea);
        Thread t2 = new Thread(tarea);
        t1.start(); t2.start();
        t1.join();  t2.join();

        System.out.println("contador = " + demo.getContador()); // con synchronized siempre 200.000
    }
}
