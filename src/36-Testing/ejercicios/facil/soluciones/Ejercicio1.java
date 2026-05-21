public class Ejercicio1 {

    static class Assert {
        static void assertEquals(Object expected, Object actual, String nombre) {
            if (expected == null && actual == null) { pass(nombre); return; }
            if (expected != null && expected.equals(actual)) { pass(nombre); return; }
            throw new AssertionError("FAIL: " + nombre + " — esperado <" + expected + "> pero fue <" + actual + ">");
        }

        static void assertNotNull(Object o, String nombre) {
            if (o != null) { pass(nombre); return; }
            throw new AssertionError("FAIL: " + nombre + " — el valor es null");
        }

        static void assertTrue(boolean cond, String nombre) {
            if (cond) { pass(nombre); return; }
            throw new AssertionError("FAIL: " + nombre + " — se esperaba true");
        }

        static void assertFalse(boolean cond, String nombre) {
            if (!cond) { pass(nombre); return; }
            throw new AssertionError("FAIL: " + nombre + " — se esperaba false");
        }

        static void assertThrows(Class<? extends Exception> tipo, Runnable r, String nombre) {
            try {
                r.run();
                throw new AssertionError("FAIL: " + nombre + " — no se lanzó ninguna excepción");
            } catch (Exception e) {
                if (tipo.isInstance(e)) { pass(nombre); return; }
                throw new AssertionError("FAIL: " + nombre + " — se esperaba " + tipo.getSimpleName() + " pero fue " + e.getClass().getSimpleName());
            }
        }

        private static void pass(String nombre) {
            System.out.println("PASS: " + nombre);
        }
    }

    static class Calculadora {
        int sumar(int a, int b) { return a + b; }

        int dividir(int a, int b) {
            if (b == 0) throw new ArithmeticException("División por cero");
            return a / b;
        }
    }

    public static void main(String[] args) {
        Calculadora calc = new Calculadora();

        Assert.assertEquals(5, calc.sumar(2, 3), "sumar(2,3) == 5");
        Assert.assertEquals(0, calc.sumar(-1, 1), "sumar(-1,1) == 0");
        Assert.assertTrue(calc.sumar(10, 10) > 15, "sumar(10,10) > 15");
        Assert.assertFalse(calc.sumar(1, 1) == 3, "sumar(1,1) != 3");
        Assert.assertNotNull(calc, "calculadora no es null");
        Assert.assertThrows(ArithmeticException.class, () -> calc.dividir(5, 0), "dividir por cero lanza ArithmeticException");
    }
}
