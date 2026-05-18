public class Ejercicio1 {
    static class Resultado<T> {
        private final T valor;
        private final String error;
        private Resultado(T valor, String error) { this.valor = valor; this.error = error; }
        static <T> Resultado<T> exito(T valor)       { return new Resultado<>(valor, null); }
        static <T> Resultado<T> error(String mensaje) { return new Resultado<>(null, mensaje); }
        boolean esExito()  { return error == null; }
        T       getValor() { return valor; }
        String  getError() { return error; }
    }
    static Resultado<Integer> parsear(String s) {
        try { return Resultado.exito(Integer.parseInt(s)); }
        catch (NumberFormatException e) { return Resultado.error("No es un número: " + s); }
    }
    public static void main(String[] args) {
        for (String input : new String[]{"42", "abc", "-7", "3.14"}) {
            Resultado<Integer> r = parsear(input);
            System.out.println("'" + input + "' → " + (r.esExito() ? r.getValor() : "ERROR: " + r.getError()));
        }
    }
}
