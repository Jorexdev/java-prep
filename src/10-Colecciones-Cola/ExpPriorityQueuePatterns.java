import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

public class ExpPriorityQueuePatterns {

    // ── 1. COMPARATOR CUSTOM PARA OBJETOS ───────────────────────────────────
    // PriorityQueue por defecto usa Comparable (orden natural).
    // Para objetos de dominio se pasa un Comparator al constructor.

    record Tarea(int prioridad, String nombre) {}

    static void comparatorCustom() {
        System.out.println("── 1. Comparator custom ────────────────────────────────────");

        // Min-heap por prioridad (menor número = mayor urgencia)
        PriorityQueue<Tarea> cola = new PriorityQueue<>(
                Comparator.comparingInt(Tarea::prioridad)
        );

        cola.offer(new Tarea(3, "Tarea baja"));
        cola.offer(new Tarea(1, "Tarea urgente"));
        cola.offer(new Tarea(2, "Tarea media"));

        System.out.println("Procesando tareas por prioridad:");
        while (!cola.isEmpty()) {
            Tarea t = cola.poll();
            System.out.println("  [" + t.prioridad() + "] " + t.nombre());
        }

        // Comparator compuesto: primero por prioridad, luego alfabético como desempate
        PriorityQueue<Tarea> colaCompuesta = new PriorityQueue<>(
                Comparator.comparingInt(Tarea::prioridad)
                          .thenComparing(Tarea::nombre)
        );
        colaCompuesta.offer(new Tarea(1, "Z-crítica"));
        colaCompuesta.offer(new Tarea(1, "A-crítica"));
        colaCompuesta.offer(new Tarea(2, "media"));

        System.out.println("Con desempate alfabético:");
        while (!colaCompuesta.isEmpty()) {
            Tarea t = colaCompuesta.poll();
            System.out.println("  [" + t.prioridad() + "] " + t.nombre());
        }
    }

    // ── 2. TOP-K ELEMENTS (K mayores con PQ de tamaño fijo) ─────────────────
    // Patrón clásico: mantener los K mayores elementos sin ordenar todo el array.
    // Complejidad: O(n log K) en tiempo, O(K) en espacio.
    // Truco: se usa un MIN-heap de tamaño K. Si el nuevo elemento supera al mínimo
    // actual, se expulsa el mínimo y entra el nuevo. Al final quedan los K mayores.

    static List<Integer> topK(int[] nums, int k) {
        // Min-heap de tamaño K: la cabeza siempre es el menor de los K actuales
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(k);

        for (int num : nums) {
            minHeap.offer(num);
            if (minHeap.size() > k) {
                minHeap.poll(); // expulsa el menor → solo quedan los K mayores
            }
        }

        // Volcar en lista ordenada de mayor a menor para la salida
        List<Integer> result = new ArrayList<>(minHeap);
        result.sort(Comparator.reverseOrder());
        return result;
    }

    static void topKPattern() {
        System.out.println("\n── 2. Top-K elements ───────────────────────────────────────");

        int[] nums = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        System.out.println("Array: " + Arrays.toString(nums));

        List<Integer> top3 = topK(nums, 3);
        System.out.println("Top-3 mayores:         " + top3);

        List<Integer> top5 = topK(nums, 5);
        System.out.println("Top-5 mayores:         " + top5);

        // Con objetos: top-2 tareas de mayor prioridad (mayor número = más importante aquí)
        PriorityQueue<Tarea> minHeapTareas = new PriorityQueue<>(
                Comparator.comparingInt(Tarea::prioridad) // min-heap por prioridad
        );
        int K = 2;
        List<Tarea> tareas = List.of(
                new Tarea(3, "deploy"), new Tarea(8, "incidente-prod"),
                new Tarea(1, "doc"), new Tarea(6, "review"), new Tarea(9, "p0-crash")
        );
        for (Tarea t : tareas) {
            minHeapTareas.offer(t);
            if (minHeapTareas.size() > K) minHeapTareas.poll();
        }
        System.out.println("Top-2 tareas críticas:");
        minHeapTareas.stream()
                     .sorted(Comparator.comparingInt(Tarea::prioridad).reversed())
                     .forEach(t -> System.out.println("  [" + t.prioridad() + "] " + t.nombre()));
    }

    // ── 3. MERGE DE K LISTAS ORDENADAS ──────────────────────────────────────
    // Patrón: insertar la cabeza de cada lista en un min-heap junto con el índice
    // de la lista. Al hacer poll() se obtiene el elemento global mínimo; se avanza
    // el puntero de esa lista y se inserta el siguiente elemento.
    // Complejidad: O(n log K) donde n es el total de elementos y K el número de listas.

    record Entry(int value, int listIdx, int elemIdx) {}

    static List<Integer> mergeKSorted(List<List<Integer>> listas) {
        PriorityQueue<Entry> heap = new PriorityQueue<>(
                Comparator.comparingInt(Entry::value)
        );

        // Insertar el primer elemento de cada lista
        for (int i = 0; i < listas.size(); i++) {
            if (!listas.get(i).isEmpty()) {
                heap.offer(new Entry(listas.get(i).get(0), i, 0));
            }
        }

        List<Integer> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            Entry min = heap.poll();
            result.add(min.value());

            int nextIdx = min.elemIdx() + 1;
            if (nextIdx < listas.get(min.listIdx()).size()) {
                heap.offer(new Entry(
                        listas.get(min.listIdx()).get(nextIdx),
                        min.listIdx(),
                        nextIdx
                ));
            }
        }
        return result;
    }

    static void mergeKListasSortedPattern() {
        System.out.println("\n── 3. Merge de K listas ordenadas ──────────────────────────");

        List<List<Integer>> listas = List.of(
                List.of(1, 4, 7),
                List.of(2, 5, 8),
                List.of(3, 6, 9)
        );

        List<Integer> merged = mergeKSorted(listas);
        System.out.println("Merge de 3 listas:     " + merged);

        // Caso con listas de distinto tamaño
        List<List<Integer>> asimetrico = List.of(
                List.of(1, 10, 100),
                List.of(2),
                List.of(3, 4, 5, 6, 7)
        );
        System.out.println("Merge asimétrico:      " + mergeKSorted(asimetrico));
    }

    // ── 4. SEMÁNTICA peek / poll / remove — EXCEPCIONES ─────────────────────
    // peek()   → devuelve la cabeza sin extraer; null si vacía        (no lanza excepción)
    // poll()   → extrae la cabeza; null si vacía                      (no lanza excepción)
    // element()→ devuelve la cabeza sin extraer; NoSuchElementException si vacía
    // remove() → extrae la cabeza; NoSuchElementException si vacía
    //
    // Regla: métodos del API Queue (peek/poll/offer) devuelven null o false en casos límite.
    //        métodos del API Collection/AbstractQueue (element/remove/add) lanzan excepción.

    static void semanticaPeekPollRemove() {
        System.out.println("\n── 4. Semántica peek/poll/remove ───────────────────────────");

        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.offer(10);
        pq.offer(5);
        pq.offer(20);

        // peek(): ve la cabeza sin modificar la cola
        System.out.println("peek():                " + pq.peek());   // 5 (mínimo)
        System.out.println("size tras peek():      " + pq.size());   // 3 (sin cambios)

        // poll(): extrae la cabeza
        System.out.println("poll():                " + pq.poll());   // 5
        System.out.println("size tras poll():      " + pq.size());   // 2

        // Con cola vacía: peek() y poll() devuelven null (sin excepción)
        PriorityQueue<Integer> vacia = new PriorityQueue<>();
        System.out.println("peek() en vacía:       " + vacia.peek()); // null
        System.out.println("poll() en vacía:       " + vacia.poll()); // null

        // element(): equivalente a peek() pero lanza NoSuchElementException si vacía
        try {
            vacia.element();
        } catch (NoSuchElementException e) {
            System.out.println("element() en vacía:    NoSuchElementException (esperado)");
        }

        // remove(): equivalente a poll() pero lanza NoSuchElementException si vacía
        try {
            vacia.remove();
        } catch (NoSuchElementException e) {
            System.out.println("remove() en vacía:     NoSuchElementException (esperado)");
        }

        // remove(Object): elimina una instancia concreta del elemento (no la cabeza)
        PriorityQueue<Integer> pq2 = new PriorityQueue<>(List.of(1, 2, 3, 4, 5));
        boolean eliminado = pq2.remove(3); // elimina el valor 3, no la cabeza
        System.out.println("remove(3) eliminado:   " + eliminado);
        System.out.println("Cola tras remove(3):   " + pq2); // [1,2,4,5] en orden heap

        System.out.println("\nResumen de garantías:");
        System.out.println("  peek()/poll()/offer() → null/false en límite, sin excepción");
        System.out.println("  element()/remove()/add() → NoSuchElementException/IllegalStateException");
    }

    // ── main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        comparatorCustom();
        topKPattern();
        mergeKListasSortedPattern();
        semanticaPeekPollRemove();
    }
}
