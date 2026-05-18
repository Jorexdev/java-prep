import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    static List<Integer> sumasVentana(int[] nums, int k) {
        List<Integer> sumas = new ArrayList<>();
        int sumaActual = 0;

        for (int i = 0; i < nums.length; i++) {
            sumaActual += nums[i];

            // Cuando la ventana ha alcanzado tamaño k
            if (i >= k - 1) {
                sumas.add(sumaActual);
                sumaActual -= nums[i - k + 1]; // elimina el elemento que sale de la ventana
            }
        }
        return sumas;
    }

    public static void main(String[] args) {
        int[] nums = {2, 4, 6, 8, 10, 12, 14};
        int k = 3;

        System.out.println("Array: [2, 4, 6, 8, 10, 12, 14]");
        System.out.println("Tamaño de ventana k=" + k);
        System.out.println("Sumas de cada ventana: " + sumasVentana(nums, k));
        // Ventanas: [2,4,6]=12, [4,6,8]=18, [6,8,10]=24, [8,10,12]=30, [10,12,14]=36
    }
}
