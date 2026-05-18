public class Ejercicio4 {

    static class Resultado<T, E extends Exception> {
        private final T valor;
        private final E error;

        private Resultado(T valor, E error) {
            this.valor = valor;
            this.error = error;
        }

        static <T, E extends Exception> Resultado<T, E> exito(T valor) {
            return new Resultado<>(valor, null);
        }

        @SuppressWarnings("unchecked")
        static <T, E extends Exception> Resultado<T, E> error(E error) {
            return new Resultado<>(null, error);
        }

        boolean esExito() { return error == null; }
        T getValor()      { return valor; }
        E getError()      { return error; }
    }

    @SuppressWarnings("unchecked")
    static Resultado<Integer, NumberFormatException> parsear(String s) {
        try {
            return Resultado.exito(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Resultado.error(e);
        }
    }

    public static void main(String[] args) {

        for (String input : new String[]{"42", "abc", "100", "xyz"}) {
            Resultado<Integer, NumberFormatException> res = parsear(input);
            if (res.esExito()) {
                System.out.println("'" + input + "' → " + res.getValor());
            } else {
                System.out.println("'" + input + "' → error: " + res.getError().getMessage());
            }
        }
    }
}
