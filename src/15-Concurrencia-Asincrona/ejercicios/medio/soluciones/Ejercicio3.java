import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio3 {
    public static void main(String[] args) throws Exception {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        AtomicInteger contador = new AtomicInteger(0);

        scheduler.schedule(() -> System.out.println("Tarea única ejecutada tras 500ms"), 500, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            int n = contador.incrementAndGet();
            System.out.println("Tarea repetida #" + n);
        }, 0, 200, TimeUnit.MILLISECONDS);

        Thread.sleep(2000);
        scheduler.shutdown();
        System.out.println("Scheduler detenido");
    }
}
