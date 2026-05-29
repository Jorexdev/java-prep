import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// ReadWriteLock propio con wait/notifyAll — sin usar java.util.concurrent.locks

public class Ejercicio5 {

    static class MiReadWriteLock {
        private int lectoresActivos    = 0;
        private boolean escritorActivo = false;
        private int escritoresEsperando = 0;

        public synchronized void lockRead() throws InterruptedException {
            // Espera si hay escritor activo o escritores esperando (writer preference)
            while (escritorActivo || escritoresEsperando > 0) {
                wait();
            }
            lectoresActivos++;
        }

        public synchronized void unlockRead() {
            lectoresActivos--;
            if (lectoresActivos == 0) {
                notifyAll(); // notifica posibles escritores esperando
            }
        }

        public synchronized void lockWrite() throws InterruptedException {
            escritoresEsperando++;
            // Espera hasta que no haya ni lectores ni escritor activo
            while (lectoresActivos > 0 || escritorActivo) {
                wait();
            }
            escritoresEsperando--;
            escritorActivo = true;
        }

        public synchronized void unlockWrite() {
            escritorActivo = false;
            notifyAll(); // despierta tanto lectores como escritores
        }
    }

    static class DataStore {
        private final Map<String, String> datos = new HashMap<>();
        private final MiReadWriteLock lock = new MiReadWriteLock();

        // Contadores para verificar exclusión mutua
        private final AtomicInteger lectoresSimultaneos = new AtomicInteger(0);
        private final AtomicInteger escritoresSimultaneos = new AtomicInteger(0);
        private volatile int maxLectoresSimultaneos = 0;
        private volatile boolean violacionDetectada = false;

        public String read(String clave) throws InterruptedException {
            lock.lockRead();
            try {
                int n = lectoresSimultaneos.incrementAndGet();
                synchronized (this) {
                    if (n > maxLectoresSimultaneos) maxLectoresSimultaneos = n;
                    if (escritoresSimultaneos.get() > 0) {
                        violacionDetectada = true; // lector y escritor simultáneos: ¡error!
                    }
                }
                Thread.sleep(5); // simula lectura
                return datos.getOrDefault(clave, "<vacío>");
            } finally {
                lectoresSimultaneos.decrementAndGet();
                lock.unlockRead();
            }
        }

        public void write(String clave, String valor) throws InterruptedException {
            lock.lockWrite();
            try {
                int n = escritoresSimultaneos.incrementAndGet();
                if (n > 1 || lectoresSimultaneos.get() > 0) {
                    violacionDetectada = true; // varios escritores o lector+escritor: ¡error!
                }
                Thread.sleep(10); // simula escritura más costosa
                datos.put(clave, valor);
            } finally {
                escritoresSimultaneos.decrementAndGet();
                lock.unlockWrite();
            }
        }

        public boolean isViolacionDetectada() { return violacionDetectada; }
        public int getMaxLectoresSimultaneos() { return maxLectoresSimultaneos; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ReadWriteLock propio con wait/notifyAll ===\n");

        DataStore store = new DataStore();

        // Insertar datos iniciales (sin concurrencia)
        store.write("nombre", "Java");
        store.write("version", "21");
        store.write("tipo", "lenguaje");

        AtomicInteger lecturasTotales  = new AtomicInteger(0);
        AtomicInteger escriturasTotales = new AtomicInteger(0);

        long finMs = System.currentTimeMillis() + 500;

        // 6 reader threads
        Thread[] readers = new Thread[6];
        for (int i = 0; i < 6; i++) {
            final int rid = i;
            readers[i] = new Thread(() -> {
                String[] claves = {"nombre", "version", "tipo"};
                while (System.currentTimeMillis() < finMs) {
                    try {
                        String clave = claves[rid % claves.length];
                        String val = store.read(clave);
                        int n = lecturasTotales.incrementAndGet();
                        if (n % 20 == 0) {
                            System.out.println("  [Reader-" + rid + "] " + clave + "=" + val +
                                               " (total lecturas: " + n + ")");
                        }
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
            }, "Reader-" + i);
        }

        // 2 writer threads
        Thread[] writers = new Thread[2];
        for (int i = 0; i < 2; i++) {
            final int wid = i;
            writers[i] = new Thread(() -> {
                int counter = 0;
                while (System.currentTimeMillis() < finMs) {
                    try {
                        String clave = wid == 0 ? "version" : "tipo";
                        String valor = wid == 0 ? "21-update-" + counter : "lenguaje-v" + counter;
                        store.write(clave, valor);
                        int n = escriturasTotales.incrementAndGet();
                        System.out.println("  [Writer-" + wid + "] escritura #" + n + ": " + clave + "=" + valor);
                        counter++;
                        Thread.sleep(50);
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
            }, "Writer-" + i);
        }

        for (Thread r : readers) r.start();
        for (Thread w : writers) w.start();
        for (Thread r : readers) r.join();
        for (Thread w : writers) w.join();

        System.out.println("\n=== Resumen ===");
        System.out.printf("  Lecturas totales             : %,d%n", lecturasTotales.get());
        System.out.printf("  Escrituras totales           : %,d%n", escriturasTotales.get());
        System.out.printf("  Max lectores simultáneos     : %d%n",  store.getMaxLectoresSimultaneos());
        System.out.printf("  Violación de exclusión mutua : %s%n",
                          store.isViolacionDetectada() ? "SI (BUG)" : "NO (correcto)");

        System.out.println("\n=== Implementacion MiReadWriteLock ===");
        System.out.println("  lockRead():  espera si escritorActivo || escritoresEsperando > 0");
        System.out.println("  unlockRead(): notifyAll() cuando lectoresActivos llega a 0");
        System.out.println("  lockWrite(): incrementa escritoresEsperando (bloquea nuevos lectores),");
        System.out.println("               luego espera a que lectoresActivos == 0");
        System.out.println("  unlockWrite(): notifyAll() para despertar lectores y escritores");
        System.out.println("  Writer preference: los escritores tienen prioridad sobre lectores nuevos.");
    }
}
