public class Ejercicio1 {

    @FunctionalInterface
    interface Transformador<T, R> {
        R transformar(T t);
    }

    public static void main(String[] args) {

        Transformador<String, Integer> aLongitud = s -> s.length();

        Transformador<Integer, String> aBinario = n -> Integer.toBinaryString(n);

        System.out.println("\"lambda\" tiene longitud: " + aLongitud.transformar("lambda"));
        System.out.println("10 en binario: " + aBinario.transformar(10));
        System.out.println("255 en binario: " + aBinario.transformar(255));
    }
}
