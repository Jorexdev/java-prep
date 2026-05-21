import java.util.*;

public class Ejercicio5 {

    @FunctionalInterface
    interface TestFunction<I, O> {
        O run(I input);
    }

    static class ParametrizedTestRunner<I, O> {
        private final String nombre;
        private final TestFunction<I, O> fn;

        ParametrizedTestRunner(String nombre, TestFunction<I, O> fn) {
            this.nombre = nombre; this.fn = fn;
        }

        void run(List<Object[]> casos) {
            int pass = 0, fail = 0;
            for (Object[] caso : casos) {
                @SuppressWarnings("unchecked") I input    = (I) caso[0];
                @SuppressWarnings("unchecked") O expected = (O) caso[1];
                try {
                    O actual = fn.run(input);
                    if (Objects.equals(expected, actual)) {
                        System.out.printf("PASS [%s] %s → %s%n", nombre, input, actual);
                        pass++;
                    } else {
                        System.out.printf("FAIL [%s] %s → esperado=%s actual=%s%n", nombre, input, expected, actual);
                        fail++;
                    }
                } catch (Exception e) {
                    System.out.printf("ERROR [%s] %s → %s%n", nombre, input, e.getMessage());
                    fail++;
                }
            }
            System.out.printf("→ %d/%d PASS%n%n", pass, pass + fail);
        }
    }

    static boolean esPrimo(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; (long) i * i <= n; i += 2) if (n % i == 0) return false;
        return true;
    }

    static int factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("negativo");
        int r = 1; for (int i = 2; i <= n; i++) r *= i; return r;
    }

    public static void main(String[] args) {
        System.out.println("=== esPrimo ===");
        new ParametrizedTestRunner<>("esPrimo", Ejercicio5::esPrimo).run(List.of(
            new Object[]{1,   false},
            new Object[]{2,   true},
            new Object[]{3,   true},
            new Object[]{4,   false},
            new Object[]{13,  true},
            new Object[]{15,  false},
            new Object[]{17,  true},
            new Object[]{97,  true},
            new Object[]{100, false},
            new Object[]{101, true}
        ));

        System.out.println("=== factorial ===");
        new ParametrizedTestRunner<>("factorial", Ejercicio5::factorial).run(List.of(
            new Object[]{0, 1},
            new Object[]{1, 1},
            new Object[]{5, 120},
            new Object[]{6, 720},
            new Object[]{10, 3628800}
        ));
    }
}
