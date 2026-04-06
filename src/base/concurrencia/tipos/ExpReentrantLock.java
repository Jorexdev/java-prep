package base.concurrencia.tipos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
    REENTRANT LOCK

    ¿Qué es?
    Lock explícito del paquete java.util.concurrent.locks.
    Ofrece más control que synchronized: tryLock, timeouts, condiciones, métricas.

    ¿Para qué sirve?
    Para los casos donde synchronized se queda corto:
    - Intentar adquirir el lock sin bloquear (tryLock).
    - Evitar deadlocks con timeouts.
    - Asociar Conditions para wait/notify más expresivos.

    ¿Cuándo usarlo?
    - Cuando necesitas tryLock para no bloquear el hilo si el recurso está ocupado.
    - Cuando necesitas varios Conditions en el mismo lock.
    - Cuando quieres consultar el estado del lock (isLocked, getQueueLength).

    Preguntas típicas de entrevista:
    - ¿Por qué siempre hay que llamar unlock() en un bloque finally?
    - ¿Qué significa "reentrant"? (el mismo hilo puede adquirir el lock varias veces)
    - ¿Qué diferencia hay entre lock() y tryLock()?
    - ¿Cuándo elegirías ReentrantLock sobre synchronized?
*/
public class ExpReentrantLock {

    private final Lock lock = new ReentrantLock();
    private int saldo = 0;

    /*
        lock() bloquea hasta adquirir el lock.
        Siempre en try/finally para garantizar que se libera aunque haya excepción.
    */
    public void ingresar(int cantidad) {
        lock.lock();
        try {
            saldo += cantidad;
        } finally {
            lock.unlock();
        }
    }

    /*
        tryLock() intenta adquirir el lock sin bloquear.
        Si otro hilo lo tiene, devuelve false inmediatamente en lugar de esperar.
        Útil para evitar deadlocks o para implementar lógica de "si no puedo, hago otra cosa".
    */
    public boolean retirar(int cantidad) {
        if (lock.tryLock()) {
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
        // No se obtuvo el lock: otro hilo lo tiene en este momento
        return false;
    }

    public int getSaldo() { return saldo; }

    public static void main(String[] args) throws InterruptedException {
        ExpReentrantLock cuenta = new ExpReentrantLock();

        Runnable r = () -> {
            for (int i = 0; i < 100_000; i++) cuenta.ingresar(1);
        };

        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start(); t2.start();
        t1.join();  t2.join();

        System.out.println("Saldo tras ingresos: " + cuenta.getSaldo()); // 200.000

        boolean ok = cuenta.retirar(50);
        System.out.println("Retiro ok: " + ok + " | saldo: " + cuenta.getSaldo());
    }
}
