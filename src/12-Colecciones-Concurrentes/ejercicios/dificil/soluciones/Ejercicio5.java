import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

// Phaser multi-fase para pipeline paralelo: carga → transformación → almacenamiento
// El Phaser sincroniza la barrera entre fases; el worker-0 abandona en transformación.

public class Ejercicio5 {

    static final int NUM_WORKERS = 4;
    static final int ITEMS_TOTAL = 12;  // 3 items por worker

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Phaser multi-fase: pipeline paralelo ===\n");

        // Phaser con NUM_WORKERS participantes registrados
        Phaser phaser = new Phaser(NUM_WORKERS) {
            // Llamado automáticamente cuando todos avanzan de fase
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                String[] nombres = {"CARGA", "TRANSFORMACION", "ALMACENAMIENTO"};
                if (phase < nombres.length) {
                    System.out.println("\n  [PHASER] Fase " + phase + " (" + nombres[phase] +
                                       ") completada. Participantes: " + registeredParties +
                                       ". Avanzando a fase " + (phase + 1) + "...\n");
                }
                // Terminar automáticamente después de la fase 2 (almacenamiento)
                return phase >= 2;
            }
        };

        // Distribuir items: cada worker procesa ITEMS_TOTAL/NUM_WORKERS items
        int itemsPorWorker = ITEMS_TOTAL / NUM_WORKERS;
        List<String>[] itemsDeWorker = new List[NUM_WORKERS];
        for (int w = 0; w < NUM_WORKERS; w++) {
            itemsDeWorker[w] = new ArrayList<>();
            for (int i = 0; i < itemsPorWorker; i++) {
                itemsDeWorker[w].add("item-" + (w * itemsPorWorker + i));
            }
        }

        AtomicLong inicio = new AtomicLong(System.currentTimeMillis());
        long[] tiempoFase = new long[3];

        Thread[] workers = new Thread[NUM_WORKERS];
        for (int w = 0; w < NUM_WORKERS; w++) {
            final int wid = w;
            final List<String> items = itemsDeWorker[w];
            workers[w] = new Thread(() -> {
                // ── FASE 0: CARGA ──────────────────────────────────────────
                for (String item : items) {
                    System.out.printf("  [Worker-%d | CARGA      ] procesando %s%n", wid, item);
                    try { Thread.sleep(10 + wid * 5); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); return;
                    }
                }
                System.out.printf("  [Worker-%d] carga completa → esperando barrera fase 0%n", wid);
                long t0 = System.currentTimeMillis();
                phaser.arriveAndAwaitAdvance(); // barrera fase 0→1
                if (wid == 0) tiempoFase[0] = System.currentTimeMillis() - inicio.get();

                // ── FASE 1: TRANSFORMACIÓN ─────────────────────────────────
                if (wid == 0) {
                    // Worker-0 simula fallo y abandona el Phaser
                    System.out.println("  [Worker-0] FALLO en transformacion → arriveAndDeregister()");
                    phaser.arriveAndDeregister();
                    return;
                }
                for (String item : items) {
                    System.out.printf("  [Worker-%d | TRANSFORM  ] procesando %s → %s%n",
                                      wid, item, item.toUpperCase());
                    try { Thread.sleep(15); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); return;
                    }
                }
                System.out.printf("  [Worker-%d] transformacion completa → esperando barrera fase 1%n", wid);
                phaser.arriveAndAwaitAdvance(); // barrera fase 1→2
                if (wid == 1) tiempoFase[1] = System.currentTimeMillis() - inicio.get();

                // ── FASE 2: ALMACENAMIENTO ─────────────────────────────────
                for (String item : items) {
                    System.out.printf("  [Worker-%d | ALMACENAM  ] guardando %s%n", wid, item.toUpperCase());
                    try { Thread.sleep(8); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); return;
                    }
                }
                System.out.printf("  [Worker-%d] almacenamiento completo → esperando barrera fase 2%n", wid);
                phaser.arriveAndAwaitAdvance(); // barrera final
                if (wid == 1) tiempoFase[2] = System.currentTimeMillis() - inicio.get();

            }, "Worker-" + w);
        }

        for (Thread t : workers) t.start();
        for (Thread t : workers) t.join();

        long total = System.currentTimeMillis() - inicio.get();

        System.out.println("\n=== Tiempos de fase ===");
        System.out.println("  Fin Carga           : " + tiempoFase[0] + "ms");
        System.out.println("  Fin Transformacion  : " + tiempoFase[1] + "ms");
        System.out.println("  Fin Almacenamiento  : " + tiempoFase[2] + "ms");
        System.out.println("  Tiempo total        : " + total + "ms");
        System.out.println("\n=== Ventaja de Phaser vs CyclicBarrier ===");
        System.out.println("  - Phaser permite añadir/quitar participantes dinámicamente");
        System.out.println("  - arriveAndDeregister() permite que worker-0 abandone sin bloquear a los demás");
        System.out.println("  - getPhase() permite consultar la fase actual en tiempo de ejecución");
        System.out.println("  - onAdvance() es el hook para lógica entre fases");
    }
}
