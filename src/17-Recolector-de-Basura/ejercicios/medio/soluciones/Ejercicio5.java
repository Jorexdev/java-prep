import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public class Ejercicio5 {

    static class ObjectPool<T> {
        private final Deque<T> pool = new ArrayDeque<>();
        private final Supplier<T> factory;
        private int totalCreated = 0;
        private int reuses = 0;

        ObjectPool(Supplier<T> factory) {
            this.factory = factory;
        }

        T acquire() {
            T obj = pool.poll();
            if (obj != null) {
                reuses++;
                return obj;
            }
            totalCreated++;
            return factory.get();
        }

        void release(T obj) {
            pool.push(obj);
        }

        int totalCreated() { return totalCreated; }
        int reuses() { return reuses; }
    }

    // Objeto costoso de crear (simula inicialización)
    static class ExpensiveObject {
        byte[] data = new byte[1024]; // 1KB de datos internos
        int id;

        ExpensiveObject(int id) {
            this.id = id;
        }

        void reset(int newId) {
            this.id = newId;
            // reutilizar el array en vez de crear uno nuevo
        }
    }

    static int idCounter = 0;

    public static void main(String[] args) {
        System.out.println("=== Object Pool Benchmark ===");
        System.out.println();

        int iterations = 10_000;

        // --- Con Object Pool ---
        ObjectPool<ExpensiveObject> pool = new ObjectPool<>(() -> new ExpensiveObject(++idCounter));

        long startPool = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ExpensiveObject obj = pool.acquire();
            obj.reset(i);
            // simular trabajo
            int sum = 0;
            for (byte b : obj.data) sum += b;
            pool.release(obj);
        }
        long timePool = System.nanoTime() - startPool;

        // --- Sin Pool (new + GC) ---
        long startNew = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ExpensiveObject obj = new ExpensiveObject(i);
            // simular trabajo
            int sum = 0;
            for (byte b : obj.data) sum += b;
            // obj queda elegible para GC
        }
        long timeNew = System.nanoTime() - startNew;

        System.out.println("Iteraciones: " + iterations);
        System.out.println();
        System.out.printf("Con Object Pool: %,d ns (%.2f ms)%n", timePool, timePool / 1_000_000.0);
        System.out.printf("Sin Pool (new):  %,d ns (%.2f ms)%n", timeNew, timeNew / 1_000_000.0);
        System.out.println();
        System.out.println("Estadísticas del pool:");
        System.out.println("  Objetos creados (total): " + pool.totalCreated());
        System.out.println("  Reutilizaciones:         " + pool.reuses());
        System.out.println("  Tasa de reutilización:   " +
            String.format("%.1f%%", pool.reuses() * 100.0 / iterations));

        if (timePool < timeNew) {
            long speedup = (timeNew - timePool) * 100 / timeNew;
            System.out.println();
            System.out.println("Pool es " + speedup + "% más rápido — menos presión de GC.");
        }
    }
}
