import java.util.List;

public class Ejercicio2 {

    static <T extends Number> double suma(List<T> nums) {
        double total = 0;
        for (T n : nums) {
            total += n.doubleValue();
        }
        return total;
    }

    public static void main(String[] args) {

        List<Integer> enteros  = List.of(1, 2, 3, 4, 5);
        List<Double>  decimales = List.of(1.5, 2.5, 3.0);
        List<Long>    largos    = List.of(100L, 200L, 300L);

        System.out.println("Suma de enteros:   " + suma(enteros));    // 15.0
        System.out.println("Suma de doubles:   " + suma(decimales));  // 7.0
        System.out.println("Suma de longs:     " + suma(largos));     // 600.0
    }
}
