import java.util.PriorityQueue;

public class Ejercicio1 {

    static class Tarea implements Comparable<Tarea> {
        private final String nombre;
        private final int prioridad; // mayor número = mayor prioridad

        Tarea(String nombre, int prioridad) {
            this.nombre = nombre;
            this.prioridad = prioridad;
        }

        @Override
        public int compareTo(Tarea otra) {
            // Orden inverso: mayor prioridad sale antes del heap
            return Integer.compare(otra.prioridad, this.prioridad);
        }

        @Override
        public String toString() {
            return nombre + "[prio=" + prioridad + "]";
        }
    }

    public static void main(String[] args) {
        PriorityQueue<Tarea> cola = new PriorityQueue<>();

        cola.add(new Tarea("Enviar email",        1));
        cola.add(new Tarea("Fix bug crítico",     5));
        cola.add(new Tarea("Code review",         3));
        cola.add(new Tarea("Actualizar deps",     2));

        System.out.println("Procesando tareas (mayor prioridad primero):");
        while (!cola.isEmpty()) {
            System.out.println("  Procesando: " + cola.poll());
        }
    }
}
