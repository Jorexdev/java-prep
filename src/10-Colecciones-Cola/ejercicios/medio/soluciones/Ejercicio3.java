import java.util.ArrayDeque;
import java.util.Queue;

public class Ejercicio3 {

    static class Proceso {
        final String nombre;
        int tiempoRestante;

        Proceso(String nombre, int tiempoRestante) {
            this.nombre = nombre;
            this.tiempoRestante = tiempoRestante;
        }

        @Override
        public String toString() {
            return nombre + "(restante=" + tiempoRestante + ")";
        }
    }

    public static void main(String[] args) {
        final int QUANTUM = 3;
        Queue<Proceso> cola = new ArrayDeque<>();

        cola.offer(new Proceso("P1", 7));
        cola.offer(new Proceso("P2", 4));
        cola.offer(new Proceso("P3", 9));
        cola.offer(new Proceso("P4", 2));

        int tiempo = 0;
        System.out.println("Round-robin con quantum=" + QUANTUM);
        System.out.println("---");

        while (!cola.isEmpty()) {
            Proceso p = cola.poll();
            int ejecutado = Math.min(QUANTUM, p.tiempoRestante);
            p.tiempoRestante -= ejecutado;
            tiempo += ejecutado;

            System.out.printf("t=%2d  Ejecutado %s durante %d ciclos", tiempo - ejecutado, p.nombre, ejecutado);
            if (p.tiempoRestante > 0) {
                System.out.println(" → vuelve a la cola: " + p);
                cola.offer(p);
            } else {
                System.out.println(" → COMPLETADO en t=" + tiempo);
            }
        }
    }
}
