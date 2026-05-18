import java.util.List;

public class Ejercicio5 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int suma = nums.stream().reduce(0, Integer::sum);
        long producto = nums.stream().reduce(1, (a, b) -> a * b);
        System.out.println("Suma:     " + suma);
        System.out.println("Producto: " + producto);
    }
}
