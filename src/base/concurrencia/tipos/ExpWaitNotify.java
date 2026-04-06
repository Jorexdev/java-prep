package base.concurrencia.tipos;

import java.util.LinkedList;
import java.util.Queue;

/*
    WAIT / NOTIFY

    ¿Qué es?
    Mecanismo de coordinación entre hilos usando el monitor de un objeto.
    - wait(): el hilo suelta el lock y espera hasta recibir una señal.
    - notify(): despierta UN hilo que esté esperando sobre el mismo monitor.
    - notifyAll(): despierta TODOS los hilos que estén esperando.

    ¿Para qué sirve?
    Para implementar patrones de comunicación entre hilos, como productor-consumidor.
    Un hilo espera hasta que otro le señala que hay algo que procesar.

    ¿Cuándo usarlo?
    - Cuando necesitas coordinación básica entre hilos sin depender de librerías.
    - Para el patrón productor-consumidor en su forma más directa.

    ¿Cuándo NO usarlo?
    - Para código nuevo: BlockingQueue (ArrayBlockingQueue, LinkedBlockingQueue)
      ya implementa este patrón de forma segura y más sencilla.

    Preguntas típicas de entrevista:
    - ¿Por qué wait() debe estar dentro de un while y no de un if?
    - ¿Por qué wait() y notify() deben usarse dentro de synchronized?
    - ¿Qué son los spurious wakeups?
    - ¿Qué diferencia hay entre notify() y notifyAll()?
*/
public class ExpWaitNotify {

    private final Queue<Integer> cola = new LinkedList<>();
    private final int CAPACIDAD = 5;

    public void producir(int valor) throws InterruptedException {
        synchronized (cola) {
            /*
                Siempre while, no if.
                Reason: los spurious wakeups pueden despertar el hilo sin que la
                condición sea realmente true. El while lo vuelve a comprobar.
            */
            while (cola.size() == CAPACIDAD) {
                cola.wait();        // suelta el lock y espera a que haya hueco
            }
            cola.add(valor);
            cola.notifyAll();       // señal: hay datos nuevos para el consumidor
        }
    }

    public int consumir() throws InterruptedException {
        synchronized (cola) {
            while (cola.isEmpty()) {
                cola.wait();        // suelta el lock y espera a que haya datos
            }
            int v = cola.remove();
            cola.notifyAll();       // señal: hay hueco para el productor
            return v;
        }
    }

    public static void main(String[] args) {
        ExpWaitNotify pc = new ExpWaitNotify();

        Thread productor = new Thread(() -> {
            try {
                for (int i = 1; i <= 20; i++) {
                    pc.producir(i);
                    System.out.println("Producido: " + i);
                }
            } catch (InterruptedException ignored) { }
        });

        Thread consumidor = new Thread(() -> {
            try {
                for (int i = 1; i <= 20; i++) {
                    System.out.println("Consumido: " + pc.consumir());
                }
            } catch (InterruptedException ignored) { }
        });

        productor.start();
        consumidor.start();
    }
}
