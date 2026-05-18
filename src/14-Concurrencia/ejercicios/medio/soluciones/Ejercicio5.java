import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Ejercicio5 {

    static class Cache {
        private final Map<String, String> store = new HashMap<>();
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        String leer(String clave) {
            rwLock.readLock().lock();
            try { return store.getOrDefault(clave, null); }
            finally { rwLock.readLock().unlock(); }
        }

        void escribir(String clave, String valor) {
            rwLock.writeLock().lock();
            try { store.put(clave, valor); }
            finally { rwLock.writeLock().unlock(); }
        }
    }

    public static void main(String[] args) throws Exception {
        Cache cache = new Cache();
        cache.escribir("config", "inicial");

        Thread[] lectores = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i;
            lectores[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    System.out.println("Lector-" + id + ": " + cache.leer("config"));
                    try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            });
        }

        Thread[] escritores = new Thread[2];
        for (int i = 0; i < 2; i++) {
            final int id = i;
            escritores[i] = new Thread(() -> {
                try { Thread.sleep(80); cache.escribir("config", "v" + (id+2)); System.out.println("Escritor-" + id + " actualizó config"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }

        for (Thread t : lectores)  t.start();
        for (Thread t : escritores) t.start();
        for (Thread t : lectores)  t.join();
        for (Thread t : escritores) t.join();
    }
}
