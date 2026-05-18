import java.util.concurrent.*;

public class Ejercicio2 {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        int[] valores = {100, 1000, 10000};
        Future<Long>[] futuros = new Future[3];

        for (int i = 0; i < 3; i++) {
            final int n = valores[i];
            futuros[i] = executor.submit(() -> {
                long suma = 0;
                for (int j = 1; j <= n; j++) suma += j;
                return suma;
            });
        }

        for (int i = 0; i < 3; i++)
            System.out.println("Suma(1.." + valores[i] + ") = " + futuros[i].get());

        executor.shutdown();
    }
}
