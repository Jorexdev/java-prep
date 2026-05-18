import java.util.List;
import java.util.Optional;

public class Ejercicio6 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(4, 2, 9, 1, 7, 3, 8, 5, 6);
        System.out.println("Min:          " + nums.stream().min(Integer::compareTo).orElse(-1));
        System.out.println("Max:          " + nums.stream().max(Integer::compareTo).orElse(-1));
        Optional<Integer> primeroMayor5 = nums.stream().filter(n -> n > 5).findFirst();
        System.out.println("Primero > 5:  " + primeroMayor5.orElse(-1));
    }
}
