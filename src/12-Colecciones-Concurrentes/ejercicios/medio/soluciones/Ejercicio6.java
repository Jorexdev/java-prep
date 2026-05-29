import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;

// StampedLock: optimistic read con fallback a read lock
// Clave: la lectura optimista NO bloquea, valida después. Si hubo escritura concurrente, fallback.

public class Ejercicio6 {

    static class PuntoMutable {
        private double x;
        private double y;
        private final StampedLock lock = new StampedLock();

        PuntoMutable(double x, double y) {
            this.x = x;
            this.y = y;
        }

        // Optimistic read: intenta sin lock, valida, hace fallback si es necesario
        double[] leer(AtomicLong optimistaExito, AtomicLong fallbacks) {
            long stamp = lock.tryOptimisticRead();
            double lx = x;
            double ly = y;

            if (!lock.validate(stamp)) {
                // Hubo escritura concurrente → fallback a read lock
                fallbacks.incrementAndGet();
                stamp = lock.readLock();
                try {
                    lx = x;
                    ly = y;
                } finally {
                    lock.unlockRead(stamp);
                }
            } else {
                optimistaExito.incrementAndGet();
            }
            return new double[]{lx, ly};
        }

        void mover(double dx, double dy) {
            long stamp = lock.writeLock();
            try {
                x += dx;
                y += dy;
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== StampedLock con optimistic read ===\n");

        PuntoMutable punto = new PuntoMutable(0.0, 0.0);

        AtomicLong optimistaExito = new AtomicLong(0);
        AtomicLong fallbacks      = new AtomicLong(0);
        AtomicInteger escrituras  = new AtomicInteger(0);
        AtomicInteger lecturas    = new AtomicInteger(0);

        long finMs = System.currentTimeMillis() + 300;

        // 6 reader threads
        Thread[] readers = new Thread[6];
        for (int i = 0; i < 6; i++) {
            readers[i] = new Thread(() -> {
                while (System.currentTimeMillis() < finMs) {
                    double[] pos = punto.leer(optimistaExito, fallbacks);
                    lecturas.incrementAndGet();
                    // Simula uso del dato leído
                    if (Double.isNaN(pos[0])) throw new IllegalStateException("dato corrupto");
                }
            }, "Reader-" + i);
        }

        // 2 writer threads
        Thread[] writers = new Thread[2];
        for (int i = 0; i < 2; i++) {
            final int wid = i;
            writers[i] = new Thread(() -> {
                while (System.currentTimeMillis() < finMs) {
                    punto.mover(wid + 0.1, wid + 0.1);
                    escrituras.incrementAndGet();
                    try { Thread.sleep(1); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Writer-" + i);
        }

        for (Thread t : readers) t.start();
        for (Thread t : writers) t.start();
        for (Thread t : readers) t.join();
        for (Thread t : writers) t.join();

        long totalLecturas = optimistaExito.get() + fallbacks.get();
        double pctOptimista = totalLecturas > 0
            ? (double) optimistaExito.get() / totalLecturas * 100 : 0;

        System.out.println("Resultados tras 300ms con 6 readers / 2 writers:");
        System.out.printf("  Lecturas totales      : %,d%n", lecturas.get());
        System.out.printf("  Optimistas exitosas   : %,d%n", optimistaExito.get());
        System.out.printf("  Fallbacks a readLock  : %,d%n", fallbacks.get());
        System.out.printf("  Escrituras totales    : %,d%n", (long) escrituras.get());
        System.out.printf("  Exito optimista       : %.1f%%%n", pctOptimista);

        System.out.println("\n=== Cuando usar StampedLock vs ReadWriteLock ===");
        System.out.println("StampedLock es más rápido cuando:");
        System.out.println("  - Las lecturas son frecuentes y las escrituras son raras");
        System.out.println("  - El cuerpo de la lectura es muy corto (< 10 instrucciones)");
        System.out.println("  - El porcentaje de exito optimista > 90%");
        System.out.println("ReadWriteLock es preferible cuando:");
        System.out.println("  - Las escrituras son frecuentes (alto % de fallbacks)");
        System.out.println("  - El cuerpo de la lectura contiene llamadas a métodos complejos");
        System.out.println("  - Se necesita reentrancia (StampedLock NO es reentrante)");
    }
}
