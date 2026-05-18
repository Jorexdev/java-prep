import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class Ejercicio1 {

    static int[] ventanaMaxima(int[] nums, int k) {
        int[] resultado = new int[nums.length - k + 1];
        Deque<Integer> deque = new ArrayDeque<>(); // almacena índices

        for (int i = 0; i < nums.length; i++) {
            // Eliminar índices fuera de la ventana actual
            while (!deque.isEmpty() && deque.peekFirst() <= i - k) {
                deque.pollFirst();
            }

            // Mantener deque monótonamente decreciente:
            // eliminar del final los índices cuyo valor es <= nums[i]
            while (!deque.isEmpty() && nums[deque.peekLast()] <= nums[i]) {
                deque.pollLast();
            }

            deque.addLast(i);

            // La ventana tiene tamaño k cuando i >= k-1
            if (i >= k - 1) {
                resultado[i - k + 1] = nums[deque.peekFirst()];
            }
        }
        return resultado;
    }

    public static void main(String[] args) {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        int k = 3;

        System.out.println("Array: " + Arrays.toString(nums));
        System.out.println("k=" + k);
        System.out.println("Máximos por ventana: " + Arrays.toString(ventanaMaxima(nums, k)));
        // Esperado: [3, 3, 5, 5, 6, 7]

        int[] nums2 = {4, 2, 12, 3, 8, 5};
        System.out.println("\nArray: " + Arrays.toString(nums2));
        System.out.println("k=2, Máximos: " + Arrays.toString(ventanaMaxima(nums2, 2)));
    }
}
