import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Ejercicio1 {

    static class BufferAcotado {
        private final Queue<String> cola = new LinkedList<>();
        private final int capacidad;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition noLleno  = lock.newCondition();
        private final Condition noVacio  = lock.newCondition();

        BufferAcotado(int cap) { this.capacidad = cap; }

        void producir(String item) throws InterruptedException {
            lock.lock();
            try {
                while (cola.size() == capacidad) noLleno.await();
                cola.add(item);
                System.out.println("Producido: " + item + " (tamaño=" + cola.size() + ")");
                noVacio.signalAll();
            } finally { lock.unlock(); }
        }

        String consumir() throws InterruptedException {
            lock.lock();
            try {
                while (cola.isEmpty()) noVacio.await();
                String item = cola.poll();
                System.out.println("Consumido: " + item + " (tamaño=" + cola.size() + ")");
                noLleno.signalAll();
                return item;
            } finally { lock.unlock(); }
        }
    }

    public static void main(String[] args) throws Exception {
        BufferAcotado buf = new BufferAcotado(3);
        Thread p = new Thread(() -> { try { for (int i=1;i<=6;i++) { buf.producir("P"+i); Thread.sleep(30); } } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        Thread c = new Thread(() -> { try { for (int i=0;i<6;i++)  { buf.consumir(); Thread.sleep(80); } } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        p.start(); c.start(); p.join(); c.join();
    }
}
