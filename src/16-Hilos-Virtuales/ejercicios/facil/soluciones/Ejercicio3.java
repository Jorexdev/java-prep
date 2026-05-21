import java.util.ArrayList;
import java.util.List;

public class Ejercicio3 {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 1000;
        int sleepMs = 50;

        System.out.println("=== I/O simulado con Virtual Threads ===");
        System.out.printf("Lanzando %d virtual threads, cada uno duerme %dms...%n%n",
                          numThreads, sleepMs);

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

        System.out.println("=== Resultado ===");
        System.out.println("Threads lanzados : " + numThreads);
        System.out.println("Sleep por thread : " + sleepMs + "ms");
        System.out.println("Tiempo total real: " + elapsed + "ms");
        System.out.println();
        System.out.println("Si fueran platform threads bloqueando OS threads:");
        System.out.println("  necesitariamos " + numThreads + " OS threads o habria espera.");
        System.out.println("Con virtual threads:");
        System.out.println("  JVM desmonta el thread del carrier durante el sleep.");
        System.out.println("  El carrier puede ejecutar otro virtual thread.");
        System.out.println("  Resultado: ~" + sleepMs + "ms total, no " + (numThreads * sleepMs / 1000) + "s.");
        System.out.println();
        boolean esCorrecto = elapsed < (sleepMs * 3); // margen generoso
        System.out.println("Tiempo cercano a " + sleepMs + "ms: " + esCorrecto + " (" + elapsed + "ms)");
    }
}
