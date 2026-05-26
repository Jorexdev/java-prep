import java.util.concurrent.*;

public class ExpVirtualThreadsBenchmark {

    static final int TASKS      = 10_000;
    static final int IO_SLEEP   =    10;   // ms, simula I/O bloqueante
    static final int POOL_SIZE  =   200;   // hilos de plataforma

    public static void main(String[] args) throws Exception {

        long platformMs = benchmarkPlatform();
        long virtualMs  = benchmarkVirtual();

        System.out.println();
        System.out.println("┌──────────────────────────────┬────────────────┬───────────────┬──────────────────┐");
        System.out.println("│ Estrategia                   │ Hilos activos  │ Tiempo (ms)   │ Throughput       │");
        System.out.println("├──────────────────────────────┼────────────────┼───────────────┼──────────────────┤");
        System.out.printf( "│ %-28s │ %-14s │ %-13d │ %-16s │%n",
                "Pool fijo (" + POOL_SIZE + " plataforma)", "" + POOL_SIZE,
                platformMs, throughput(TASKS, platformMs));
        System.out.printf( "│ %-28s │ %-14s │ %-13d │ %-16s │%n",
                "Virtual thread per task", "" + TASKS,
                virtualMs, throughput(TASKS, virtualMs));
        System.out.println("└──────────────────────────────┴────────────────┴───────────────┴──────────────────┘");

        System.out.println();
        System.out.println("Pool: " + TASKS + " tareas / " + POOL_SIZE + " hilos → "
                + (TASKS / POOL_SIZE) + " batches × " + IO_SLEEP + " ms ≈ "
                + ((TASKS / POOL_SIZE) * IO_SLEEP) + " ms esperados");
        System.out.println("Virtual: todas corren a la vez → ~" + IO_SLEEP + " ms esperados");
    }

    static long benchmarkPlatform() throws Exception {
        System.out.println("Ejecutando con pool de " + POOL_SIZE + " hilos de plataforma...");
        ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
        long t0 = System.currentTimeMillis();

        CompletableFuture<?>[] cfs = new CompletableFuture[TASKS];
        for (int i = 0; i < TASKS; i++) {
            cfs[i] = CompletableFuture.runAsync(ExpVirtualThreadsBenchmark::ioTask, pool);
        }
        CompletableFuture.allOf(cfs).join();

        long elapsed = System.currentTimeMillis() - t0;
        pool.shutdown();
        System.out.println("  Plataforma terminó en " + elapsed + " ms");
        return elapsed;
    }

    static long benchmarkVirtual() throws Exception {
        System.out.println("Ejecutando con virtual thread per task...");
        // newVirtualThreadPerTaskExecutor: lanza un virtual thread por cada tarea enviada
        long t0 = System.currentTimeMillis();

        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<?>[] cfs = new CompletableFuture[TASKS];
            for (int i = 0; i < TASKS; i++) {
                cfs[i] = CompletableFuture.runAsync(ExpVirtualThreadsBenchmark::ioTask, exec);
            }
            CompletableFuture.allOf(cfs).join();
        }

        long elapsed = System.currentTimeMillis() - t0;
        System.out.println("  Virtual terminó en " + elapsed + " ms");
        return elapsed;
    }

    static void ioTask() {
        try { Thread.sleep(IO_SLEEP); } catch (InterruptedException ignored) {}
    }

    static String throughput(int tasks, long ms) {
        if (ms == 0) return "∞ t/s";
        long tps = tasks * 1000L / ms;
        return tps + " t/s";
    }
}
