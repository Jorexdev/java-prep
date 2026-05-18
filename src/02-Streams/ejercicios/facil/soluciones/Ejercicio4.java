import java.util.List;
import java.util.stream.Collectors;

public class Ejercicio4 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(9, 3, 7, 1, 5, 8, 2, 6, 4);
        List<Integer> resultado = nums.stream()
            .sorted()
            .skip(2)
            .limit(3)
            .collect(Collectors.toList());
        System.out.println("Sorted: " + nums.stream().sorted().collect(Collectors.toList()));
        System.out.println("skip(2).limit(3): " + resultado);
    }
}
