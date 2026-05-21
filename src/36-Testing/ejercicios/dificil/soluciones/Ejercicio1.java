import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Ejercicio1 {

    static class ContadorNoSincronizado {
        private int valor = 0;
        void incrementar() { valor++; }
        int getValor() { return valor; }
    }

    static class ContadorAtomico {
        private final AtomicInteger valor = new AtomicInteger();
        void incrementar() { valor.incrementAndGet(); }
        int getValor() { return valor.get(); }
    }

    static int ejecutar(int threads, int ops, Runnable incrementar, java.util.function.IntSupplier getValor)
            throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                for (int j = 0; j < ops; j++) incrementar.run();
                done.countDown();
            }).start();
        }
        ready.await();
        start.countDown();
        done.await();
        return getValor.getAsInt();
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = 100, ops = 1000, esperado = threads * ops;

        System.out.println("Threads: " + threads + "  Ops/thread: " + ops + "  Esperado: " + esperado);

        System.out.println("\n=== Contador SIN sincronización (race condition) ===");
        ContadorNoSincronizado c1 = new ContadorNoSincronizado();
        int r1 = ejecutar(threads, ops, c1::incrementar, c1::getValor);
        System.out.printf("Obtenido: %d  Perdidos: %d  %s%n",
            r1, esperado - r1, r1 == esperado ? "Sin pérdidas (con suerte)" : "FAIL — race condition detectada");

        System.out.println("\n=== Contador CON AtomicInteger ===");
        ContadorAtomico c2 = new ContadorAtomico();
        int r2 = ejecutar(threads, ops, c2::incrementar, c2::getValor);
        System.out.printf("Obtenido: %d  Esperado: %d  %s%n",
            r2, esperado, r2 == esperado ? "PASS" : "FAIL");
    }
}
