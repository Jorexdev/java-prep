import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Performance tuning en Java: JVM flags, String vs StringBuilder,
 * object pooling, boxing/unboxing y lazy initialization.
 * Cada técnica se mide con System.nanoTime() para comparar.
 */
public class ExpPerformanceTuning {

    // ═══════════════════════════════════════════════════════════════
    // JVM FLAGS RELEVANTES (se pasan al ejecutar la JVM, no en código)
    // ═══════════════════════════════════════════════════════════════
    //
    // -Xms512m          — heap inicial; evita resize frecuente al arrancar
    // -Xmx2g            — heap máximo; previene OOM inesperados
    // -XX:+UseG1GC      — G1 GC: bajo pause time, bueno para servicios web (default JDK 9+)
    // -XX:+UseZGC       — ZGC: sub-ms pauses, ideal para latencia crítica (JDK 15+)
    // -XX:MaxGCPauseMillis=200  — objetivo de pausa máxima para G1
    // -XX:+PrintGCDetails -Xlog:gc  — diagnóstico de GC en producción
    // -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap.hprof
    //                   — vuelca el heap en OOM para análisis post-mortem
    // -server           — activa optimizaciones JIT agresivas (HotSpot server compiler)

    // ═══════════════════════════════════════════════════════════════
    // 1. STRING CONCATENATION vs STRINGBUILDER EN LOOPS
    // ═══════════════════════════════════════════════════════════════

    static long benchmarkStringConcat(int iterations) {
        long start = System.nanoTime();
        String result = "";
        for (int i = 0; i < iterations; i++) {
            result += i; // crea un nuevo objeto String en cada iteración — O(n²) en memoria
        }
        long elapsed = System.nanoTime() - start;
        // Previene que el JIT elimine el resultado por dead code elimination
        if (result.isEmpty()) System.out.println("vacío");
        return elapsed;
    }

    static long benchmarkStringBuilder(int iterations) {
        long start = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sb.append(i); // amortized O(1), un solo buffer que crece según necesidad
        }
        String result = sb.toString();
        long elapsed = System.nanoTime() - start;
        if (result.isEmpty()) System.out.println("vacío");
        return elapsed;
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. OBJECT POOLING CON ArrayDeque
    // ═══════════════════════════════════════════════════════════════
    // Útil cuando la creación del objeto es cara (conexiones, buffers grandes).
    // Aquí se simula con objetos simples para ilustrar el patrón.

    static class HeavyObject {
        private final byte[] buffer = new byte[1024]; // simula objeto con recursos
        void reset() { /* limpiar estado para reutilización */ }
    }

    static class HeavyObjectPool {
        private final ArrayDeque<HeavyObject> pool = new ArrayDeque<>();
        private final int maxSize;

        HeavyObjectPool(int maxSize) {
            this.maxSize = maxSize;
            // Pre-calentar el pool
            for (int i = 0; i < maxSize; i++) pool.push(new HeavyObject());
        }

        HeavyObject borrow() {
            HeavyObject obj = pool.poll();
            return obj != null ? obj : new HeavyObject(); // si el pool está vacío, crea uno nuevo
        }

        void returnToPool(HeavyObject obj) {
            if (pool.size() < maxSize) {
                obj.reset();
                pool.push(obj);
            }
            // Si el pool está lleno, el GC recoge el objeto descartado
        }
    }

    static long benchmarkWithoutPool(int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            HeavyObject obj = new HeavyObject(); // nueva asignación en heap en cada iteración
            obj.reset();
        }
        return System.nanoTime() - start;
    }

    static long benchmarkWithPool(int iterations, HeavyObjectPool pool) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            HeavyObject obj = pool.borrow();
            obj.reset();
            pool.returnToPool(obj);
        }
        return System.nanoTime() - start;
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. BOXING/UNBOXING — int vs Integer en loops
    // ═══════════════════════════════════════════════════════════════

    static long benchmarkBoxing(int iterations) {
        long start = System.nanoTime();
        Long sum = 0L; // Long (objeto), no long (primitivo)
        for (int i = 0; i < iterations; i++) {
            sum += i; // cada suma: unbox Long → long, suma, box long → Long
        }
        long elapsed = System.nanoTime() - start;
        if (sum < 0) System.out.println("desbordamiento");
        return elapsed;
    }

    static long benchmarkPrimitive(int iterations) {
        long start = System.nanoTime();
        long sum = 0L; // primitivo, sin autoboxing
        for (int i = 0; i < iterations; i++) {
            sum += i;
        }
        long elapsed = System.nanoTime() - start;
        if (sum < 0) System.out.println("desbordamiento");
        return elapsed;
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. LAZY INITIALIZATION
    // ═══════════════════════════════════════════════════════════════

    static class EagerService {
        // Se inicializa aunque nunca se use — coste en startup
        private final List<String> cache = loadFromDatabase();

        private List<String> loadFromDatabase() {
            // simula carga lenta
            return new ArrayList<>(List.of("item1", "item2", "item3"));
        }

        List<String> getCache() { return cache; }
    }

    static class LazyService {
        private List<String> cache; // null hasta el primer acceso

        List<String> getCache() {
            if (cache == null) {
                cache = loadFromDatabase(); // solo se carga cuando se necesita
            }
            return cache;
        }

        private List<String> loadFromDatabase() {
            return new ArrayList<>(List.of("item1", "item2", "item3"));
        }
    }

    // Para acceso concurrente, usar double-checked locking con volatile:
    static class ThreadSafeLazyService {
        private volatile List<String> cache;

        List<String> getCache() {
            if (cache == null) {
                synchronized (this) {
                    if (cache == null) { // segunda comprobación dentro del lock
                        cache = new ArrayList<>(List.of("item1", "item2", "item3"));
                    }
                }
            }
            return cache;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN — mide y compara
    // ═══════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        final int ITERATIONS = 10_000;

        System.out.println("=== Performance Tuning en Java ===\n");

        // Warm-up para que el JIT compile antes de medir
        benchmarkStringConcat(100);
        benchmarkStringBuilder(100);

        System.out.println("1. String concatenation vs StringBuilder (" + ITERATIONS + " iter):");
        long concatMs = benchmarkStringConcat(ITERATIONS) / 1_000_000;
        long sbMs     = benchmarkStringBuilder(ITERATIONS) / 1_000_000;
        System.out.println("   String +=    : " + concatMs + " ms");
        System.out.println("   StringBuilder: " + sbMs + " ms");

        System.out.println("\n2. Object pooling (" + ITERATIONS + " iter):");
        HeavyObjectPool pool = new HeavyObjectPool(10);
        long noPoolMs = benchmarkWithoutPool(ITERATIONS) / 1_000_000;
        long poolMs   = benchmarkWithPool(ITERATIONS, pool) / 1_000_000;
        System.out.println("   Sin pool: " + noPoolMs + " ms");
        System.out.println("   Con pool: " + poolMs + " ms");

        System.out.println("\n3. Boxing vs primitivos (" + ITERATIONS + " iter):");
        long boxingMs    = benchmarkBoxing(ITERATIONS) / 1_000_000;
        long primitiveMs = benchmarkPrimitive(ITERATIONS) / 1_000_000;
        System.out.println("   Long (boxing) : " + boxingMs + " ms");
        System.out.println("   long (prim.)  : " + primitiveMs + " ms");

        System.out.println("\n4. Lazy initialization:");
        long t0 = System.nanoTime();
        LazyService lazy = new LazyService();
        long createMs = (System.nanoTime() - t0) / 1000;
        System.out.println("   LazyService creado en: " + createMs + " µs (sin cargar datos)");
        long t1 = System.nanoTime();
        lazy.getCache();
        long accessMs = (System.nanoTime() - t1) / 1000;
        System.out.println("   Primer acceso getCache(): " + accessMs + " µs");

        System.out.println("\n=== JVM flags recomendados (ver comentarios en el código) ===");
        System.out.println("   -Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200");
    }
}
