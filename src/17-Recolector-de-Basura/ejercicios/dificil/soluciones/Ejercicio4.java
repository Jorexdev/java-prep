import java.nio.ByteBuffer;

public class Ejercicio4 {

    static class OffHeapBuffer {
        private final ByteBuffer buffer;
        private final int capacity;

        OffHeapBuffer(int capacity) {
            this.capacity = capacity;
            this.buffer = ByteBuffer.allocateDirect(capacity * Integer.BYTES);
        }

        void write(int index, int value) {
            buffer.putInt(index * Integer.BYTES, value);
        }

        int read(int index) {
            return buffer.getInt(index * Integer.BYTES);
        }

        int capacity() { return capacity; }
    }

    static long benchmarkOffHeap(int count) {
        OffHeapBuffer buf = new OffHeapBuffer(count);
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) buf.write(i, i * 2);
        for (int i = 0; i < count; i++) buf.read(i);
        return System.nanoTime() - start;
    }

    static long benchmarkOnHeap(int count) {
        int[] arr = new int[count];
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) arr[i] = i * 2;
        for (int i = 0; i < count; i++) { int v = arr[i]; }
        return System.nanoTime() - start;
    }

    public static void main(String[] args) {
        int COUNT = 1_000;
        int WARMUP = 5;

        // warmup
        for (int i = 0; i < WARMUP; i++) {
            benchmarkOffHeap(COUNT);
            benchmarkOnHeap(COUNT);
        }

        long offHeapNs = benchmarkOffHeap(COUNT);
        long onHeapNs  = benchmarkOnHeap(COUNT);

        System.out.println("=== Off-heap vs On-heap: " + COUNT + " integers ===");
        System.out.printf("  ByteBuffer.allocateDirect: %,6d ns%n", offHeapNs);
        System.out.printf("  int[] on-heap:             %,6d ns%n", onHeapNs);

        System.out.println("\n=== Verificación de correctness ===");
        OffHeapBuffer buf = new OffHeapBuffer(5);
        for (int i = 0; i < 5; i++) buf.write(i, i * 10);
        for (int i = 0; i < 5; i++) System.out.printf("  buf[%d] = %d%n", i, buf.read(i));

        System.out.println("\n=== Cuándo usar buffers directos ===");
        System.out.println("  + I/O de red/disco: no hay copia entre heap y native memory");
        System.out.println("  + Sin presión de GC: el buffer vive fuera del heap gestionado");
        System.out.println("  + NIO channels (FileChannel, SocketChannel) son más eficientes con direct buffers");
        System.out.println("  - Acceso más lento que arrays en operaciones pequeñas (overhead de JNI)");
        System.out.println("  - Se libera con Cleaner/PhantomReference, no con el GC estándar");
        System.out.println("  - Controlar con -XX:MaxDirectMemorySize");
    }
}
