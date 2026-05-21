import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

// Por que LongAdder es mas rapido bajo alta contencion:
//
// AtomicLong tiene UNA celda de memoria. Con 8 threads en contention, todos
// compiten con CAS sobre esa celda. Muchos CAS fallan y se reintentan (spin).
//
// LongAdder mantiene un array de celdas (Cell[]). Cada thread tiene su celda
// preferida. Las sumas ocurren en paralelo sin contention. sum() agrega todas
// las celdas al final. El precio: sum() es mas costoso, pero increment() es mucho
// mas barato bajo carga concurrente alta.

public class Ejercicio3 {

    static long runAtomicLong(int numThreads, int increments) throws InterruptedException {
        AtomicLong counter = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(numThreads);
        long start = System.currentTimeMillis();
        for (int t = 0; t < numThreads; t++) {
            new Thread(() -> {
                for (int i = 0; i < increments; i++) counter.incrementAndGet();
                latch.countDown();
            }).start();
        }
        latch.await();
        long elapsed = System.currentTimeMillis() - start;
        long expected = (long) numThreads * increments;
        if (counter.get() != expected) System.out.println("AtomicLong ERROR: " + counter.get() + " != " + expected);
        return elapsed;
    }

    static long runLongAdder(int numThreads, int increments) throws InterruptedException {
        LongAdder counter = new LongAdder();
        CountDownLatch latch = new CountDownLatch(numThreads);
        long start = System.currentTimeMillis();
        for (int t = 0; t < numThreads; t++) {
            new Thread(() -> {
                for (int i = 0; i < increments; i++) counter.increment();
                latch.countDown();
            }).start();
        }
        latch.await();
        long elapsed = System.currentTimeMillis() - start;
        long expected = (long) numThreads * increments;
        if (counter.sum() != expected) System.out.println("LongAdder ERROR: " + counter.sum() + " != " + expected);
        return elapsed;
    }

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 8;
        int increments = 100_000;
        int rounds = 3;

        System.out.println("=== LongAdder vs AtomicLong ===");
        System.out.println("Threads: " + numThreads + ", incrementos/thread: " + increments);
        System.out.println("Total: " + ((long) numThreads * increments));
        System.out.println();

        long totalAtomic = 0, totalAdder = 0;

        System.out.printf("%-8s %12s %12s%n", "Ronda", "AtomicLong", "LongAdder");
        System.out.println("-".repeat(34));

        for (int r = 1; r <= rounds; r++) {
            long atomic = runAtomicLong(numThreads, increments);
            long adder = runLongAdder(numThreads, increments);
            totalAtomic += atomic;
            totalAdder += adder;
            System.out.printf("%-8d %10dms %10dms%n", r, atomic, adder);
        }

        long avgAtomic = totalAtomic / rounds;
        long avgAdder = totalAdder / rounds;

        System.out.println("-".repeat(34));
        System.out.printf("%-8s %10dms %10dms%n", "Media", avgAtomic, avgAdder);
        System.out.println();
        if (avgAdder > 0) {
            System.out.printf("Speedup LongAdder: %.2fx%n", (double) avgAtomic / avgAdder);
        }
        System.out.println();
        System.out.println("LongAdder usa celdas independientes por thread -> sin contention en increment().");
        System.out.println("sum() agrega las celdas, util cuando se lee raramente pero se escribe mucho.");
    }
}
