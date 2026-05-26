import java.util.ArrayList;
import java.util.List;

// Simula @ParameterizedTest de JUnit 5 con @MethodSource y @CsvSource.
// TestRunner ejecuta el mismo método de test con múltiples conjuntos de argumentos
// y muestra PASSED/FAILED por cada combinación, con el motivo si falla.

// ── Resultado de un caso de prueba ────────────────────────────────────────────

class TestCase {
    final String   label;
    final boolean  passed;
    final String   failReason;

    TestCase(String label, boolean passed, String failReason) {
        this.label      = label;
        this.passed     = passed;
        this.failReason = failReason;
    }
}

// ── TestRunner ────────────────────────────────────────────────────────────────

// Equivale al motor de JUnit 5 que itera los argumentos de @ParameterizedTest
class TestRunner {

    // Ejecuta una función boolean(T) contra una lista de (input, expectedResult)
    // y devuelve los resultados individuales.
    // @ParameterizedTest @MethodSource / @CsvSource equivalente
    <T> List<TestCase> run(String testName, List<Object[]> cases,
                           java.util.function.Function<T, Boolean> testedFn) {
        System.out.println("  Ejecutando: " + testName);
        List<TestCase> results = new ArrayList<>();

        for (Object[] row : cases) {
            @SuppressWarnings("unchecked") T input = (T) row[0];
            boolean expected = (Boolean) row[1];

            boolean actual;
            String  failReason = null;
            try {
                actual = testedFn.apply(input);
                if (actual != expected) {
                    failReason = "expected " + expected + " but was " + actual;
                }
            } catch (Exception ex) {
                actual     = false;
                failReason = "excepción: " + ex.getMessage();
            }

            boolean passed = (failReason == null);
            String label   = testName + "[n=" + input + "]";
            results.add(new TestCase(label, passed, failReason));

            if (passed) {
                System.out.println("    PASSED " + label);
            } else {
                System.out.println("    FAILED " + label + " — " + failReason);
            }
        }

        return results;
    }

    void printSummary(List<TestCase> results) {
        long passed = results.stream().filter(t -> t.passed).count();
        System.out.println();
        System.out.printf("  Resumen: %d/%d PASSED%n", passed, results.size());
        if (passed < results.size()) {
            System.out.println("  Casos fallidos:");
            results.stream()
                .filter(t -> !t.passed)
                .forEach(t -> System.out.println("    ✗ " + t.label + " → " + t.failReason));
        } else {
            System.out.println("  Todos los casos pasaron.");
        }
    }
}

// ── Función bajo prueba ───────────────────────────────────────────────────────

class PrimeMath {
    // La función que vamos a parametrizar: ¿es n primo?
    static boolean isPrime(int n) {
        if (n < 2)  return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpParameterized {
    public static void main(String[] args) {

        TestRunner runner = new TestRunner();

        System.out.println("=== Simulación @ParameterizedTest ===\n");

        // ─── @CsvSource equivalente: pares (n, expectedIsPrime) ──────────────
        System.out.println("[ @CsvSource — isPrime(n) con 10 inputs ]\n");
        System.out.println("  Equivalente JUnit 5:");
        System.out.println("  @ParameterizedTest");
        System.out.println("  @CsvSource({\"-1,false\",\"0,false\",\"1,false\",\"2,true\",...})");
        System.out.println("  void testIsPrime(int n, boolean expected) {");
        System.out.println("      assertThat(PrimeMath.isPrime(n)).isEqualTo(expected);");
        System.out.println("  }\n");

        // (input, expectedResult)
        List<Object[]> csvCases = List.of(
            new Object[]{-1,  false},   // negativo → no primo
            new Object[]{ 0,  false},   // cero → no primo
            new Object[]{ 1,  false},   // 1 no es primo por definición
            new Object[]{ 2,  true},    // primo más pequeño
            new Object[]{ 4,  false},   // par compuesto
            new Object[]{ 7,  true},    // primo
            new Object[]{11,  true},    // primo
            new Object[]{12,  false},   // compuesto
            new Object[]{97,  true},    // primo grande
            new Object[]{100, false}    // compuesto
        );

        List<TestCase> resultsCsv = runner.run("isPrime", csvCases, PrimeMath::isPrime);
        runner.printSummary(resultsCsv);

        System.out.println("\n" + "─".repeat(60) + "\n");

        // ─── @MethodSource equivalente: casos generados por un método ────────
        System.out.println("[ @MethodSource — casos de fallo deliberado para ver el output ]\n");
        System.out.println("  Equivalente JUnit 5:");
        System.out.println("  @ParameterizedTest @MethodSource(\"primeProvider\")");
        System.out.println("  static Stream<Arguments> primeProvider() { ... }\n");

        // Introducimos dos casos con expected INCORRECTO para mostrar FAILED
        List<Object[]> methodSourceCases = List.of(
            new Object[]{ 3,  true},    // correcto
            new Object[]{ 4,  true},    // FALLA: 4 no es primo pero expected=true
            new Object[]{ 5,  true},    // correcto
            new Object[]{ 6,  false},   // correcto
            new Object[]{ 7,  false}    // FALLA: 7 es primo pero expected=false
        );

        List<TestCase> resultsMethod = runner.run("isPrime_conFallos", methodSourceCases, PrimeMath::isPrime);
        runner.printSummary(resultsMethod);

        System.out.println("\n" + "─".repeat(60));
        System.out.println("\n[ Notas ]");
        System.out.println("  @CsvSource   → valores inline en la anotación (pequeños datasets).");
        System.out.println("  @MethodSource → método estático que devuelve Stream<Arguments> (datos complejos).");
        System.out.println("  @ValueSource  → un solo parámetro: int, String, etc.");
        System.out.println("  @EnumSource   → itera los valores de un enum.");
    }
}
