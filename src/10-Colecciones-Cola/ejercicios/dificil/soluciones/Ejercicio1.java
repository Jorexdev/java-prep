import java.util.Comparator;
import java.util.PriorityQueue;

public class Ejercicio1 {

    static class TareaProgramada {
        final String nombre;
        final long executeAt; // timestamp en segundos simulados

        TareaProgramada(String nombre, long executeAt) {
            this.nombre = nombre;
            this.executeAt = executeAt;
        }

        @Override
        public String toString() {
            return nombre + "@t=" + executeAt;
        }
    }

    public static void main(String[] args) {
        PriorityQueue<TareaProgramada> scheduler = new PriorityQueue<>(
                Comparator.comparingLong(t -> t.executeAt)
        );

        scheduler.offer(new TareaProgramada("Backup diario",    10));
        scheduler.offer(new TareaProgramada("Envío de reporte",  3));
        scheduler.offer(new TareaProgramada("Limpieza de logs",  7));
        scheduler.offer(new TareaProgramada("Health check",      1));
        scheduler.offer(new TareaProgramada("Rotación de claves",15));

        System.out.println("Simulando scheduler (avance de tiempo):");
        long tiempoActual = 0;

        while (!scheduler.isEmpty()) {
            TareaProgramada siguiente = scheduler.peek();
            // Avanzamos el reloj hasta la siguiente tarea
            tiempoActual = siguiente.executeAt;
            scheduler.poll();
            System.out.printf("  t=%2d  Ejecutando: %s%n", tiempoActual, siguiente.nombre);
        }
    }
}
