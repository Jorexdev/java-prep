import java.util.ArrayDeque;

public class Ejercicio2 {

    static class CircularBuffer {
        private final ArrayDeque<String> buffer;
        private final int capacidad;

        CircularBuffer(int capacidad) {
            this.capacidad = capacidad;
            this.buffer = new ArrayDeque<>(capacidad);
        }

        public void offer(String elemento) {
            if (isFull()) {
                throw new IllegalStateException("Buffer lleno (cap=" + capacidad + ")");
            }
            buffer.offerLast(elemento);
        }

        public String poll() {
            if (buffer.isEmpty()) return null;
            return buffer.pollFirst();
        }

        public boolean isFull()  { return buffer.size() == capacidad; }
        public boolean isEmpty() { return buffer.isEmpty(); }
        public int size()        { return buffer.size(); }

        @Override
        public String toString() { return buffer.toString(); }
    }

    public static void main(String[] args) {
        CircularBuffer buf = new CircularBuffer(3);

        buf.offer("msg-A");
        buf.offer("msg-B");
        buf.offer("msg-C");
        System.out.println("Buffer lleno: " + buf.isFull() + " → " + buf);

        try {
            buf.offer("msg-D"); // debería lanzar excepción
        } catch (IllegalStateException e) {
            System.out.println("Excepción esperada: " + e.getMessage());
        }

        System.out.println("\nExtrayendo: " + buf.poll());
        System.out.println("Ahora isFull: " + buf.isFull());
        buf.offer("msg-D"); // ahora sí cabe
        System.out.println("Tras añadir msg-D: " + buf);
    }
}
