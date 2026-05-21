import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio6 {
    static volatile int contadorNormal = 0;
    static AtomicInteger contadorAtomico = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 10;
        int incrementsPorThread = 1000;
        int esperado = numThreads * incrementsPorThread;

        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            new Thread(() -> {
                for (int i = 0; i < incrementsPorThread; i++) {
                    contadorNormal++;           // race condition: read-modify-write no atomico
                    contadorAtomico.incrementAndGet(); // atomico: siempre correcto
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        System.out.println("=== AtomicInteger vs int normal ===");
        System.out.println("Esperado: " + esperado);
        System.out.println();
        System.out.println("int volatile:   " + contadorNormal +
                           (contadorNormal == esperado ? " [OK - tuvo suerte]" : " [INCORRECTO - race condition]"));
        System.out.println("AtomicInteger:  " + contadorAtomico.get() +
                           (contadorAtomico.get() == esperado ? " [OK - siempre correcto]" : " [FALLO inesperado]"));
        System.out.println();
        System.out.println("Nota: volatile garantiza visibilidad pero NO atomicidad en read-modify-write.");
        System.out.println("AtomicInteger usa CAS (Compare-And-Swap) a nivel de CPU -> siempre correcto.");
    }
}
