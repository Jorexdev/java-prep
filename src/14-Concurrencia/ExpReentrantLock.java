import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExpReentrantLock {

    private final Lock lock = new ReentrantLock();  // lock explícito, más control que synchronized
    private int saldo = 0;

    public void ingresar(int cantidad) {
        lock.lock();            // bloquea hasta adquirir el lock
        try {
            saldo += cantidad;
        } finally {
            lock.unlock();      // siempre en finally para garantizar que se libera aunque haya excepción
        }
    }

    public boolean retirar(int cantidad) {
        if (lock.tryLock()) {   // intenta adquirir sin bloquear — si otro hilo lo tiene, devuelve false
            try {
                if (saldo >= cantidad) {
                    saldo -= cantidad;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }
        return false;           // no se obtuvo el lock: otro hilo lo tiene en este momento
    }

    public int getSaldo() { return saldo; }

    public static void main(String[] args) throws InterruptedException {
        ExpReentrantLock cuenta = new ExpReentrantLock();

        // dos hilos incrementan el saldo 100.000 veces cada uno
        Runnable r = () -> {
            for (int i = 0; i < 100_000; i++) cuenta.ingresar(1);
        };

        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start(); t2.start();
        t1.join();  t2.join();

        System.out.println("Saldo tras ingresos: " + cuenta.getSaldo()); // siempre 200.000

        boolean ok = cuenta.retirar(50);
        System.out.println("Retiro ok: " + ok + " | saldo: " + cuenta.getSaldo());
    }
}
