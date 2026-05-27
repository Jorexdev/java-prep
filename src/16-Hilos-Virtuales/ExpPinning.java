import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class ExpPinning {

    // ── 1. QUÉ ES EL PINNING ─────────────────────────────────────────────────
    // Un virtual thread está "montado" sobre un carrier thread (hilo de plataforma)
    // del ForkJoinPool. Cuando el VT se bloquea en I/O → se desmonta → el carrier
    // queda libre para otro VT. Esto es lo que hace eficientes a los VTs.
    //
    // PINNING: el VT NO puede desmontarse y BLOQUEA el carrier thread.
    // Ocurre cuando el VT está dentro de:
    //   1. Un bloque synchronized  (más común — JDK trabaja en eliminarlo)
    //   2. Una llamada a código nativo (JNI)
    //
    // Impacto: si hay N carrier threads y N VTs pinados, el sistema se paraliza.

    // ── 2. REPRODUCIR PINNING CON synchronized ────────────────────────────────
    // Con -Djdk.tracePinnedThreads=short el JVM imprime cuando un VT pina.
    static void demoPinning() throws Exception {
        System.out.println("── 2. Pinning con synchronized ──");

        Object lock = new Object();
        int carriers = Runtime.getRuntime().availableProcessors();
        System.out.println("  Carrier threads disponibles: " + carriers);

        // Lanzar más VTs que carriers, todos bloqueados en synchronized
        // Si el pool se llena de VTs pinados → ninguno avanza
        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(carriers + 2);

        long inicio = System.currentTimeMillis();
        for (int i = 0; i < carriers + 2; i++) {
            int id = i;
            exec.submit(() -> {
                synchronized (lock) {
                    // Simulamos I/O bloqueante DENTRO de synchronized → pinning
                    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    System.out.println("    VT-" + id + " completado");
                    latch.countDown();
                }
            });
        }
        latch.await();
        long elapsed = System.currentTimeMillis() - inicio;
        System.out.println("  Tiempo total: " + elapsed + "ms (serializado por pinning si carriers < tareas)");
        exec.shutdown();
    }

    // ── 3. SOLUCIÓN: ReentrantLock en vez de synchronized ────────────────────
    // ReentrantLock.lock() usa LockSupport.park() internamente → el VT se puede
    // DESMONTAR mientras espera. El carrier queda libre para otros VTs.
    // Regla: en código que corre sobre virtual threads, sustituir synchronized
    // por ReentrantLock cuando el cuerpo del bloque hace I/O o bloquea.
    static void demoReentrantLock() throws Exception {
        System.out.println("\n── 3. ReentrantLock — sin pinning ──");

        ReentrantLock lock = new ReentrantLock();
        int carriers = Runtime.getRuntime().availableProcessors();

        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(carriers + 2);

        long inicio = System.currentTimeMillis();
        for (int i = 0; i < carriers + 2; i++) {
            int id = i;
            exec.submit(() -> {
                lock.lock();
                try {
                    // I/O DENTRO del lock → VT se desmonta, carrier libre
                    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    System.out.println("    VT-" + id + " completado");
                    latch.countDown();
                } finally {
                    lock.unlock();
                }
            });
        }
        latch.await();
        long elapsed = System.currentTimeMillis() - inicio;
        System.out.println("  Tiempo total: " + elapsed + "ms (aún secuencial por el lock, pero carrier libre)");
        exec.shutdown();
    }

    // ── 4. DETECTAR PINNING ───────────────────────────────────────────────────
    // Flag JVM: -Djdk.tracePinnedThreads=short  → una línea por VT pinado
    //           -Djdk.tracePinnedThreads=full   → stack trace completo
    //
    // También en JFR: evento jdk.VirtualThreadPinned
    // En Java 24+: el JVM trabaja activamente en eliminar el pinning de synchronized.
    static void formasDeDetectar() {
        System.out.println("\n── 4. Cómo detectar pinning ──");
        System.out.println("  JVM flag: -Djdk.tracePinnedThreads=short");
        System.out.println("  JFR event: jdk.VirtualThreadPinned");
        System.out.println("  JDK 24+: gran parte del pinning en synchronized ya eliminado");
        System.out.println();
        System.out.println("  Regla práctica:");
        System.out.println("  • synchronized con cuerpo I/O-bound → migrar a ReentrantLock");
        System.out.println("  • synchronized con cuerpo CPU-bound (<1ms) → puede quedarse");
        System.out.println("  • Código de biblioteca tercera con synchronized → no controlable");
    }

    public static void main(String[] args) throws Exception {
        demoPinning();
        demoReentrantLock();
        formasDeDetectar();
    }
}
