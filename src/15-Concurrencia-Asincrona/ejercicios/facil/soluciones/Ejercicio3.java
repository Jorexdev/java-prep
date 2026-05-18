import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Ejercicio3 {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Callable<String>> tareas = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int id = i;
            tareas.add(() -> { Thread.sleep(100); return "Tarea-" + id + " completada"; });
        }

        long t0 = System.currentTimeMillis();
        List<Future<String>> resultados = executor.invokeAll(tareas);
        long elapsed = System.currentTimeMillis() - t0;

        for (Future<String> f : resultados) System.out.println(f.get());
        System.out.println("Tiempo paralelo: " + elapsed + "ms (vs ~500ms secuencial)");
        executor.shutdown();
    }
}
