import java.util.ArrayList;
import java.util.List;

// Por que virtual threads NO ayudan en CPU-bound:
//
// Los virtual threads estan disenados para liberar el carrier thread durante
// operaciones bloqueantes (I/O, sleep, lock). En codigo CPU-puro, el thread
// nunca cede el carrier: siempre esta ocupado calculando.
//
// El paralelismo real esta limitado por el numero de CPU cores disponibles.
// Tanto platform threads como virtual threads usan el ForkJoinPool comun como
// scheduler, con tantos carriers como cores. 4 tareas CPU-bound en 4 cores
// = mismo tiempo independientemente del tipo de thread.
//
// Virtual threads solo brillan cuando hay bloqueos I/O que liberar.

public class Ejercicio2 {

    static int contarPrimos(int hasta) {
        int count = 0;
        for (int n = 2; n <= hasta; n++) {
            boolean primo = true;
            for (int d = 2; d * d <= n; d++) {
                if (n % d == 0) { primo = false; break; }
            }
            if (primo) count++;
        }
        return count;
    }

    static long medirTareasCPU(boolean virtual, int numTareas, int hasta) throws InterruptedException {
        List<Thread> threads = new ArrayList<>(numTareas);
        long start = System.currentTimeMillis();

        for (int t = 0; t < numTareas; t++) {
            Thread th;
            if (virtual) {
                th = Thread.ofVirtual().name("cpu-vt-" + t).unstarted(() -> {
                    int r = contarPrimos(hasta);
                    // result used to prevent dead code elimination
                    if (r < 0) System.out.println("imposible");
                });
            } else {
                th = Thread.ofPlatform().name("cpu-pt-" + t).unstarted(() -> {
                    int r = contarPrimos(hasta);
                    if (r < 0) System.out.println("imposible");
                });
            }
            threads.add(th);
            th.start();
        }

        for (Thread th : threads) th.join();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws InterruptedException {
        int numTareas = 4;
        int hasta = 100_000;

        System.out.println("=== CPU-bound: Virtual vs Platform Threads ===");
        System.out.printf("Tareas: %d, calcular primos hasta %,d%n%n", numTareas, hasta);

        // Warm up
        medirTareasCPU(false, 2, 10_000);
        medirTareasCPU(true, 2, 10_000);

        long platformMs = medirTareasCPU(false, numTareas, hasta);
        long virtualMs = medirTareasCPU(true, numTareas, hasta);

        System.out.println("=== Resultados ===");
        System.out.printf("%-25s %8dms%n", "Platform threads:", platformMs);
        System.out.printf("%-25s %8dms%n", "Virtual threads:", virtualMs);
        System.out.println();

        double ratio = (double) Math.max(platformMs, virtualMs) / Math.min(platformMs, virtualMs);
        System.out.printf("Diferencia: %.2fx (esperado: ~1x, sin mejora significativa)%n", ratio);
        System.out.println();
        System.out.println("Conclusion: virtual threads NO mejoran tareas CPU-bound.");
        System.out.println("El bottleneck es la CPU, no el bloqueo I/O.");
        System.out.println("Para CPU-bound paralelo: usa ForkJoinPool o parallelStream().");
    }
}
