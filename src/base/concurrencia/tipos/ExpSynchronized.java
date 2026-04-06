package base.concurrencia.tipos;

/*
    SYNCHRONIZED

    ¿Qué es?
    Mecanismo de exclusión mutua integrado en Java.
    Solo un hilo a la vez puede ejecutar una sección sincronizada sobre el mismo monitor.

    ¿Para qué sirve?
    Para proteger secciones críticas donde múltiples hilos acceden a datos compartidos.
    Garantiza exclusión mutua y visibilidad (flush de memoria al entrar y salir).

    ¿Cuándo usarlo?
    - Cuando la lógica crítica es simple y no necesitas tryLock ni timeouts.
    - Cuando quieres la forma más directa de proteger un método o bloque.

    ¿Cuándo NO usarlo?
    - Si necesitas tryLock (no bloqueante) o timeouts: usa ReentrantLock.
    - Si solo necesitas visibilidad sin exclusión mutua: usa volatile.

    Preguntas típicas de entrevista:
    - ¿Qué es el monitor de un objeto?
    - ¿Cuál es la diferencia entre sincronizar un método y sincronizar un bloque?
    - ¿Qué pasa si quitas synchronized del ejemplo? (race condition, resultado incorrecto)
    - ¿synchronized garantiza visibilidad además de exclusión mutua?
*/
public class ExpSynchronized {

    private int contador = 0;

    /*
        Método sincronizado: el lock es "this".
        Prueba a quitar synchronized y ejecutar: el resultado será distinto de 200.000
        porque los dos hilos leerán y escribirán el contador al mismo tiempo (race condition).
    */
    public synchronized void incMetodo() {
        contador++;
    }

    /*
        Bloque sincronizado: equivalente al método, pero más granular.
        Útil si solo una parte del método necesita protección.
    */
    public void incBloque() {
        synchronized (this) {
            contador++;
        }
    }

    public int getContador() { return contador; }

    public static void main(String[] args) throws InterruptedException {
        ExpSynchronized demo = new ExpSynchronized();

        Runnable tarea = () -> {
            for (int i = 0; i < 100_000; i++) {
                demo.incMetodo();
            }
        };

        Thread t1 = new Thread(tarea);
        Thread t2 = new Thread(tarea);
        t1.start(); t2.start();
        t1.join();  t2.join();

        // Con synchronized el resultado siempre es 200.000
        System.out.println("contador = " + demo.getContador());
    }
}
