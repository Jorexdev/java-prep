package base.concurrencia.tipos;

import java.util.LinkedList;
import java.util.Queue;

public class ExpWaitNotify {

    private final Queue<Integer> cola = new LinkedList<>();
    private final int CAPACIDAD = 5;

    public void producir(int valor) throws InterruptedException {
        synchronized (cola) {
            // while, no if — los spurious wakeups pueden despertar el hilo sin que la
            // condición sea realmente true; el while vuelve a comprobarla
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

        // productor: genera 20 valores y los encola
        Thread productor = new Thread(() -> {
            try {
                for (int i = 1; i <= 20; i++) {
                    pc.producir(i);
                    System.out.println("Producido: " + i);
                }
            } catch (InterruptedException ignored) { }
        });

        // consumidor: extrae 20 valores de la cola
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
