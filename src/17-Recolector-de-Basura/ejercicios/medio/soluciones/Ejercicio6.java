import java.util.*;

public class Ejercicio6 {

    // Objetos short-lived: se crean y descartan rapidamente (candidatos a Young generation)
    static class ShortLived {
        private final byte[] data;
        private final String name;

        ShortLived(int id, int sizeKb) {
            this.data = new byte[sizeKb * 1024];
            this.name = "short-" + id;
        }
    }

    // Objetos long-lived: se retienen en memoria durante toda la vida del programa
    static class LongLived {
        private final byte[] data;
        private final String name;

        LongLived(int id, int sizeKb) {
            this.data = new byte[sizeKb * 1024];
            this.name = "long-" + id;
        }
    }

    static Runtime RT = Runtime.getRuntime();

    static long heapUsedMB() {
        return (RT.totalMemory() - RT.freeMemory()) / (1024 * 1024);
    }

    static void printMemory(String label) {
        System.out.printf("  %-38s | heap usado: %3d MB%n", label, heapUsedMB());
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== GC Generacional: Short-lived vs Long-lived ===");
        System.out.println();

        List<LongLived> longLivedList = new ArrayList<>();

        // --- Fase 1: solo short-lived ---
        System.out.println("[ Fase 1 ] Creando SOLO objetos short-lived (10 rondas x 100 objetos de 10KB)");
        System.out.println("  -> Deben acumularse en Young generation y recogerse rapidamente");
        System.out.println();

        long t1 = System.currentTimeMillis();
        for (int ronda = 1; ronda <= 10; ronda++) {
            List<ShortLived> batch = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                batch.add(new ShortLived(i, 10)); // 10 KB cada uno = 1 MB por ronda
            }
            // batch sale de scope: todos son elegibles para GC
            printMemory("Ronda " + ronda + " creados, batch descartado");
        }
        System.gc();
        Thread.sleep(100);
        printMemory("Tras GC (short-lived)");
        System.out.printf("  Tiempo fase 1: %d ms%n%n", System.currentTimeMillis() - t1);

        // --- Fase 2: solo long-lived ---
        System.out.println("[ Fase 2 ] Creando SOLO objetos long-lived (50 objetos de 500KB)");
        System.out.println("  -> Se retienen en Old generation, el GC NO puede liberarlos");
        System.out.println();

        long t2 = System.currentTimeMillis();
        for (int i = 1; i <= 50; i++) {
            longLivedList.add(new LongLived(i, 500)); // 500 KB = 25 MB total
            if (i % 10 == 0) printMemory("Long-lived acumulados: " + i);
        }
        System.gc();
        Thread.sleep(100);
        printMemory("Tras GC (long-lived, lista aun activa)");
        System.out.printf("  Tiempo fase 2: %d ms%n%n", System.currentTimeMillis() - t2);

        // --- Fase 3: mezcla (patron tipico de aplicacion real) ---
        System.out.println("[ Fase 3 ] Patron mixto: long-lived + rafagas de short-lived");
        System.out.println("  -> Minor GC limpia short-lived; long-lived permanece en Old gen");
        System.out.println();

        long t3 = System.currentTimeMillis();
        for (int ronda = 1; ronda <= 5; ronda++) {
            // short-lived descartados inmediatamente
            List<ShortLived> burst = new ArrayList<>();
            for (int i = 0; i < 50; i++) burst.add(new ShortLived(i, 20));
            printMemory("Ronda " + ronda + " burst descartado");
        }
        System.gc();
        Thread.sleep(100);
        printMemory("Tras GC final (mezcla)");
        System.out.printf("  Tiempo fase 3: %d ms%n%n", System.currentTimeMillis() - t3);

        // --- Liberamos long-lived ---
        System.out.println("[ Fase 4 ] Liberando long-lived (vaciamos la lista)");
        longLivedList.clear();
        System.gc();
        Thread.sleep(100);
        printMemory("Tras liberar long-lived + GC");

        System.out.println();
        System.out.println("=== Conclusiones ===");
        System.out.println("Short-lived: viven en Young gen (Eden/Survivor).");
        System.out.println("  Minor GC los recoge rapido y a bajo coste.");
        System.out.println("Long-lived : sobreviven minor GCs y promueven a Old gen.");
        System.out.println("  Solo se liberan en Major GC (mas costoso, mas raro).");
        System.out.println("GC generacional es eficiente porque la mayoria de objetos");
        System.out.println("  son short-lived (hipotesis generacional debil).");
    }
}
