import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

// Simula AssertJ fluent assertions vs JUnit assertEquals.
// FluentAssert envuelve un valor y ofrece encadenamiento de aserciones.
// Compara la legibilidad y el mensaje de error de ambos estilos.

// ── Resultado de una aserción ─────────────────────────────────────────────────

class AssertionResult {
    final String   description;
    final boolean  passed;
    final String   failMessage;

    AssertionResult(String description, boolean passed, String failMessage) {
        this.description = description;
        this.passed      = passed;
        this.failMessage = failMessage;
    }
}

// ── JUnit-style assertEquals (simulado) ──────────────────────────────────────

class JUnitAssert {

    // Equivale a assertEquals(expected, actual, message)
    static AssertionResult assertEquals(Object expected, Object actual, String testName) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        String fail = ok ? null
            : "AssertionError: expected:<" + expected + "> but was:<" + actual + ">";
        return new AssertionResult(testName, ok, fail);
    }

    static AssertionResult assertTrue(boolean condition, String testName) {
        return new AssertionResult(testName, condition,
            condition ? null : "AssertionError: expected true but was false");
    }

    static AssertionResult assertNull(Object obj, String testName) {
        boolean ok = obj == null;
        return new AssertionResult(testName, ok,
            ok ? null : "AssertionError: expected null but was: <" + obj + ">");
    }
}

// ── AssertJ fluent API (simulada) ─────────────────────────────────────────────

class FluentAssert<T> {

    private final T actual;
    private final String describedAs;
    private boolean failed = false;
    private String  failReason = null;

    private FluentAssert(T actual, String describedAs) {
        this.actual      = actual;
        this.describedAs = describedAs;
    }

    // Equivale a assertThat(value)
    static <T> FluentAssert<T> assertThat(T value) {
        return new FluentAssert<>(value, String.valueOf(value));
    }

    static <T> FluentAssert<T> assertThat(T value, String as) {
        return new FluentAssert<>(value, as);
    }

    // .isEqualTo(expected)
    FluentAssert<T> isEqualTo(T expected) {
        if (!failed) {
            boolean ok = expected == null ? actual == null : expected.equals(actual);
            if (!ok) fail("isEqualTo(" + expected + ")", "but was <" + actual + ">");
        }
        return this;
    }

    // .isNotNull()
    FluentAssert<T> isNotNull() {
        if (!failed && actual == null) fail("isNotNull()", "but was null");
        return this;
    }

    // .isNull()
    FluentAssert<T> isNull() {
        if (!failed && actual != null) fail("isNull()", "but was <" + actual + ">");
        return this;
    }

    // .isGreaterThan(value) — solo para Comparable
    @SuppressWarnings("unchecked")
    FluentAssert<T> isGreaterThan(T value) {
        if (!failed) {
            int cmp = ((Comparable<T>) actual).compareTo(value);
            if (cmp <= 0) fail("isGreaterThan(" + value + ")", "but <" + actual + "> is not greater");
        }
        return this;
    }

    // .isLessThan(value)
    @SuppressWarnings("unchecked")
    FluentAssert<T> isLessThan(T value) {
        if (!failed) {
            int cmp = ((Comparable<T>) actual).compareTo(value);
            if (cmp >= 0) fail("isLessThan(" + value + ")", "but <" + actual + "> is not less");
        }
        return this;
    }

    // .contains(element) — para colecciones o strings
    FluentAssert<T> contains(Object element) {
        if (!failed) {
            boolean ok;
            if (actual instanceof Collection<?> c) ok = c.contains(element);
            else if (actual instanceof String s)   ok = s.contains(String.valueOf(element));
            else { fail("contains", "unsupported type"); return this; }
            if (!ok) fail("contains(" + element + ")", "but <" + actual + "> does not contain it");
        }
        return this;
    }

    // .hasSize(size) — para colecciones o strings
    FluentAssert<T> hasSize(int size) {
        if (!failed) {
            int actualSize;
            if (actual instanceof Collection<?> c) actualSize = c.size();
            else if (actual instanceof String s)   actualSize = s.length();
            else { fail("hasSize", "unsupported type"); return this; }
            if (actualSize != size) fail("hasSize(" + size + ")", "but size was " + actualSize);
        }
        return this;
    }

    // .startsWith(prefix) — para strings
    FluentAssert<T> startsWith(String prefix) {
        if (!failed) {
            if (!(actual instanceof String s) || !s.startsWith(prefix))
                fail("startsWith(\"" + prefix + "\")", "but was <" + actual + ">");
        }
        return this;
    }

    // .satisfies(consumer) — comprobación personalizada
    FluentAssert<T> satisfies(Consumer<T> consumer) {
        if (!failed) {
            try {
                consumer.accept(actual);
            } catch (AssertionError e) {
                fail("satisfies(lambda)", e.getMessage());
            }
        }
        return this;
    }

    // Extrae el resultado
    AssertionResult toResult(String testName) {
        return new AssertionResult(testName, !failed, failed ? failReason : null);
    }

    private void fail(String assertion, String detail) {
        failed     = true;
        failReason = "[" + describedAs + "] " + assertion + " → " + detail;
    }
}

// ── Runner de demo ────────────────────────────────────────────────────────────

class AssertionDemo {

    static void print(AssertionResult r, boolean fluent) {
        String style = fluent ? "[AssertJ] " : "[JUnit ] ";
        if (r.passed)  System.out.println("    " + style + "PASS — " + r.description);
        else           System.out.println("    " + style + "FAIL — " + r.description
            + "\n            Mensaje: " + r.failMessage);
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpAssertions {
    public static void main(String[] args) {

        System.out.println("=== Simulación AssertJ vs JUnit assertEquals ===\n");
        System.out.println("Las mismas 8 aserciones, escritas de ambas formas.\n");

        // ─── Datos de prueba ──────────────────────────────────────────────────
        String  nombre     = "Jorge";
        int     edad       = 28;
        List<String> roles = List.of("ADMIN", "USER");
        String  email      = "jorge@example.com";
        Object  nulo       = null;

        // ─── 1. Igualdad ──────────────────────────────────────────────────────
        System.out.println("[ 1. isEqualTo / assertEquals ]");
        AssertionDemo.print(JUnitAssert.assertEquals("Jorge", nombre, "nombre es 'Jorge'"), false);
        AssertionDemo.print(FluentAssert.assertThat(nombre, "nombre").isEqualTo("Jorge")
            .toResult("nombre es 'Jorge'"), true);

        // ─── 2. Mayor que ─────────────────────────────────────────────────────
        System.out.println("\n[ 2. isGreaterThan / assertTrue > ]");
        AssertionDemo.print(JUnitAssert.assertTrue(edad > 18, "edad > 18"), false);
        AssertionDemo.print(FluentAssert.assertThat(edad, "edad").isGreaterThan(18)
            .toResult("edad > 18"), true);

        // ─── 3. Contiene elemento ─────────────────────────────────────────────
        System.out.println("\n[ 3. contains ]");
        AssertionDemo.print(JUnitAssert.assertTrue(roles.contains("ADMIN"), "roles contiene ADMIN"), false);
        AssertionDemo.print(FluentAssert.assertThat(roles, "roles").contains("ADMIN")
            .toResult("roles contiene ADMIN"), true);

        // ─── 4. No nulo ───────────────────────────────────────────────────────
        System.out.println("\n[ 4. isNotNull / assertTrue != null ]");
        AssertionDemo.print(JUnitAssert.assertTrue(email != null, "email no es null"), false);
        AssertionDemo.print(FluentAssert.assertThat(email, "email").isNotNull()
            .toResult("email no es null"), true);

        // ─── 5. Tamaño ────────────────────────────────────────────────────────
        System.out.println("\n[ 5. hasSize / assertEquals size ]");
        AssertionDemo.print(JUnitAssert.assertEquals(2, roles.size(), "roles tiene 2 elementos"), false);
        AssertionDemo.print(FluentAssert.assertThat(roles, "roles").hasSize(2)
            .toResult("roles tiene 2 elementos"), true);

        // ─── 6. Empieza con ───────────────────────────────────────────────────
        System.out.println("\n[ 6. startsWith / assertTrue startsWith ]");
        AssertionDemo.print(JUnitAssert.assertTrue(email.startsWith("jorge"), "email empieza con 'jorge'"), false);
        AssertionDemo.print(FluentAssert.assertThat(email, "email").startsWith("jorge")
            .toResult("email empieza con 'jorge'"), true);

        // ─── 7. Es null ───────────────────────────────────────────────────────
        System.out.println("\n[ 7. isNull / assertNull ]");
        AssertionDemo.print(JUnitAssert.assertNull(nulo, "nulo es null"), false);
        AssertionDemo.print(FluentAssert.assertThat(nulo, "nulo").isNull()
            .toResult("nulo es null"), true);

        // ─── 8. Satisfies (lambda personalizada) ─────────────────────────────
        System.out.println("\n[ 8. satisfies — comprobación compuesta ]");
        AssertionDemo.print(JUnitAssert.assertTrue(
            nombre.length() >= 2 && nombre.charAt(0) == 'J', "nombre válido (len>=2 y empieza con J)"), false);
        AssertionDemo.print(FluentAssert.assertThat(nombre, "nombre").satisfies(n -> {
            if (n.length() < 2)        throw new AssertionError("longitud < 2");
            if (n.charAt(0) != 'J')    throw new AssertionError("no empieza con J");
        }).toResult("nombre válido (len>=2 y empieza con J)"), true);

        // ─── Caso de fallo para mostrar el mensaje de error ───────────────────
        System.out.println("\n" + "─".repeat(60));
        System.out.println("\n[ Comparativa de mensajes de fallo con assertion incorrecta ]\n");
        System.out.println("  Valor real: nombre = \"Jorge\", assertion: isEqualTo(\"Ana\")\n");
        AssertionDemo.print(JUnitAssert.assertEquals("Ana", nombre, "nombre es 'Ana'"), false);
        AssertionDemo.print(FluentAssert.assertThat(nombre, "nombre").isEqualTo("Ana")
            .toResult("nombre es 'Ana'"), true);

        System.out.println("\n[ Conclusión ]");
        System.out.println("  JUnit  : assertEquals(expected, actual) — funciona pero mensaje genérico.");
        System.out.println("  AssertJ: fluent, encadenable, mensaje descriptivo que dice el valor real.");
        System.out.println("  AssertJ también ofrece: usingRecursiveComparison(), extracting(), filteredOn().");
    }
}
