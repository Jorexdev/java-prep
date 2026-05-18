import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio2 {
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(5, 3, 9, 1, 7, 4, 8, 2, 6));
        Collections.sort(nums);
        System.out.println("Ordenados: " + nums);

        int idx = Collections.binarySearch(nums, 7);
        System.out.println("binarySearch(7): índice " + idx + " → " + nums.get(idx));

        int noExiste = Collections.binarySearch(nums, 10);
        System.out.println("binarySearch(10): " + noExiste + " (negativo — punto inserción: " + (-noExiste - 1) + ")");
    }
}
