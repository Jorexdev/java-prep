import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Ejercicio4 {

    static class Connection {
        private static final AtomicInteger counter = new AtomicInteger();
        private final int id;
        private final BlockingQueue<Connection> pool;

        Connection(BlockingQueue<Connection> pool) {
            this.id = counter.incrementAndGet();
            this.pool = pool;
        }

        void execute(String sql) {
            System.out.printf("[Conn-%d][%s] %s%n", id, Thread.currentThread().getName(), sql);
            try { Thread.sleep(30); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        void close() {
            System.out.printf("[Conn-%d] devuelta al pool%n", id);
            pool.offer(this);
        }
    }

    static class ConnectionPool {
        private final BlockingQueue<Connection> available;

        ConnectionPool(int size) {
            available = new LinkedBlockingQueue<>(size);
            for (int i = 0; i < size; i++) available.offer(new Connection(available));
            System.out.println("Pool creado con " + size + " conexiones");
        }

        Connection acquire() throws InterruptedException {
            Connection c = available.take();
            System.out.printf("[%s] adquirida Conn-%d (pool restante: %d)%n",
                Thread.currentThread().getName(), c.id, available.size());
            return c;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ConnectionPool pool = new ConnectionPool(3);
        CountDownLatch done = new CountDownLatch(10);

        for (int i = 1; i <= 10; i++) {
            final int req = i;
            new Thread(() -> {
                try {
                    Connection c = pool.acquire();
                    c.execute("SELECT * FROM pedidos WHERE id = " + req);
                    c.close();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }, "T" + i).start();
        }

        done.await();
        System.out.println("\nTodas las consultas completadas.");
    }
}
