import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Ejercicio3 {

    static class MergeSortAction extends RecursiveAction {
        private static final int UMBRAL = 2000;
        private final int[] array;
        private final int inicio, fin;

        MergeSortAction(int[] array, int inicio, int fin) {
            this.array = array; this.inicio = inicio; this.fin = fin;
        }

        @Override protected void compute() {
            if (fin - inicio <= UMBRAL) { Arrays.sort(array, inicio, fin); return; }
            int mid = (inicio + fin) / 2;
            MergeSortAction izq = new MergeSortAction(array, inicio, mid);
            MergeSortAction der = new MergeSortAction(array, mid, fin);
            izq.fork(); der.compute(); izq.join();
            merge(array, inicio, mid, fin);
        }

        static void merge(int[] a, int lo, int mid, int hi) {
            int[] tmp = Arrays.copyOfRange(a, lo, hi);
            int i = 0, j = mid - lo, k = lo;
            while (i < mid - lo && j < hi - lo)
                a[k++] = tmp[i] <= tmp[j] ? tmp[i++] : tmp[j++];
            while (i < mid - lo) a[k++] = tmp[i++];
            while (j < hi - lo)  a[k++] = tmp[j++];
        }
    }

    public static void main(String[] args) {
        int n = 100_000;
        int[] forkArray = new java.util.Random(42).ints(n, 0, 1_000_000).toArray();
        int[] stdArray  = forkArray.clone();

        long t0 = System.currentTimeMillis();
        ForkJoinPool.commonPool().invoke(new MergeSortAction(forkArray, 0, forkArray.length));
        System.out.println("ForkJoin MergeSort: " + (System.currentTimeMillis() - t0) + "ms");

        long t1 = System.currentTimeMillis();
        Arrays.sort(stdArray);
        System.out.println("Arrays.sort:        " + (System.currentTimeMillis() - t1) + "ms");
        System.out.println("Resultados iguales: " + Arrays.equals(forkArray, stdArray));
    }
}
