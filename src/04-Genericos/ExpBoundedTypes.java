import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExpBoundedTypes {

    // ── 1. SIN BOUND: <T> solo puede llamar métodos de Object ────────────────
    static <T> void sinBound(T valor) {
        // Solo métodos de Object disponibles
        System.out.println("toString(): " + valor.toString());
        System.out.println("hashCode(): " + valor.hashCode());
        // valor.doubleValue() → ERROR: T no tiene ese método
    }

    // ── 2. UPPER BOUND: <T extends Number> accede a métodos de Number ────────
    static <T extends Number> double sumarLista(List<T> lista) {
        double acc = 0;
        for (T n : lista) acc += n.doubleValue(); // doubleValue() disponible por el bound
        return acc;
    }

    // ── 3. <T extends Comparable<T>>: ordenación genérica ───────────────────
    // T debe ser comparable consigo mismo — patrón habitual en algoritmos de ordenación
    static <T extends Comparable<T>> T max(List<T> lista) {
        if (lista.isEmpty()) throw new IllegalArgumentException("Lista vacía");
        T maximo = lista.get(0);
        for (T elem : lista) {
            if (elem.compareTo(maximo) > 0) maximo = elem;
        }
        return maximo;
    }

    static <T extends Comparable<T>> void bubbleSort(List<T> lista) {
        int n = lista.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (lista.get(j).compareTo(lista.get(j + 1)) > 0) {
                    T tmp = lista.get(j);
                    lista.set(j, lista.get(j + 1));
                    lista.set(j + 1, tmp);
                }
            }
        }
    }

    // ── 4. MULTIPLE BOUNDS: <T extends Serializable & Comparable<T>> ─────────
    // La clase (si hay una) va primero; las interfaces después separadas por &.
    // El tipo T debe cumplir TODOS los bounds simultáneamente.
    static <T extends Serializable & Comparable<T>> T minSerializable(List<T> lista) {
        T min = lista.get(0);
        for (T e : lista) {
            if (e.compareTo(min) < 0) min = e;
        }
        // Aquí podríamos serializar 'min' porque T extends Serializable
        return min;
    }

    // ── 5. RECURSIVE BOUND: <T extends Enum<T>> ──────────────────────────────
    // Los enums usan un self-referential bound. Nos permite llamar métodos
    // propios de Enum: name(), ordinal(), compareTo().
    static <T extends Enum<T>> void mostrarEnum(T[] valores) {
        for (T v : valores) {
            System.out.printf("  ordinal=%d  name=%-10s%n", v.ordinal(), v.name());
        }
    }

    enum Prioridad { BAJA, MEDIA, ALTA, CRITICA }

    // ── 6. Statistics<T extends Number>: sum, avg, min, max ─────────────────
    static class Statistics<T extends Number & Comparable<T>> {
        private final List<T> datos;

        Statistics(List<T> datos) {
            if (datos == null || datos.isEmpty()) throw new IllegalArgumentException("Sin datos");
            this.datos = datos;
        }

        double sum() {
            double acc = 0;
            for (T t : datos) acc += t.doubleValue();
            return acc;
        }

        double avg() { return sum() / datos.size(); }

        T min() {
            T m = datos.get(0);
            for (T t : datos) if (t.compareTo(m) < 0) m = t;
            return m;
        }

        T max() {
            T m = datos.get(0);
            for (T t : datos) if (t.compareTo(m) > 0) m = t;
            return m;
        }

        @Override
        public String toString() {
            return String.format("Statistics{sum=%.2f, avg=%.2f, min=%s, max=%s}",
                    sum(), avg(), min(), max());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Sin bound — solo métodos de Object ===");
        sinBound("Hola generics");
        sinBound(42);

        System.out.println("\n=== 2. <T extends Number> — sumar listas numéricas ===");
        List<Integer> enteros  = List.of(1, 2, 3, 4, 5);
        List<Double>  decimales = List.of(1.1, 2.2, 3.3);
        System.out.println("sumarLista(enteros)   = " + sumarLista(enteros));   // 15.0
        System.out.println("sumarLista(decimales) = " + sumarLista(decimales)); // 6.6

        System.out.println("\n=== 3. <T extends Comparable<T>> — max y bubbleSort ===");
        List<Integer> nums = new ArrayList<>(List.of(5, 2, 8, 1, 9, 3));
        System.out.println("max(nums) = " + max(nums)); // 9
        bubbleSort(nums);
        System.out.println("bubbleSort: " + nums); // [1, 2, 3, 5, 8, 9]

        List<String> palabras = new ArrayList<>(List.of("banana", "apple", "cherry"));
        System.out.println("max(palabras) = " + max(palabras)); // cherry
        bubbleSort(palabras);
        System.out.println("bubbleSort: " + palabras); // [apple, banana, cherry]

        System.out.println("\n=== 4. Multiple bounds: Serializable & Comparable<T> ===");
        // String implementa Serializable y Comparable<String>
        List<String> ss = List.of("delta", "alpha", "gamma");
        System.out.println("minSerializable: " + minSerializable(ss)); // alpha

        System.out.println("\n=== 5. Recursive bound: <T extends Enum<T>> ===");
        mostrarEnum(Prioridad.values());

        System.out.println("\n=== 6. Statistics<T extends Number & Comparable<T>> ===");
        Statistics<Integer> statsInt = new Statistics<>(List.of(10, 5, 8, 3, 15, 7));
        System.out.println("Integers: " + statsInt);

        Statistics<Double> statsDbl = new Statistics<>(List.of(2.5, 1.1, 4.7, 3.3));
        System.out.println("Doubles:  " + statsDbl);
    }
}
