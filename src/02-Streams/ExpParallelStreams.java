import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.*;

public class ExpParallelStreams {

    public static void main(String[] args) {

        // ======================================
        // 1. COMPARATIVA DE RENDIMIENTO — suma secuencial vs paralela
        //    CPU-bound + datos grandes = candidato ideal para parallelStream
        // ======================================

        int N = 10_000_000;
        List<Long> numeros = LongStream.rangeClosed(1, N)
                .boxed()
                .collect(Collectors.toList());

        long t0 = System.nanoTime();
        long sumaSecuencial = numeros.stream()
                .mapToLong(Long::longValue)
                .sum();
        long msSecuencial = (System.nanoTime() - t0) / 1_000_000;

        long t1 = System.nanoTime();
        long sumaParalela = numeros.parallelStream()
                .mapToLong(Long::longValue)
                .sum();
        long msParalela = (System.nanoTime() - t1) / 1_000_000;

        System.out.println("Suma secuencial: " + sumaSecuencial + " en " + msSecuencial + "ms");
        System.out.println("Suma paralela:   " + sumaParalela   + " en " + msParalela   + "ms");
        System.out.println("Mismo resultado: " + (sumaSecuencial == sumaParalela));

        // ======================================
        // 2. PROBLEMA DE ORDEN — forEach vs forEachOrdered
        // ======================================

        List<Integer> pequeño = List.of(1, 2, 3, 4, 5);

        System.out.print("\nparallel forEach (orden NO garantizado): ");
        pequeño.parallelStream()
                .forEach(n -> System.out.print(n + " "));

        System.out.print("\nparallel forEachOrdered (orden garantizado): ");
        pequeño.parallelStream()
                .forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();

        // ======================================
        // 3. ACUMULACIÓN THREAD-UNSAFE — bug clásico y su corrección
        // ======================================

        // MAL: ArrayList no es thread-safe; en paralelo puede perder elementos o lanzar excepción
        List<Integer> listaBuggy = new ArrayList<>();
        try {
            IntStream.range(0, 1000)
                    .parallel()
                    .forEach(listaBuggy::add); // condición de carrera sobre ArrayList
        } catch (Exception e) {
            System.out.println("Error esperado con ArrayList: " + e.getClass().getSimpleName());
        }
        System.out.println("ArrayList buggy: " + listaBuggy.size() + " elementos (puede ser <1000)");

        // BIEN: el collector gestiona la combinación de resultados internamente y es seguro
        List<Integer> listaSafe = IntStream.range(0, 1000)
                .parallel()
                .boxed()
                .collect(Collectors.toList()); // colector thread-safe: usa contenedores locales + merge
        System.out.println("Collector seguro: " + listaSafe.size() + " elementos");

        // ======================================
        // 4. CUÁNDO NO USAR PARALLEL — datos pequeños / overhead mayor que beneficio
        // ======================================

        List<String> pocos = List.of("Spring", "Kafka", "Docker");

        long t2 = System.nanoTime();
        pocos.stream().map(String::toUpperCase).toList();
        long nsSecuencialPeq = System.nanoTime() - t2;

        long t3 = System.nanoTime();
        pocos.parallelStream().map(String::toUpperCase).toList();
        long nsParalelaPeq = System.nanoTime() - t3;

        System.out.println("\nDatos pequeños (3 elementos):");
        System.out.println("  secuencial: " + nsSecuencialPeq + "ns");
        System.out.println("  paralela:   " + nsParalelaPeq   + "ns  (suele ser mayor por overhead)");

        // ======================================
        // 5. POOL FORK/JOIN — información del entorno
        //    parallelStream usa ForkJoinPool.commonPool() por defecto
        // ======================================

        int hilos = java.util.concurrent.ForkJoinPool.commonPool().getParallelism();
        System.out.println("\nForkJoinPool.commonPool().getParallelism() = " + hilos);
        System.out.println("(coincide con Runtime.getRuntime().availableProcessors() - 1 aprox.)");
    }
}
