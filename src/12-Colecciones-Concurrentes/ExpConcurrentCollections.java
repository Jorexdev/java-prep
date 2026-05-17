import java.util.concurrent.*;
import java.util.*;
import java.util.stream.IntStream;

public class ExpConcurrentCollections {

    public static void main(String[] args) throws InterruptedException {

        // ========== ConcurrentHashMap: contadores concurrentes ==========
        ConcurrentHashMap<String, Integer> hits = new ConcurrentHashMap<>();

        Runnable contarA = () -> IntStream.range(0, 100_000)
                .forEach(i -> hits.merge("A", 1, Integer::sum)); // atómico

        Runnable contarB = () -> IntStream.range(0, 100_000)
                .forEach(i -> hits.compute("B", (k, v) -> v == null ? 1 : v + 1)); // atómico

        Thread t1 = new Thread(contarA);
        Thread t2 = new Thread(contarA);
        Thread t3 = new Thread(contarB);
        t1.start(); t2.start(); t3.start();
        t1.join();  t2.join();  t3.join();

        // Iterador weakly-consistent: no lanza C.M.E.
        hits.forEach((k, v) -> System.out.println(k + " -> " + v));

        // ========== CopyOnWriteArrayList: muchas lecturas, pocas escrituras ==========
        CopyOnWriteArrayList<String> lista = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));

        // Lectura concurrente segura
        Runnable lector = () -> lista.forEach(s -> {}); // snapshot
        Runnable escritor = () -> lista.add("x");       // crea copia

        Thread l1 = new Thread(lector);
        Thread l2 = new Thread(lector);
        Thread e1 = new Thread(escritor);
        l1.start(); l2.start(); e1.start();
        l1.join();  l2.join();  e1.join();

        System.out.println("COWAL -> " + lista); // incluye "x"
    }
}
