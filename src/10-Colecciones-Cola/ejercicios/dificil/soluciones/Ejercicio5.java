import java.util.Comparator;
import java.util.PriorityQueue;

// TaskScheduler: programa tareas con delay y prioridad usando PriorityQueue

public class Ejercicio5 {

    static class Task {
        final String nombre;
        final int prioridad;       // mayor valor = antes
        final long executeAt;      // System.currentTimeMillis() + delayMs

        Task(String nombre, int prioridad, long executeAt) {
            this.nombre    = nombre;
            this.prioridad = prioridad;
            this.executeAt = executeAt;
        }
    }

    static class TaskScheduler {
        // Ordena primero por executeAt (más próximo primero), luego por prioridad desc
        private final PriorityQueue<Task> queue = new PriorityQueue<>(
            Comparator.comparingLong((Task t) -> t.executeAt)
                      .thenComparingInt(t -> -t.prioridad)
        );

        private int totalScheduled = 0;
        private int totalExecuted  = 0;

        public void schedule(String nombre, int prioridad, long delayMs) {
            long executeAt = System.currentTimeMillis() + delayMs;
            queue.offer(new Task(nombre, prioridad, executeAt));
            totalScheduled++;
        }

        /**
         * Ejecuta todas las tareas cuyo executeAt <= ahora, en orden de prioridad
         * descendente entre las que ya son elegibles.
         * Retorna el número de tareas ejecutadas en esta llamada.
         */
        public int runPending() {
            long now = System.currentTimeMillis();
            int ejecutadas = 0;

            // Extraer todas las tareas elegibles
            PriorityQueue<Task> pendientes = new PriorityQueue<>(
                Comparator.comparingInt((Task t) -> -t.prioridad)
            );
            while (!queue.isEmpty() && queue.peek().executeAt <= now) {
                pendientes.offer(queue.poll());
            }

            // Ejecutarlas en orden de prioridad descendente
            while (!pendientes.isEmpty()) {
                Task t = pendientes.poll();
                long retraso = System.currentTimeMillis() - t.executeAt;
                System.out.printf("  [EJECUTADO] %-20s prioridad=%2d retardo=+%dms%n",
                                  t.nombre, t.prioridad, retraso);
                ejecutadas++;
                totalExecuted++;
            }
            return ejecutadas;
        }

        public boolean hasPending() {
            return !queue.isEmpty();
        }

        public int totalScheduled() { return totalScheduled; }
        public int totalExecuted()  { return totalExecuted; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== TaskScheduler con prioridad y delay ===\n");

        TaskScheduler scheduler = new TaskScheduler();
        long t0 = System.currentTimeMillis();

        // 10 tareas con distintas prioridades y delays (0-500ms)
        scheduler.schedule("Tarea-critica-A",  10,   0);
        scheduler.schedule("Tarea-critica-B",   9,   0);
        scheduler.schedule("Tarea-alta-C",      7,  50);
        scheduler.schedule("Tarea-alta-D",      7,  50);
        scheduler.schedule("Tarea-media-E",     5, 100);
        scheduler.schedule("Tarea-media-F",     5, 200);
        scheduler.schedule("Tarea-baja-G",      3, 200);
        scheduler.schedule("Tarea-baja-H",      2, 300);
        scheduler.schedule("Tarea-minima-I",    1, 400);
        scheduler.schedule("Tarea-minima-J",    1, 500);

        System.out.println("Tareas programadas: " + scheduler.totalScheduled());
        System.out.println("Iniciando bucle de polling...\n");

        // Polling hasta que todas se ejecuten
        while (scheduler.hasPending() || scheduler.totalExecuted() < scheduler.totalScheduled()) {
            int ejecutadas = scheduler.runPending();
            if (ejecutadas == 0 && scheduler.hasPending()) {
                Thread.sleep(20); // esperar antes del siguiente poll
            }
            if (!scheduler.hasPending()) break;
        }
        // Un último intento para capturar las últimas
        Thread.sleep(20);
        scheduler.runPending();

        long elapsed = System.currentTimeMillis() - t0;

        System.out.println("\n=== Resumen ===");
        System.out.println("Tiempo total     : " + elapsed + "ms");
        System.out.println("Tareas ejecutadas: " + scheduler.totalExecuted() + " / " + scheduler.totalScheduled());
        System.out.println("\nConclusion: PriorityQueue garantiza O(log n) en offer/poll.");
        System.out.println("Dentro de una misma ventana temporal se respeta la prioridad descendente.");
    }
}
