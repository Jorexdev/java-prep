import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Ejercicio1 {
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(4, 1, 7, 2, 9, 3, 7, 5, 7));

        System.out.println("Min:       " + Collections.min(nums));
        System.out.println("Max:       " + Collections.max(nums));
        System.out.println("Freq(7):   " + Collections.frequency(nums, 7));
    }
}
