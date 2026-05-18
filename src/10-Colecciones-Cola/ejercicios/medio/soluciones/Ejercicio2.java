import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Ejercicio2 {

    static List<Integer> topKMenores(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int n : nums) minHeap.add(n);

        List<Integer> resultado = new ArrayList<>();
        for (int i = 0; i < k && !minHeap.isEmpty(); i++) {
            resultado.add(minHeap.poll());
        }
        return resultado;
    }

    public static void main(String[] args) {
        int[] nums = {5, 1, 8, 2, 9, 3, 7, 4, 6};
        int k = 3;

        System.out.println("Array: [5, 1, 8, 2, 9, 3, 7, 4, 6]");
        System.out.println("Top " + k + " más pequeños: " + topKMenores(nums, k)); // [1, 2, 3]

        k = 5;
        System.out.println("Top " + k + " más pequeños: " + topKMenores(nums, k)); // [1, 2, 3, 4, 5]
    }
}
