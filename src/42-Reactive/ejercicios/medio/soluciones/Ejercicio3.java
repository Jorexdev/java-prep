import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

// FlatMap concurrente: lanzar sub-consultas en paralelo y hacer merge de resultados
public class Ejercicio3 {

    private static final Random RANDOM = new Random(42);

    // Simula una consulta async que tarda entre 10 y 50ms
    static CompletableFuture<String> consultaAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            long delay = 10 + (RANDOM.nextInt(5) * 10); // 10, 20, 30, 40 o 50ms
            System.out.println("  [Query-" + id + "] Iniciada (delay=" + delay + "ms)");
            try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            String resultado = "resultado-" + id;
            System.out.println("  [Query-" + id + "] Completada → " + resultado);
            return resultado;
        });
    }

    // flatMapConcurrent: lanza todas en paralelo y hace merge en orden de llegada
    static List<String> flatMapConcurrent(List<Integer> ids) throws Exception {
        List<String> resultados = new CopyOnWriteArrayList<>();

        // Lanzar todas las consultas en paralelo
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int id : ids) {
            CompletableFuture<Void> f = consultaAsync(id)
                .thenAccept(resultado -> resultados.add(resultado));
            futures.add(f);
        }

        // Esperar a que todas completen
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return resultados;
    }

    // Para comparación: consulta secuencial (concatMap)
    static List<String> concatMapSecuencial(List<Integer> ids) throws Exception {
        List<String> resultados = new ArrayList<>();
        for (int id : ids) {
            resultados.add(consultaAsync(id).get()); // espera una por una
        }
        return resultados;
    }

    public static void main(String[] args) throws Exception {
        List<Integer> ids = List.of(1, 2, 3, 4, 5);

        System.out.println("=== flatMap concurrente (todas en paralelo) ===\n");
        long t1 = System.currentTimeMillis();
        List<String> paralelos = flatMapConcurrent(ids);
        long tiempoParalelo = System.currentTimeMillis() - t1;

        System.out.println();
        System.out.println("Resultados (orden de llegada): " + paralelos);
        System.out.printf("Tiempo flatMap concurrente: %dms%n", tiempoParalelo);

        System.out.println();
        System.out.println("=== concatMap secuencial (una por una) ===\n");
        long t2 = System.currentTimeMillis();
        List<String> secuenciales = concatMapSecuencial(ids);
        long tiempoSecuencial = System.currentTimeMillis() - t2;

        System.out.println();
        System.out.println("Resultados (orden de ID): " + secuenciales);
        System.out.printf("Tiempo concatMap secuencial: %dms%n", tiempoSecuencial);

        System.out.println();
        System.out.println("=== Comparativa ===");
        System.out.printf("Paralelo:    %dms%n", tiempoParalelo);
        System.out.printf("Secuencial:  %dms%n", tiempoSecuencial);
        System.out.printf("Speedup:     %.1fx%n", (double) tiempoSecuencial / tiempoParalelo);
        System.out.println();
        System.out.println("flatMap:    orden de llegada (más rápido, sin orden garantizado)");
        System.out.println("concatMap:  orden de IDs (secuencial, más lento)");
        System.out.println("switchMap:  cancela el anterior al llegar el siguiente (typeahead)");
    }
}
