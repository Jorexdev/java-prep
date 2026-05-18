public class Ejercicio2 {

    static class Conversor<I, O> {
        private final java.util.function.Function<I, O> fn;

        Conversor(java.util.function.Function<I, O> fn) {
            this.fn = fn;
        }

        O convertir(I input) {
            return fn.apply(input);
        }

        <N> Conversor<I, N> andThen(Conversor<O, N> siguiente) {
            return new Conversor<>(input -> siguiente.convertir(this.convertir(input)));
        }
    }

    public static void main(String[] args) {

        Conversor<String, Integer> parsear   = new Conversor<>(Integer::parseInt);
        Conversor<Integer, Double> duplicar  = new Conversor<>(n -> n * 2.0);
        Conversor<Double, String>  formatear = new Conversor<>(d -> String.format("%.2f€", d));

        // Encadenar los tres: String → Integer → Double → String
        Conversor<String, String> pipeline = parsear.andThen(duplicar).andThen(formatear);

        System.out.println(pipeline.convertir("5"));   // 10.00€
        System.out.println(pipeline.convertir("100")); // 200.00€
        System.out.println(pipeline.convertir("42"));  // 84.00€
    }
}
