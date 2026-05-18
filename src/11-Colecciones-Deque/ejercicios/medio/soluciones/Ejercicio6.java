import java.util.ArrayDeque;
import java.util.Stack;

public class Ejercicio6 {

    static long medirArrayDeque(int n) {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        long inicio = System.nanoTime();
        for (int i = 0; i < n; i++) deque.push(i);
        for (int i = 0; i < n; i++) deque.pop();
        return System.nanoTime() - inicio;
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    static long medirStack(int n) {
        Stack<Integer> stack = new Stack<>();
        long inicio = System.nanoTime();
        for (int i = 0; i < n; i++) stack.push(i);
        for (int i = 0; i < n; i++) stack.pop();
        return System.nanoTime() - inicio;
    }

    public static void main(String[] args) {
        final int N = 100_000;

        // Calentamiento JVM
        medirArrayDeque(N);
        medirStack(N);

        long tiempoDeque = medirArrayDeque(N);
        long tiempoStack = medirStack(N);

        System.out.printf("ArrayDeque — %d ops: %,d ns%n", N * 2, tiempoDeque);
        System.out.printf("Stack      — %d ops: %,d ns%n", N * 2, tiempoStack);
        System.out.printf("Ratio Stack/ArrayDeque: %.2fx%n", (double) tiempoStack / tiempoDeque);
        System.out.println("→ ArrayDeque es más rápido (no sincronizado, diseñado para ser stack/queue).");
    }
}
