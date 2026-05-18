import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

public class Ejercicio6 {

    static class SumaRecursiva extends RecursiveTask<Long> {
        private static final int UMBRAL = 1000;
        private final int[] array;
        private final int inicio, fin;

        SumaRecursiva(int[] array, int inicio, int fin) {
            this.array = array; this.inicio = inicio; this.fin = fin;
        }

        @Override protected Long compute() {
            if (fin - inicio <= UMBRAL) {
                long suma = 0;
                for (int i = inicio; i < fin; i++) suma += array[i];
                return suma;
            }
            int mid = (inicio + fin) / 2;
            SumaRecursiva izq = new SumaRecursiva(array, inicio, mid);
            SumaRecursiva der = new SumaRecursiva(array, mid, fin);
            izq.fork();
            return der.compute() + izq.join();
        }
    }

    public static void main(String[] args) {
        int[] array = IntStream.rangeClosed(1, 10_000).toArray();
        ForkJoinPool pool = ForkJoinPool.commonPool();
        long suma = pool.invoke(new SumaRecursiva(array, 0, array.length));
        System.out.println("Suma ForkJoin: " + suma);
        System.out.println("Suma esperada: " + (long)10_000 * 10_001 / 2);
    }
}
