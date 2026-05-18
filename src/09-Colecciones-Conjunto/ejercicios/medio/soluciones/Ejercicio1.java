import java.util.HashSet;
import java.util.Set;

public class Ejercicio1 {
    public static void main(String[] args) {
        Set<Integer> setA = new HashSet<>(Set.of(1, 2, 3, 4));
        Set<Integer> setB = new HashSet<>(Set.of(3, 4, 5, 6));

        System.out.println("Conjunto A: " + setA);
        System.out.println("Conjunto B: " + setB);

        // Unión: A ∪ B
        Set<Integer> union = new HashSet<>(setA);
        union.addAll(setB);
        System.out.println("Unión A ∪ B:                " + union);

        // Intersección: A ∩ B
        Set<Integer> interseccion = new HashSet<>(setA);
        interseccion.retainAll(setB);
        System.out.println("Intersección A ∩ B:         " + interseccion);

        // Diferencia simétrica: (A ∪ B) - (A ∩ B)
        Set<Integer> difSimetrica = new HashSet<>(union);
        difSimetrica.removeAll(interseccion);
        System.out.println("Diferencia simétrica A △ B: " + difSimetrica);
    }
}
