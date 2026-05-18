import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Ejercicio1 {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 1; i <= 6; i++) {
            final int id = i;
            executor.submit(() -> System.out.println("Tarea-" + id + " en " + Thread.currentThread().getName()));
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Todas completadas");
    }
}
