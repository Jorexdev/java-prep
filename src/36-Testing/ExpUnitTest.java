import java.util.ArrayList;
import java.util.List;

/**
 * Simulación de un framework de testing unitario minimalista (sin JUnit).
 *
 * Demuestra:
 * - Clase bajo prueba: ServicioCalculadora (sumar, dividir, esPrimo)
 * - Mini-framework: MiniTest con assertEquals, assertThrows, assertTrue
 * - 8 tests con salida PASS/FAIL
 *
 * Ejecutar: java -cp target/classes ExpUnitTest
 */
public class ExpUnitTest {

    // ── Clase bajo prueba ────────────────────────────────────────────────────

    static class ServicioCalculadora {

        public int sumar(int a, int b) {
            return a + b;
        }

        public int dividir(int dividendo, int divisor) {
            if (divisor == 0) {
                throw new ArithmeticException("División por cero no permitida");
            }
            return dividendo / divisor;
        }

        public boolean esPrimo(int n) {
            if (n < 2) return false;
            if (n == 2) return true;
            if (n % 2 == 0) return false;
            for (int i = 3; i * i <= n; i += 2) {
                if (n % i == 0) return false;
            }
            return true;
        }
    }

    // ── Mini-framework de testing ────────────────────────────────────────────

    static class MiniTest {

        private final List<String> resultados = new ArrayList<>();
        private int pasados = 0;
        private int fallados = 0;

        public void assertEquals(String nombre, Object esperado, Object actual) {
            if (esperado.equals(actual)) {
                registrar(nombre, true, null);
            } else {
                registrar(nombre, false,
                    "esperado: " + esperado + " | actual: " + actual);
            }
        }

        public void assertTrue(String nombre, boolean condicion) {
            if (condicion) {
                registrar(nombre, true, null);
            } else {
                registrar(nombre, false, "la condición era false");
            }
        }

        public void assertThrows(String nombre,
                                  Class<? extends Exception> tipoEsperado,
                                  Runnable codigo) {
            try {
                codigo.run();
                registrar(nombre, false,
                    "se esperaba " + tipoEsperado.getSimpleName() + " pero no se lanzó ninguna excepción");
            } catch (Exception e) {
                if (tipoEsperado.isInstance(e)) {
                    registrar(nombre, true, null);
                } else {
                    registrar(nombre, false,
                        "se esperaba " + tipoEsperado.getSimpleName()
                        + " pero se lanzó " + e.getClass().getSimpleName());
                }
            }
        }

        private void registrar(String nombre, boolean paso, String mensaje) {
            if (paso) {
                pasados++;
                resultados.add("  PASS  " + nombre);
            } else {
                fallados++;
                resultados.add("  FAIL  " + nombre + " → " + mensaje);
            }
        }

        public void imprimirResumen() {
            System.out.println();
            resultados.forEach(System.out::println);
            System.out.println();
            System.out.println("─────────────────────────────────────────");
            System.out.printf("  Total: %d  |  PASS: %d  |  FAIL: %d%n",
                pasados + fallados, pasados, fallados);
            System.out.println("─────────────────────────────────────────");
        }
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        ServicioCalculadora calc = new ServicioCalculadora();
        MiniTest test = new MiniTest();

        System.out.println("═══════════════════════════════════════════");
        System.out.println("  ExpUnitTest — Mini Testing Framework");
        System.out.println("═══════════════════════════════════════════");

        // Test 1 — sumar dos positivos
        test.assertEquals(
            "sumar(2, 3) debe retornar 5",
            5,
            calc.sumar(2, 3)
        );

        // Test 2 — sumar con negativo
        test.assertEquals(
            "sumar(-1, 1) debe retornar 0",
            0,
            calc.sumar(-1, 1)
        );

        // Test 3 — dividir exacto
        test.assertEquals(
            "dividir(10, 2) debe retornar 5",
            5,
            calc.dividir(10, 2)
        );

        // Test 4 — dividir con resultado entero truncado
        test.assertEquals(
            "dividir(7, 2) debe retornar 3 (entero truncado)",
            3,
            calc.dividir(7, 2)
        );

        // Test 5 — división por cero lanza ArithmeticException
        test.assertThrows(
            "dividir(5, 0) debe lanzar ArithmeticException",
            ArithmeticException.class,
            () -> calc.dividir(5, 0)
        );

        // Test 6 — número primo
        test.assertTrue(
            "17 debe ser primo",
            calc.esPrimo(17)
        );

        // Test 7 — número no primo
        test.assertTrue(
            "18 no debe ser primo",
            !calc.esPrimo(18)
        );

        // Test 8 — casos borde: 0 y 1 no son primos
        test.assertTrue(
            "0 y 1 no son primos",
            !calc.esPrimo(0) && !calc.esPrimo(1)
        );

        test.imprimirResumen();
    }
}
