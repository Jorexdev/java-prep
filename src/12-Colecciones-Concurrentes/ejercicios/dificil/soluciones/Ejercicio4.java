import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Ejercicio4 {

    static final int NUM_WORKERS = 4;
    static final int TASKS_PER_WORKER = 10;
    static final AtomicInteger steals = new AtomicInteger(0);
    static final AtomicInteger completadas = new AtomicInteger(0);

    static final ArrayDeque<Runnable>[] deques = new ArrayDeque[NUM_WORKERS];
    static final ReentrantLock[] dequeLocks = new ReentrantLock[NUM_WORKERS];

    static {
        for (int i = 0; i < NUM_WORKERS; i++) {
            deques[i] = new ArrayDeque<>();
            dequeLocks[i] = new ReentrantLock();
        }
    }

    static Runnable steal(int thiefId) {
        // Encontrar el worker con mas tareas
        int victimId = -1;
        int maxSize = 0;
        for (int i = 0; i < NUM_WORKERS; i++) {
            if (i == thiefId) continue;
            int sz;
            dequeLocks[i].lock();
            try { sz = deques[i].size(); } finally { dequeLocks[i].unlock(); }
            if (sz > maxSize) { maxSize = sz; victimId = i; }
        }
        if (victimId < 0 || maxSize == 0) return null;

        // Robar la mitad
        List<Runnable> stolen = new ArrayList<>();
        dequeLocks[victimId].lock();
        try {
            int toSteal = Math.max(1, deques[victimId].size() / 2);
            for (int i = 0; i < toSteal; i++) {
                Runnable t = deques[victimId].pollLast(); // roba del final (LIFO)
                if (t != null) stolen.add(t);
            }
        } finally {
            dequeLocks[victimId].unlock();
        }

        if (!stolen.isEmpty()) {
            dequeLocks[thiefId].lock();
            try { deques[thiefId].addAll(stolen); } finally { dequeLocks[thiefId].unlock(); }
            steals.addAndGet(stolen.size());
            System.out.println("  [Worker-" + thiefId + "] robo " + stolen.size() +
                               " tareas de Worker-" + victimId);
            return stolen.get(0);
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        Random rng = new Random(1);

        // Distribuir 40 tareas: 10 por worker
        for (int w = 0; w < NUM_WORKERS; w++) {
            for (int t = 0; t < TASKS_PER_WORKER; t++) {
                final int wId = w;
                final int tId = w * TASKS_PER_WORKER + t;
                final int sleepMs = rng.nextInt(10); // 0-9ms (algunas mas lentas)
                deques[w].add(() -> {
                    System.out.println("  [Task-" + tId + "] ejecutado por Worker-" + wId +
                                       " (sim=" + sleepMs + "ms)");
                    try { Thread.sleep(sleepMs); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    completadas.incrementAndGet();
                });
            }
        }

        CountDownLatch latch = new CountDownLatch(NUM_WORKERS);
        long start = System.currentTimeMillis();

        for (int w = 0; w < NUM_WORKERS; w++) {
            final int wid = w;
            new Thread(() -> {
                while (completadas.get() < NUM_WORKERS * TASKS_PER_WORKER) {
                    Runnable task = null;
                    dequeLocks[wid].lock();
                    try { task = deques[wid].pollFirst(); } finally { dequeLocks[wid].unlock(); }

                    if (task == null) {
                        // Deque vacio: intentar robar
                        task = steal(wid);
                    }

                    if (task != null) {
                        task.run();
                    } else {
                        // Nada que hacer: esperar un poco
                        try { Thread.sleep(1); } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                latch.countDown();
            }, "Worker-" + w).start();
        }

        latch.await();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("\n=== Work Stealing ===");
        System.out.println("Tareas totales   : " + NUM_WORKERS * TASKS_PER_WORKER);
        System.out.println("Tareas completadas: " + completadas.get());
        System.out.println("Robos realizados : " + steals.get());
        System.out.println("Tiempo total     : " + elapsed + "ms");
        System.out.println("Work stealing balancea la carga dinamicamente sin coordinacion central.");
    }
}
