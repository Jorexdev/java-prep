public class Ejercicio2 {

    static class TestRunner {
        private int passed = 0;
        private int failed = 0;

        void setUp() {
            System.out.println("  → setUp");
        }

        void tearDown() {
            System.out.println("  → tearDown");
        }

        void run(String nombre, Runnable test) {
            System.out.println("[TEST] " + nombre);
            setUp();
            try {
                test.run();
                System.out.println("  PASS: " + nombre);
                passed++;
            } catch (AssertionError | Exception e) {
                System.out.println("  FAIL: " + nombre + " — " + e.getMessage());
                failed++;
            } finally {
                tearDown();
            }
            System.out.println();
        }

        void resumen() {
            System.out.println("=== Resultado: " + passed + " pasados, " + failed + " fallidos ===");
        }
    }

    static class Pila {
        private final int[] datos = new int[10];
        private int tope = -1;

        void push(int v) { datos[++tope] = v; }
        int pop()        { return datos[tope--]; }
        boolean estaVacia() { return tope == -1; }
        int tamaño()      { return tope + 1; }
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();

        runner.run("push aumenta el tamaño", () -> {
            Pila p = new Pila();
            p.push(1);
            if (p.tamaño() != 1) throw new AssertionError("esperado 1 pero fue " + p.tamaño());
        });

        runner.run("pop devuelve el último elemento", () -> {
            Pila p = new Pila();
            p.push(42);
            int val = p.pop();
            if (val != 42) throw new AssertionError("esperado 42 pero fue " + val);
        });

        runner.run("pila nueva está vacía", () -> {
            Pila p = new Pila();
            if (!p.estaVacia()) throw new AssertionError("la pila debería estar vacía");
        });

        runner.run("pop en pila vacía lanza excepción — este test falla a propósito", () -> {
            Pila p = new Pila();
            int val = p.pop();
            throw new AssertionError("no debería llegar aquí, val=" + val);
        });

        runner.resumen();
    }
}
