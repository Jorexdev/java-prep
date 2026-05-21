import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ejercicio2 {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 4;
        int operacionesPorThread = 1000;
        int esperado = numThreads * operacionesPorThread;

        // --- Test con HashMap (no thread-safe) ---
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        AtomicBoolean hashMapError = new AtomicBoolean(false);
        CountDownLatch latch1 = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int offset = t * operacionesPorThread;
            new Thread(() -> {
                try {
                    for (int i = 0; i < operacionesPorThread; i++) {
                        hashMap.put(offset + i, offset + i);
                    }
                } catch (Exception e) {
                    hashMapError.set(true);
                    System.out.println("HashMap ERROR: " + e.getClass().getSimpleName());
                } finally {
                    latch1.countDown();
                }
            }).start();
        }
        latch1.await();

        // --- Test con ConcurrentHashMap ---
        ConcurrentHashMap<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();
        CountDownLatch latch2 = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int offset = t * operacionesPorThread;
            new Thread(() -> {
                try {
                    for (int i = 0; i < operacionesPorThread; i++) {
                        concurrentMap.put(offset + i, offset + i);
                    }
                } finally {
                    latch2.countDown();
                }
            }).start();
        }
        latch2.await();

        System.out.println("=== ConcurrentHashMap vs HashMap ===");
        System.out.println("Entradas esperadas: " + esperado);
        System.out.println();
        System.out.println("HashMap           : " + hashMap.size() +
                           (hashMapError.get() ? " [EXCEPCION durante ejecucion]" :
                            hashMap.size() != esperado ? " [DATOS CORRUPTOS - race condition]" : " [OK - tuvo suerte]"));
        System.out.println("ConcurrentHashMap : " + concurrentMap.size() +
                           (concurrentMap.size() == esperado ? " [OK - siempre correcto]" : " [FALLO inesperado]"));
        System.out.println();
        System.out.println("HashMap no sincroniza internamente: puede corromperse o lanzar excepcion.");
        System.out.println("ConcurrentHashMap usa lock striping: seguro y eficiente.");
    }
}
