import java.util.ArrayList;
import java.util.List;
public class Ejercicio1 {
    abstract static class EstrategiaOrdenamiento {
        protected final String nombre;
        EstrategiaOrdenamiento(String nombre) { this.nombre = nombre; }
        abstract void ordenar(List<Integer> lista);
        // Abstract class aquí permite compartir el campo 'nombre' sin repetición.
        // Usa interface si no necesitas estado compartido y quieres que la estrategia
        // sea implementable por clases que ya extienden otra jerarquía.
    }
    static class BubbleSort extends EstrategiaOrdenamiento {
        BubbleSort() { super("Bubble Sort"); }
        @Override void ordenar(List<Integer> lista) {
            int n = lista.size();
            for (int i = 0; i < n - 1; i++)
                for (int j = 0; j < n - i - 1; j++)
                    if (lista.get(j) > lista.get(j + 1)) {
                        int tmp = lista.get(j); lista.set(j, lista.get(j+1)); lista.set(j+1, tmp);
                    }
        }
    }
    static class InsertionSort extends EstrategiaOrdenamiento {
        InsertionSort() { super("Insertion Sort"); }
        @Override void ordenar(List<Integer> lista) {
            for (int i = 1; i < lista.size(); i++) {
                int key = lista.get(i), j = i - 1;
                while (j >= 0 && lista.get(j) > key) { lista.set(j + 1, lista.get(j)); j--; }
                lista.set(j + 1, key);
            }
        }
    }
    public static void main(String[] args) {
        List<Integer> nums1 = new ArrayList<>(List.of(5, 2, 8, 1, 9, 3));
        List<Integer> nums2 = new ArrayList<>(List.of(5, 2, 8, 1, 9, 3));
        EstrategiaOrdenamiento bubble    = new BubbleSort();
        EstrategiaOrdenamiento insertion = new InsertionSort();
        bubble.ordenar(nums1);    System.out.println(bubble.nombre    + ": " + nums1);
        insertion.ordenar(nums2); System.out.println(insertion.nombre + ": " + nums2);
    }
}
