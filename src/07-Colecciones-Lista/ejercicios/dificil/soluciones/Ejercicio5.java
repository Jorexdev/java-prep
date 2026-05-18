import java.util.ArrayList;
import java.util.LinkedList;

public class Ejercicio5 {

    public static void main(String[] args) {
        int N = 10_000;

        // --- ArrayList: insertar al inicio ---
        ArrayList<Integer> arrayList = new ArrayList<>();
        long inicio = System.nanoTime();
        for (int i = 0; i < N; i++) {
            arrayList.add(0, i); // O(n) por cada inserción
        }
        long tiempoAL = System.nanoTime() - inicio;

        // --- LinkedList: insertar al inicio ---
        LinkedList<Integer> linkedList = new LinkedList<>();
        inicio = System.nanoTime();
        for (int i = 0; i < N; i++) {
            linkedList.add(0, i); // O(1) para inserción al frente
        }
        long tiempoLL = System.nanoTime() - inicio;

        System.out.println("Insertar " + N + " elementos al índice 0:");
        System.out.printf("  ArrayList  : %6.2f ms%n", tiempoAL / 1_000_000.0);
        System.out.printf("  LinkedList : %6.2f ms%n", tiempoLL / 1_000_000.0);
        System.out.printf("  LinkedList es ~%.1fx más rápida%n",
            (double) tiempoAL / tiempoLL);
        System.out.println("\nConclusión: LinkedList es mucho más eficiente para inserciones");
        System.out.println("al inicio/medio porque no necesita desplazar elementos.");
    }
}
