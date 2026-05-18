import java.util.Set;

public class Ejercicio6 {

    static void verificarSubconjunto(String etiqueta, Set<?> a, Set<?> b) {
        boolean esSubconjunto = b.containsAll(a);
        System.out.printf("  %-25s → %s%n", etiqueta, esSubconjunto ? "SÍ es subconjunto" : "NO es subconjunto");
    }

    public static void main(String[] args) {
        Set<Integer> setA = Set.of(1, 2, 3);
        Set<Integer> setB = Set.of(1, 2, 3, 4, 5);
        Set<Integer> setC = Set.of(1, 2, 7);       // parcial
        Set<Integer> setD = Set.of(10, 20, 30);     // sin relación

        System.out.println("¿A ⊆ B? (B.containsAll(A))");
        verificarSubconjunto("A={1,2,3} ⊆ B={1,2,3,4,5}", setA, setB); // true
        verificarSubconjunto("B={1,2,3,4,5} ⊆ A={1,2,3}", setB, setA); // false
        verificarSubconjunto("C={1,2,7} ⊆ B={1,2,3,4,5}", setC, setB); // false
        verificarSubconjunto("D={10,20,30} ⊆ B={1,2,3,4,5}", setD, setB); // false
        verificarSubconjunto("A={1,2,3} ⊆ A={1,2,3}",  setA, setA); // true (un set es subconjunto de sí mismo)
    }
}
