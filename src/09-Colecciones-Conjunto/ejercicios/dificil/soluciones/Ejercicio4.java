import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ejercicio4 {

    static Set<Set<Integer>> powerSet(Set<Integer> original) {
        List<Integer> elementos = new ArrayList<>(original);
        int n = elementos.size();
        Set<Set<Integer>> resultado = new HashSet<>();

        // Para n elementos hay 2^n subconjuntos posibles
        for (int mascara = 0; mascara < (1 << n); mascara++) {
            Set<Integer> subconjunto = new HashSet<>();
            for (int bit = 0; bit < n; bit++) {
                if ((mascara & (1 << bit)) != 0) {
                    subconjunto.add(elementos.get(bit));
                }
            }
            resultado.add(subconjunto);
        }
        return resultado;
    }

    public static void main(String[] args) {
        Set<Integer> base = Set.of(1, 2, 3);
        System.out.println("Conjunto base: " + base);

        Set<Set<Integer>> ps = powerSet(base);
        System.out.println("Tamaño del power set: " + ps.size() + " (esperado: " + (1 << base.size()) + ")");
        System.out.println("Subconjuntos:");
        ps.stream()
          .sorted((a, b) -> Integer.compare(a.size(), b.size()))
          .forEach(s -> System.out.println("  " + s));
    }
}
