import java.util.*;

public class Ejercicio3 {

    record Stats(String nombre, long minNs, long maxNs, long avgNs, long p95Ns) {
        @Override public String toString() {
            return String.format("%-28s  avg=%7.3fms  p95=%7.3fms  min=%7.3fms  max=%7.3fms",
                nombre, avgNs / 1e6, p95Ns / 1e6, minNs / 1e6, maxNs / 1e6);
        }
    }

    static Stats benchmark(String nombre, Runnable fn, int iteraciones) {
        int warmup = Math.max(10, iteraciones / 10);
        for (int i = 0; i < warmup; i++) fn.run();

        long[] times = new long[iteraciones];
        for (int i = 0; i < iteraciones; i++) {
            long t = System.nanoTime();
            fn.run();
            times[i] = System.nanoTime() - t;
        }
        Arrays.sort(times);
        return new Stats(
            nombre,
            times[0],
            times[iteraciones - 1],
            Arrays.stream(times).sum() / iteraciones,
            times[(int) (iteraciones * 0.95)]
        );
    }

    public static void main(String[] args) {
        int N = 100_000;
        int buscar = N / 2;

        List<Integer> lista = new ArrayList<>(N);
        Set<Integer>  set   = new HashSet<>(N);
        for (int i = 0; i < N; i++) { lista.add(i); set.add(i); }

        System.out.println("Benchmarking búsqueda de " + buscar + " en N=" + N + " (1000 iteraciones)");
        System.out.println();

        Stats s1 = benchmark("List.contains O(n)",     () -> lista.contains(buscar), 1000);
        Stats s2 = benchmark("HashSet.contains O(1)",  () -> set.contains(buscar),   1000);

        System.out.println(s1);
        System.out.println(s2);
        System.out.printf("%nHashSet es ~%.0fx más rápido en promedio%n",
            (double) s1.avgNs() / Math.max(1, s2.avgNs()));

        System.out.println("\n--- Búsqueda de elemento ausente (worst case para List) ---");
        int ausente = N + 1;
        Stats s3 = benchmark("List.contains ausente",    () -> lista.contains(ausente), 1000);
        Stats s4 = benchmark("HashSet.contains ausente", () -> set.contains(ausente),   1000);
        System.out.println(s3);
        System.out.println(s4);
    }
}
