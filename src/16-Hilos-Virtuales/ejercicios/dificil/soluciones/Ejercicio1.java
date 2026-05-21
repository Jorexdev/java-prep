import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 10_000;
        int sleepMs = 100;

        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long heapAntes = rt.totalMemory() - rt.freeMemory();

        System.out.println("=== 10000 Virtual Threads ===");
        System.out.printf("Lanzando %,d virtual threads con I/O simulado de %dms...%n%n", numThreads, sleepMs);

        List<Thread> threads = new ArrayList<>(numThreads);
        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            Thread t = Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(t);
        }

        for (Thread t : threads) {
            t.join();
        }

        long elapsed = System.currentTimeMillis() - start;

        rt.gc();
        long heapDespues = rt.totalMemory() - rt.freeMemory();
        double throughput = numThreads / (elapsed / 1000.0);

        System.out.println("=== Resultados ===");
        System.out.printf("Threads lanzados : %,d%n", numThreads);
        System.out.printf("Tiempo total     : %dms%n", elapsed);
        System.out.printf("Throughput       : %,.0f req/seg%n", throughput);
        System.out.printf("Heap antes       : %,d bytes (%.1f MB)%n", heapAntes, heapAntes / 1_048_576.0);
        System.out.printf("Heap despues     : %,d bytes (%.1f MB)%n", heapDespues, heapDespues / 1_048_576.0);
        System.out.println();
        System.out.println("Tiempo ~= " + sleepMs + "ms: " + (elapsed < sleepMs * 3 ? "[OK]" : "[ver resultado]"));
        System.out.println();
        System.out.println("Con platform threads, 10000 threads necesitarian ~10GB de stack.");
        System.out.println("Los virtual threads son tan ligeros que 10000 caben facilmente en el heap.");
        System.out.println("La JVM usa el ForkJoinPool como carrier (normalmente = num CPUs).");
    }
}
