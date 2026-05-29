import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio5 {

    // Connection pool con Virtual Threads y Semaphore
    // Simula conexiones a una BD: cada conexion tiene un ID y estado (libre/en uso)
    static class Connection {
        private final int id;
        private volatile boolean inUse = false;

        Connection(int id) {
            this.id = id;
            System.out.printf("  [Pool] conexion #%d creada%n", id);
        }

        void execute(String query) throws InterruptedException {
            System.out.printf("  [Conn #%d] ejecutando: %s%n", id, query);
            Thread.sleep(50 + (int)(Math.random() * 50)); // simula I/O de BD
        }

        int getId() { return id; }
        boolean isInUse() { return inUse; }
        void setInUse(boolean v) { this.inUse = v; }
    }

    static class ConnectionPool {
        private final int poolSize;
        private final Semaphore semaphore;
        private final BlockingQueue<Connection> available;
        private final AtomicInteger borrowed = new AtomicInteger(0);
        private final AtomicInteger maxBorrowed = new AtomicInteger(0);
        private final AtomicInteger totalAcquired = new AtomicInteger(0);

        ConnectionPool(int poolSize) {
            this.poolSize = poolSize;
            this.semaphore = new Semaphore(poolSize, true); // fair=true
            this.available = new LinkedBlockingQueue<>();
            for (int i = 1; i <= poolSize; i++) {
                available.add(new Connection(i));
            }
            System.out.printf("[Pool] inicializado con %d conexiones%n%n", poolSize);
        }

        // Obtiene una conexion del pool, bloqueando si no hay ninguna libre
        Connection acquire() throws InterruptedException {
            semaphore.acquire(); // espera hasta que haya un slot disponible
            Connection conn = available.poll();
            if (conn == null) {
                semaphore.release();
                throw new IllegalStateException("Bug: semaphore permitio acquire pero no hay conexion");
            }
            conn.setInUse(true);
            int cur = borrowed.incrementAndGet();
            maxBorrowed.updateAndGet(prev -> Math.max(prev, cur));
            totalAcquired.incrementAndGet();
            return conn;
        }

        // Devuelve la conexion al pool
        void release(Connection conn) {
            conn.setInUse(false);
            borrowed.decrementAndGet();
            available.offer(conn);
            semaphore.release();
        }

        int getPoolSize() { return poolSize; }
        int getMaxBorrowed() { return maxBorrowed.get(); }
        int getTotalAcquired() { return totalAcquired.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Connection Pool con Virtual Threads y Semaphore ===");
        System.out.println("Pool size: 3 | Clientes concurrentes: 15");
        System.out.println("-".repeat(55));

        int poolSize = 3;
        int numClientes = 15;
        ConnectionPool pool = new ConnectionPool(poolSize);

        CountDownLatch latch = new CountDownLatch(numClientes);
        long inicio = System.currentTimeMillis();

        for (int i = 1; i <= numClientes; i++) {
            final int clientId = i;
            Thread.ofVirtual().name("cliente-" + i).start(() -> {
                Connection conn = null;
                try {
                    System.out.printf("[cliente-%02d] solicitando conexion...%n", clientId);
                    conn = pool.acquire();
                    System.out.printf("[cliente-%02d] conexion #%d adquirida | prestadas: %d%n",
                            clientId, conn.getId(), pool.borrowed.get());
                    conn.execute("SELECT * FROM tabla WHERE id=" + clientId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    if (conn != null) {
                        pool.release(conn);
                        System.out.printf("[cliente-%02d] conexion #%d liberada%n",
                                clientId, conn.getId());
                    }
                    latch.countDown();
                }
            });
        }

        latch.await();
        long total = System.currentTimeMillis() - inicio;

        System.out.println("-".repeat(55));
        System.out.println();
        System.out.printf("Pool size              : %d conexiones%n", pool.getPoolSize());
        System.out.printf("Clientes atendidos     : %d/%d%n", pool.getTotalAcquired(), numClientes);
        System.out.printf("Max prestadas simult.  : %d (limite: %d)%n",
                pool.getMaxBorrowed(), poolSize);
        System.out.printf("Tiempo total           : %d ms%n", total);
        System.out.printf("Throughput             : %.1f ops/seg%n",
                numClientes * 1000.0 / total);
        System.out.println();

        // Con pool=3 y 15 clientes de ~75ms, esperamos ~5 rondas = ~375ms
        System.out.printf("Tiempo sin pool (secuencial): ~%d ms%n", numClientes * 75);
        System.out.println();
        System.out.println("El Semaphore garantiza que nunca se presten mas de "
                + poolSize + " conexiones simultaneas.");
        System.out.println("Los Virtual Threads esperan en el semaphore sin bloquear platform threads.");
        System.out.println("Invariante: max_prestadas <= pool_size -> "
                + (pool.getMaxBorrowed() <= poolSize));
    }
}
