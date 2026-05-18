import java.util.List;

public class Ejercicio1 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(3, 15, 7, 22, 1, 18, 9, 30, 5);
        System.out.println("Mayores de 10:");
        nums.stream()
            .filter(n -> n > 10)
            .forEach(System.out::println);
    }
}
