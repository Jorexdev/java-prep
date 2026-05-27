import java.util.ArrayList;
import java.util.List;

public class ExpTypeErasure {

    // ── 1. MISMO getClass() EN TIEMPO DE EJECUCIÓN ───────────────────────────
    // El compilador elimina la información de tipo genérico al compilar.
    // En bytecode List<String> y List<Integer> son simplemente List (raw type).
    static void mismaTipoEnRuntime() {
        List<String> strings = new ArrayList<>();
        List<Integer> ints   = new ArrayList<>();

        System.out.println("List<String>.getClass(): " + strings.getClass()); // class java.util.ArrayList
        System.out.println("List<Integer>.getClass(): " + ints.getClass());   // class java.util.ArrayList
        System.out.println("Son la misma clase: " + (strings.getClass() == ints.getClass())); // true
    }

    // ── 2. HEAP POLLUTION: cast a raw type → ClassCastException en runtime ───
    // El compilador avisa con "unchecked cast" pero no impide la compilación.
    // El error se produce cuando intentamos USAR el valor con el tipo incorrecto.
    @SuppressWarnings("unchecked") // oculta la advertencia — PELIGROSO si no controlamos el origen
    static void heapPollution() {
        List<Integer> ints = new ArrayList<>();
        ints.add(1);
        ints.add(2);

        // Perdemos el tipo: raw type
        List raw = ints;

        // Añadimos un String a través del raw type — compila, pero contamina el heap
        raw.add("texto-intruso");

        // La lista ahora tiene [1, 2, "texto-intruso"] mezclados
        // El ClassCastException salta cuando leemos con el tipo fuerte
        try {
            for (Integer n : ints) { // aquí el compilador inserta un (Integer) cast implícito
                System.out.println(n);
            }
        } catch (ClassCastException e) {
            System.out.println("ClassCastException: " + e.getMessage());
            System.out.println("→ Heap pollution: mezclamos tipos a través de raw type");
        }
    }

    // ── 3. instanceof CON GENÉRICOS — POR QUÉ NO COMPILA ────────────────────
    // `instanceof List<String>` NO compila porque en runtime no existe
    // List<String>, solo List.  Solo puedes hacer `instanceof List<?>` o `instanceof List`.
    static void instanceofConGenericos(Object obj) {
        // ❌ if (obj instanceof List<String>) { }  → ERROR de compilación
        // ✅ Puedes usar el tipo raw o wildcard sin acotación:
        if (obj instanceof List<?> lista) {
            System.out.println("Es una List con " + lista.size() + " elementos");
        }
    }

    // ── 4. TIPOS REIFICABLES vs NO REIFICABLES ───────────────────────────────
    // Reificable: su información de tipo existe COMPLETA en runtime.
    //   → int, String, int[], List (raw), List<?>
    // No reificable: su información de tipo se borra en runtime.
    //   → List<String>, Map<K,V>, T (parámetro de tipo), E[], T[]
    //
    // Consecuencia práctica: no puedes crear arrays de tipos genéricos.
    //   new T[10]         → ERROR de compilación
    //   new List<String>  → OK (objeto), pero new List<String>[10] → ERROR
    static void reificables() {
        // Reificables — instanceof funciona
        Object arr = new int[]{1, 2};
        System.out.println("int[] es reificable: " + (arr instanceof int[]));

        Object lista = new ArrayList<String>();
        System.out.println("List (raw) es reificable: " + (lista instanceof List));

        // No reificable en array — el compilador lo bloquea:
        // List<String>[] noCompila = new List<String>[5]; // ERROR
        System.out.println("No se pueden crear arrays de tipos parametrizados");
    }

    // ── 5. BRIDGE METHODS ────────────────────────────────────────────────────
    // Cuando subclases sobreescriben métodos genéricos, el compilador genera
    // un "bridge method" con la firma borrada para mantener el polimorfismo.
    // Ejemplo: si Comparable<T> tiene compareTo(T o), en bytecode existe
    // también compareTo(Object o) generado por el compilador.
    static class Wrapper<T extends Comparable<T>> implements Comparable<Wrapper<T>> {
        final T valor;
        Wrapper(T v) { this.valor = v; }

        // Este método tiene firma genérica.
        // El compilador genera también: int compareTo(Object o) { return compareTo((Wrapper)o); }
        // Ese es el "bridge method" — transparente para nosotros, visible con javap -c.
        @Override
        public int compareTo(Wrapper<T> otro) {
            return this.valor.compareTo(otro.valor);
        }
        @Override public String toString() { return "Wrapper(" + valor + ")"; }
    }

    static void bridgeMethods() {
        Wrapper<Integer> a = new Wrapper<>(10);
        Wrapper<Integer> b = new Wrapper<>(20);
        // Usamos el método compilado (el bridge method actúa detrás en el polimorfismo)
        System.out.println("compareTo: " + a.compareTo(b)); // -1
        System.out.println("El compilador generó un bridge method con firma (Object) para el polimorfismo");
    }

    // ── 6. @SuppressWarnings("unchecked") — CUÁNDO ES SEGURO ────────────────
    // SEGURO: cuando TÚ controlas el origen del cast y sabes con certeza el tipo.
    // PELIGROSO: cuando el origen es externo o desconocido.
    @SuppressWarnings("unchecked")
    static <T> T castSeguro(Object obj) {
        // Seguro si el llamante garantiza que obj es T.
        // La anotación suprime el aviso del compilador — la responsabilidad es nuestra.
        return (T) obj;
    }

    static void suppressWarnings() {
        // Seguro: sabemos que el objeto es Integer
        Integer n = castSeguro(42);
        System.out.println("castSeguro(42) → " + n);

        // Peligroso: aquí el compilador no avisa pero falla en runtime
        try {
            String s = castSeguro(42); // Integer no es String
            System.out.println(s.length()); // ClassCastException aquí
        } catch (ClassCastException e) {
            System.out.println("@SuppressWarnings peligroso: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Mismo getClass() en runtime ===");
        mismaTipoEnRuntime();

        System.out.println("\n=== 2. Heap pollution y ClassCastException ===");
        heapPollution();

        System.out.println("\n=== 3. instanceof con genéricos ===");
        instanceofConGenericos(new ArrayList<>(List.of("a", "b")));

        System.out.println("\n=== 4. Tipos reificables vs no reificables ===");
        reificables();

        System.out.println("\n=== 5. Bridge methods ===");
        bridgeMethods();

        System.out.println("\n=== 6. @SuppressWarnings(\"unchecked\") ===");
        suppressWarnings();
    }
}
