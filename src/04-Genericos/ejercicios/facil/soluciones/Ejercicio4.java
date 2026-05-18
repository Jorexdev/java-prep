public class Ejercicio4 {

    // Par con dos parámetros de tipo independientes
    static class Par<A, B> {
        private final A primero;
        private final B segundo;

        public Par(A primero, B segundo) {
            this.primero = primero;
            this.segundo = segundo;
        }

        public A getPrimero() { return primero; }
        public B getSegundo() { return segundo; }

        @Override
        public String toString() {
            return "(" + primero + ", " + segundo + ")";
        }
    }

    public static void main(String[] args) {
        Par<String, Integer> edad = new Par<>("Ana", 30);
        System.out.println(edad);                    // (Ana, 30)
        System.out.println(edad.getPrimero());       // Ana
        System.out.println(edad.getSegundo());       // 30

        Par<Double, Boolean> resultado = new Par<>(9.8, true);
        System.out.println(resultado);               // (9.8, true)
    }
}
