import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Ejercicio4 {
    public static void main(String[] args) throws Exception {
        int atletas = 4;
        int fases = 3;
        CyclicBarrier barrera = new CyclicBarrier(atletas, () -> System.out.println("--- Fase completada: todos listos ---"));

        for (int a = 1; a <= atletas; a++) {
            final int id = a;
            new Thread(() -> {
                try {
                    for (int f = 1; f <= fases; f++) {
                        long tiempo = (long)(Math.random() * 200 + 100);
                        Thread.sleep(tiempo);
                        System.out.println("Atleta-" + id + " completó fase " + f + " en " + tiempo + "ms");
                        barrera.await();
                    }
                    System.out.println("Atleta-" + id + " terminó la carrera");
                } catch (InterruptedException | BrokenBarrierException e) { Thread.currentThread().interrupt(); }
            }).start();
        }
    }
}
