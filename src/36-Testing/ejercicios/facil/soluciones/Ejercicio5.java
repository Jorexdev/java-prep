public class Ejercicio5 {

    static class Nota {
        private final int valor;

        Nota(int valor) {
            if (valor < 0 || valor > 10)
                throw new IllegalArgumentException("Nota fuera de rango [0-10]: " + valor);
            this.valor = valor;
        }

        String getCalificacion() {
            if (valor < 5)  return "Suspenso";
            if (valor <= 6) return "Aprobado";
            if (valor <= 8) return "Notable";
            return "Sobresaliente";
        }
    }

    static void testCalificacion(int valor, String esperada) {
        String nombre = "Nota(" + valor + ") == " + esperada;
        try {
            Nota n = new Nota(valor);
            String actual = n.getCalificacion();
            if (esperada.equals(actual)) {
                System.out.println("PASS: " + nombre);
            } else {
                System.out.println("FAIL: " + nombre + " — obtenido: " + actual);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("FAIL: " + nombre + " — excepción inesperada: " + e.getMessage());
        }
    }

    static void testInvalida(int valor) {
        String nombre = "Nota(" + valor + ") lanza IllegalArgumentException";
        try {
            new Nota(valor);
            System.out.println("FAIL: " + nombre + " — no se lanzó excepción");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: " + nombre);
        }
    }

    public static void main(String[] args) {
        testInvalida(-1);
        testCalificacion(0,  "Suspenso");
        testCalificacion(4,  "Suspenso");
        testCalificacion(5,  "Aprobado");
        testCalificacion(6,  "Aprobado");
        testCalificacion(7,  "Notable");
        testCalificacion(8,  "Notable");
        testCalificacion(9,  "Sobresaliente");
        testCalificacion(10, "Sobresaliente");
        testInvalida(11);
    }
}
