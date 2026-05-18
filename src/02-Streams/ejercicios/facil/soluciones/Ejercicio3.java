import java.util.List;

public class Ejercicio3 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(4, 8, 15, 16, 23, 42, 2, 6);
        System.out.println("Pares:          " + nums.stream().filter(n -> n % 2 == 0).count());
        System.out.println("Alguno > 100:   " + nums.stream().anyMatch(n -> n > 100));
        System.out.println("Todos positivos:" + nums.stream().allMatch(n -> n > 0));
        System.out.println("Ninguno cero:   " + nums.stream().noneMatch(n -> n == 0));
    }
}
