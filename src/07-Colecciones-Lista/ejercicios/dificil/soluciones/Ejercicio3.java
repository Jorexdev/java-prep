import java.util.ArrayList;
import java.util.List;

public class Ejercicio3 {

    public static List<List<Integer>> powerSet(List<Integer> lista) {
        int n = lista.size();
        int total = 1 << n; // 2^n subconjuntos
        List<List<Integer>> resultado = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            List<Integer> subconjunto = new ArrayList<>();
            for (int bit = 0; bit < n; bit++) {
                if ((i & (1 << bit)) != 0) {
                    subconjunto.add(lista.get(bit));
                }
            }
            resultado.add(subconjunto);
        }
        return resultado;
    }

    public static void main(String[] args) {
        List<Integer> lista = List.of(1, 2, 3);
        List<List<Integer>> conjuntos = powerSet(lista);

        System.out.println("Power set de " + lista + " (" + conjuntos.size() + " subconjuntos):");
        for (List<Integer> sub : conjuntos) {
            System.out.println("  " + sub);
        }

        System.out.println("\nPower set de [1,2,3,4]: " + powerSet(List.of(1, 2, 3, 4)).size() + " subconjuntos");
    }
}
