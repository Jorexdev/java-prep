import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio4 {

    enum Prioridad { HIGH, MEDIUM, LOW }

    static class MultiLevelQueue {
        private final Deque<String> high   = new ArrayDeque<>();
        private final Deque<String> medium = new ArrayDeque<>();
        private final Deque<String> low    = new ArrayDeque<>();

        public void offer(String msg, Prioridad prioridad) {
            switch (prioridad) {
                case HIGH   -> high.offerLast(msg);
                case MEDIUM -> medium.offerLast(msg);
                case LOW    -> low.offerLast(msg);
            }
        }

        public String poll() {
            if (!high.isEmpty())   return high.pollFirst();
            if (!medium.isEmpty()) return medium.pollFirst();
            if (!low.isEmpty())    return low.pollFirst();
            return null;
        }

        public boolean isEmpty() {
            return high.isEmpty() && medium.isEmpty() && low.isEmpty();
        }

        public int size() {
            return high.size() + medium.size() + low.size();
        }
    }

    public static void main(String[] args) {
        MultiLevelQueue mlq = new MultiLevelQueue();

        mlq.offer("Log de debug",       Prioridad.LOW);
        mlq.offer("Alerta de sistema",  Prioridad.HIGH);
        mlq.offer("Reporte mensual",    Prioridad.MEDIUM);
        mlq.offer("INCIDENTE CRÍTICO",  Prioridad.HIGH);
        mlq.offer("Backup completado",  Prioridad.LOW);
        mlq.offer("Notificación email", Prioridad.MEDIUM);

        System.out.println("Cola multinivel (HIGH > MEDIUM > LOW):");
        while (!mlq.isEmpty()) {
            System.out.println("  poll() → " + mlq.poll());
        }
    }
}
