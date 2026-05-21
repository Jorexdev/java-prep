import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Ejercicio5 {

    static class LockEntry {
        final String owner;
        final long expiryMs;

        LockEntry(String owner, long expiryMs) {
            this.owner = owner;
            this.expiryMs = expiryMs;
        }
    }

    static class DistributedLockManager {
        private final ConcurrentHashMap<String, LockEntry> locks = new ConcurrentHashMap<>();
        private final AtomicLong clock;
        private volatile boolean running = true;

        DistributedLockManager(AtomicLong clock) {
            this.clock = clock;
            Thread cleanup = new Thread(() -> {
                while (running) {
                    try { Thread.sleep(100); } catch (InterruptedException e) { break; }
                    long now = clock.get();
                    locks.entrySet().removeIf(entry -> {
                        boolean expired = now >= entry.getValue().expiryMs;
                        if (expired) {
                            System.out.println("[LockManager] Lock expirado: " + entry.getKey()
                                + " (owner=" + entry.getValue().owner + ")");
                        }
                        return expired;
                    });
                }
            });
            cleanup.setDaemon(true);
            cleanup.start();
        }

        boolean tryLock(String resource, String owner, long ttlMs) {
            long expiry = clock.get() + ttlMs;
            LockEntry entry = new LockEntry(owner, expiry);
            LockEntry existing = locks.putIfAbsent(resource, entry);
            if (existing == null) {
                System.out.println("[" + owner + "] Adquirió lock: " + resource + " (expira en " + ttlMs + "ms)");
                return true;
            }
            System.out.println("[" + owner + "] NO pudo adquirir lock: " + resource
                + " (dueño actual: " + existing.owner + ")");
            return false;
        }

        boolean unlock(String resource, String owner) {
            LockEntry entry = locks.get(resource);
            if (entry != null && entry.owner.equals(owner)) {
                locks.remove(resource, entry);
                System.out.println("[" + owner + "] Liberó lock: " + resource);
                return true;
            }
            return false;
        }

        void stop() { running = false; }
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicLong clock = new AtomicLong(0);
        DistributedLockManager manager = new DistributedLockManager(clock);
        String resource = "pedido-1";

        System.out.println("=== Exclusión mutua ===");
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(3);

        String[] owners = {"servicio-A", "servicio-B", "servicio-C"};
        for (String owner : owners) {
            new Thread(() -> {
                try {
                    start.await();
                    boolean locked = manager.tryLock(resource, owner, 500);
                    if (locked) {
                        Thread.sleep(50);
                        manager.unlock(resource, owner);
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        done.await();

        System.out.println("\n=== Expiración automática ===");
        clock.set(0);
        manager.tryLock(resource, "servicio-lento", 200);
        System.out.println("servicio-rápido intenta el lock:");
        manager.tryLock(resource, "servicio-rapido", 200);
        System.out.println("Avanzando el reloj 300ms...");
        clock.addAndGet(300);
        Thread.sleep(200);
        System.out.println("Después de expiración, servicio-rapido intenta de nuevo:");
        manager.tryLock(resource, "servicio-rapido", 200);

        manager.stop();
    }
}
