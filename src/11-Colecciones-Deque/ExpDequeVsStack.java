import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

public class ExpDequeVsStack {

    // ── POR QUÉ java.util.Stack ES LEGACY ────────────────────────────────────
    // Stack extiende Vector, que está sincronizado en CADA operación.
    // En código single-thread (el caso habitual), ese lock es puro overhead.
    // Además hereda todos los métodos de Vector (get(i), set(i,e)...)
    // que no tienen sentido en una pila — rompe el principio de encapsulación.
    //
    // La alternativa recomendada desde Java 6: Deque / ArrayDeque.

    // ── BENCHMARK: Stack vs ArrayDeque — push/pop 1M elementos ──────────────
    static void benchmark(int n) {
        System.out.println("  Benchmark con " + n + " operaciones push + pop:");

        // Stack (Vector sincronizado)
        Stack<Integer> stack = new Stack<>();
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) stack.push(i);
        for (int i = 0; i < n; i++) stack.pop();
        long durStack = System.nanoTime() - t0;

        // ArrayDeque (no sincronizado, array circular)
        Deque<Integer> dq = new ArrayDeque<>();
        long t1 = System.nanoTime();
        for (int i = 0; i < n; i++) dq.push(i);
        for (int i = 0; i < n; i++) dq.pop();
        long durDeque = System.nanoTime() - t1;

        // LinkedList como Deque
        Deque<Integer> ll = new LinkedList<>();
        long t2 = System.nanoTime();
        for (int i = 0; i < n; i++) ll.push(i);
        for (int i = 0; i < n; i++) ll.pop();
        long durLL = System.nanoTime() - t2;

        System.out.printf("  Stack (Vector):   %,d ms%n", durStack / 1_000_000);
        System.out.printf("  ArrayDeque:       %,d ms%n", durDeque / 1_000_000);
        System.out.printf("  LinkedList:       %,d ms%n", durLL    / 1_000_000);
        System.out.println("  ArrayDeque gana: sin sincronización + cache-friendly (array contiguo).");
        System.out.println("  LinkedList pierde: un nodo por elemento = mucha presión en el GC.");
    }

    // ── MODO PILA (LIFO): Stack vs Deque ─────────────────────────────────────
    // Operaciones equivalentes:
    //   Stack.push(e)   ↔  Deque.push(e)  / addFirst(e)
    //   Stack.pop()     ↔  Deque.pop()    / removeFirst()
    //   Stack.peek()    ↔  Deque.peek()   / peekFirst()
    static void modoPila() {
        System.out.println("\n  -- Stack legacy --");
        Stack<String> stack = new Stack<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");
        System.out.println("  peek():  " + stack.peek()); // C
        System.out.println("  pop():   " + stack.pop());  // C
        System.out.println("  estado: " + stack);         // [A, B]

        System.out.println("\n  -- ArrayDeque como pila --");
        Deque<String> dq = new ArrayDeque<>();
        dq.push("A");       // equivale a addFirst
        dq.push("B");
        dq.push("C");
        System.out.println("  peekFirst(): " + dq.peekFirst()); // C
        System.out.println("  pop():       " + dq.pop());       // C  (= removeFirst)
        System.out.println("  estado:      " + dq);             // [B, A]
    }

    // ── MODO COLA (FIFO): solo con Deque ─────────────────────────────────────
    // Stack no puede actuar como cola de forma natural.
    // Deque: offer / poll / peek operan sobre el extremo correcto para FIFO.
    //   offer(e)  → añade al final   (= offerLast)
    //   poll()    → retira del frente (= pollFirst)
    //   peek()    → lee el frente     (= peekFirst)
    static void modoCola() {
        Deque<String> dq = new ArrayDeque<>();
        dq.offer("primero");
        dq.offer("segundo");
        dq.offer("tercero");
        System.out.println("  peek():  " + dq.peek());  // primero
        System.out.println("  poll():  " + dq.poll());  // primero
        System.out.println("  estado: " + dq);          // [segundo, tercero]
        System.out.println("  → FIFO: el primero en entrar es el primero en salir.");
    }

    // ── TABLA RESUMEN DE EQUIVALENCIAS ───────────────────────────────────────
    static void tablaEquivalencias() {
        System.out.println();
        System.out.println("  Operación         Stack         Deque (pila)    Deque (cola)");
        System.out.println("  ─────────────────────────────────────────────────────────────");
        System.out.println("  Insertar          push(e)       push(e)         offer(e)");
        System.out.println("                                  addFirst(e)     offerLast(e)");
        System.out.println("  Leer sin borrar   peek()        peekFirst()     peek()");
        System.out.println("                                  peek()          peekFirst()");
        System.out.println("  Extraer           pop()         pop()           poll()");
        System.out.println("                                  removeFirst()   pollFirst()");
        System.out.println("  Orden             LIFO          LIFO            FIFO");
    }

    public static void main(String[] args) {
        System.out.println("=== Stack legacy vs ArrayDeque: benchmark ===");
        benchmark(1_000_000);

        System.out.println("\n=== Modo pila (LIFO) — Stack vs ArrayDeque ===");
        modoPila();

        System.out.println("\n=== Modo cola (FIFO) — solo Deque ===");
        modoCola();

        System.out.println("\n=== Tabla de equivalencias ===");
        tablaEquivalencias();

        System.out.println();
        System.out.println("  REGLA PRÁCTICA:");
        System.out.println("  ✓ Usar ArrayDeque cuando necesitas pila o cola en código single-thread.");
        System.out.println("  ✓ Usar LinkedList solo si necesitas null o List + Deque simultáneo.");
        System.out.println("  ✗ No usar Stack — es legacy, sincronizado innecesariamente.");
        System.out.println("  ✗ No usar Vector ni HashTable — misma razón: sincronización inútil.");
    }
}
