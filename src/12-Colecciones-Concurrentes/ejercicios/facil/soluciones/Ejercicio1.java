import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Ejercicio1 {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("a", 0);
        map.put("b", 0);
        map.put("c", 0);

        int numThreads = 5;
        int incrementsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            new Thread(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    map.merge("a", 1, Integer::sum);
                    map.merge("b", 1, Integer::sum);
                    map.merge("c", 1, Integer::sum);
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        System.out.println("=== ConcurrentHashMap basics ===");
        System.out.println("Threads: " + numThreads + ", incrementos por thread: " + incrementsPerThread);
        System.out.println("Esperado por clave: " + (numThreads * incrementsPerThread));
        System.out.println();
        System.out.println("a = " + map.get("a") + (map.get("a") == 5000 ? " [OK]" : " [FALLO]"));
        System.out.println("b = " + map.get("b") + (map.get("b") == 5000 ? " [OK]" : " [FALLO]"));
        System.out.println("c = " + map.get("c") + (map.get("c") == 5000 ? " [OK]" : " [FALLO]"));
    }
}
