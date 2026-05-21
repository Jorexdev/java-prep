import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class Ejercicio3 {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, List<String>> grupos = new ConcurrentHashMap<>();
        String[] categorias = {"A", "B", "C"};
        int numThreads = 10;
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            new Thread(() -> {
                for (String cat : categorias) {
                    String item = cat + "-item-t" + threadId;
                    grupos.computeIfAbsent(cat, k -> new CopyOnWriteArrayList<>()).add(item);
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        System.out.println("=== computeIfAbsent: agrupacion concurrente ===");
        for (String cat : categorias) {
            List<String> lista = grupos.get(cat);
            System.out.println("Grupo " + cat + " -> " + lista.size() + " items " +
                               (lista.size() == numThreads ? "[OK]" : "[FALLO esperado " + numThreads + "]"));
        }
        System.out.println();
        System.out.println("Muestra de grupo A: " + grupos.get("A").subList(0, Math.min(3, grupos.get("A").size())));
    }
}
