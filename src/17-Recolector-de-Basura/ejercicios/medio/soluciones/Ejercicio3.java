import java.util.ArrayList;
import java.util.List;

public class Ejercicio3 {

    static String humanReadable(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Heap Pressure Simulation ===");
        System.out.println();

        Runtime rt = Runtime.getRuntime();
        List<byte[]> acumulador = new ArrayList<>();

        long total = rt.totalMemory();
        System.out.println("Heap total inicial: " + humanReadable(total));
        System.out.println("Umbral 20% libre: " + humanReadable((long)(total * 0.20)));
        System.out.println();
        System.out.printf("%-8s %-15s %-15s %-8s%n", "Iter", "Libre", "Usado", "Estado");
        System.out.println("-".repeat(50));

        int iter = 0;
        boolean gcActivado = false;

        while (iter < 50) {
            iter++;
            acumulador.add(new byte[512 * 1024]); // 512KB

            long libre = rt.freeMemory();
            long usado = rt.totalMemory() - libre;
            double pctLibre = (double) libre / rt.totalMemory() * 100;

            String estado = pctLibre < 20.0 ? "PRESION" : "ok";
            System.out.printf("%-8d %-15s %-15s %-8s%n",
                iter, humanReadable(libre), humanReadable(usado), estado);

            if (pctLibre < 20.0 && !gcActivado) {
                System.out.println();
                System.out.println(">>> Memoria libre < 20%. Activando GC...");
                gcActivado = true;
                acumulador.clear(); // liberar referencias
                System.gc();
                Thread.sleep(200);

                long librePost = rt.freeMemory();
                double pctPost = (double) librePost / rt.totalMemory() * 100;
                System.out.printf(">>> Después de gc: libre=%-15s (%.1f%%)%n",
                    humanReadable(librePost), pctPost);

                if (pctPost < 20.0) {
                    System.out.println(">>> Memoria sigue baja. Deteniendo.");
                    break;
                } else {
                    System.out.println(">>> Memoria recuperada. Continuando...");
                    System.out.println();
                    gcActivado = false;
                }
            }
        }

        System.out.println();
        System.out.println("Iteraciones ejecutadas: " + iter);
    }
}
