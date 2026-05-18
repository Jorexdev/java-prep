import java.util.concurrent.locks.ReentrantLock;

public class Ejercicio6 {

    static final ReentrantLock lockA = new ReentrantLock();
    static final ReentrantLock lockB = new ReentrantLock();

    static void sinDeadlock(String nombre, ReentrantLock primero, ReentrantLock segundo) throws InterruptedException {
        boolean adquirido = false;
        while (!adquirido) {
            if (primero.tryLock()) {
                try {
                    if (segundo.tryLock()) {
                        try {
                            System.out.println(nombre + ": ambos locks adquiridos");
                            adquirido = true;
                        } finally { segundo.unlock(); }
                    }
                } finally { primero.unlock(); }
            }
            if (!adquirido) Thread.sleep(1);
        }
    }

    public static void main(String[] args) throws Exception {
        Thread h1 = new Thread(() -> { try { sinDeadlock("Hilo-1", lockA, lockB); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        Thread h2 = new Thread(() -> { try { sinDeadlock("Hilo-2", lockA, lockB); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        h1.start(); h2.start(); h1.join(); h2.join();
        System.out.println("Sin deadlock — ambos completaron");
    }
}
