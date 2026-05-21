import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ejercicio5 {
    public static void main(String[] args) throws InterruptedException {
        // Clave: score (descendente). Valor: nombre del jugador.
        // Usamos Integer.reverseOrder() para ordenar mayor score primero.
        ConcurrentSkipListMap<Integer, String> leaderboard =
            new ConcurrentSkipListMap<>(Comparator.reverseOrder());

        String[] jugadores = {"Ana","Bob","Carlos","Diana","Eva","Felix","Gema","Hugo","Iris","Jorge"};

        // Insertar scores iniciales
        Random rng = new Random(42);
        for (int i = 0; i < jugadores.length; i++) {
            leaderboard.put(rng.nextInt(100) + 1, jugadores[i]);
        }

        AtomicBoolean running = new AtomicBoolean(true);
        CountDownLatch updatesDone = new CountDownLatch(5);

        // 5 threads actualizan scores
        for (int t = 0; t < 5; t++) {
            final int tid = t;
            new Thread(() -> {
                Random r = new Random(tid);
                for (int i = 0; i < 20; i++) {
                    String jugador = jugadores[r.nextInt(jugadores.length)];
                    int score = r.nextInt(100) + 1;
                    leaderboard.put(score, jugador);
                    try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
                updatesDone.countDown();
            }).start();
        }

        // 1 thread lee el top-3 cada 50ms
        Thread reader = new Thread(() -> {
            int reads = 0;
            while (running.get() && reads < 4) {
                System.out.println("\n--- Top-3 (lectura " + (reads + 1) + ") ---");
                int rank = 1;
                for (Map.Entry<Integer, String> entry : leaderboard.entrySet()) {
                    if (rank > 3) break;
                    System.out.println("  " + rank + ". " + entry.getValue() + " -> " + entry.getKey() + " pts");
                    rank++;
                }
                reads++;
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        reader.start();

        updatesDone.await();
        running.set(false);
        reader.join();

        System.out.println("\n=== Leaderboard final completo ===");
        int rank = 1;
        for (Map.Entry<Integer, String> entry : leaderboard.entrySet()) {
            System.out.println("  " + rank + ". " + entry.getValue() + " -> " + entry.getKey() + " pts");
            rank++;
        }
        System.out.println("\nConcurrentSkipListMap: siempre ordenado, thread-safe, sin locks globales.");
    }
}
