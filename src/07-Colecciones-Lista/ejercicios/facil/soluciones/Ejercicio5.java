import java.util.ArrayList;
import java.util.List;

public class Ejercicio5 {
    public static void main(String[] args) {
        List<Integer> lista = new ArrayList<>(List.of(10, 20, 30, 40, 50, 60, 70, 80));
        System.out.println("Original: " + lista);

        // subList devuelve una vista respaldada por la lista original (backed view)
        List<Integer> sub = lista.subList(2, 5); // índices [2, 5) → 30, 40, 50
        System.out.println("subList(2,5): " + sub);

        // clear() sobre la vista elimina esos elementos de la lista original
        sub.clear();
        System.out.println("Tras sub.clear(), lista original: " + lista);
        // → [10, 20, 60, 70, 80]
        // subList es una vista, no una copia; cualquier cambio en ella afecta al backing list
    }
}
