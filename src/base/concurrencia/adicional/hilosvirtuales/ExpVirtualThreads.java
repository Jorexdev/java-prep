package base.concurrencia.adicional.hilosvirtuales;

import java.util.concurrent.*;

public class ExpVirtualThreads {

    public static void main(String[] args) throws Exception {

        // Crear un Virtual Thread directamente
        // La API es la misma que con hilos normales
        Thread vt = Thread.ofVirtual().start(() -> {
            System.out.println("Virtual Thread: " + Thread.currentThread());
            dormir(100);
        });
        vt.join();

        // Executor de Virtual Threads: un nuevo Virtual Thread por cada tarea
        // Ideal cuando tienes muchas tareas I/O cortas e independientes
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            var f1 = exec.submit(() -> { dormir(50); return "uno"; });
            var f2 = exec.submit(() -> { dormir(80); return "dos"; });
            System.out.println("Resultados: " + f1.get() + ", " + f2.get());
        }

        // Escalar a 10.000 tareas I/O-bound sin problema
        // Con hilos de plataforma esto requeriría un pool grande o programación reactiva
        long inicio = System.currentTimeMillis();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<?>[] cfs = new CompletableFuture[10_000];
            for (int i = 0; i < 10_000; i++) {
                cfs[i] = CompletableFuture.runAsync(() -> dormir(5), exec);
            }
            CompletableFuture.allOf(cfs).join();
        }
        System.out.println("10.000 tareas I/O en: " + (System.currentTimeMillis() - inicio) + " ms");
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
