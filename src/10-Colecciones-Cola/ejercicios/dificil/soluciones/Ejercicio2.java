import java.util.ArrayDeque;

public class Ejercicio2 {

    static class CircularBuffer {
        private final ArrayDeque<String> buffer;
        private final int capacidad;

        CircularBuffer(int capacidad) {
            this.capacidad = capacidad;
            this.buffer = new ArrayDeque<>(capacidad);
        }

        public void offer(String mensaje) {
            if (buffer.size() >= capacidad) {
                String descartado = buffer.pollFirst(); // elimina el más antiguo
                System.out.println("  Buffer lleno, descartando: " + descartado);
            }
            buffer.offerLast(mensaje);
        }

        public String poll() {
            return buffer.pollFirst();
        }

        public int size()    { return buffer.size(); }
        public boolean isFull() { return buffer.size() == capacidad; }

        @Override
        public String toString() { return buffer.toString(); }
    }

    public static void main(String[] args) {
        CircularBuffer buf = new CircularBuffer(3);

        System.out.println("Buffer circular (cap=3):");
        buf.offer("msg-1");
        buf.offer("msg-2");
        buf.offer("msg-3");
        System.out.println("Lleno: " + buf.isFull() + " → " + buf);

        buf.offer("msg-4"); // descarta msg-1
        buf.offer("msg-5"); // descarta msg-2
        System.out.println("Tras 2 inserciones extra → " + buf);

        System.out.println("\nExtrayendo:");
        while (buf.size() > 0) {
            System.out.println("  " + buf.poll());
        }
    }
}
