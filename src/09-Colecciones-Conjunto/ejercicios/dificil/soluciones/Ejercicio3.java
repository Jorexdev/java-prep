import java.util.NavigableSet;
import java.util.TreeSet;

public class Ejercicio3 {
    public static void main(String[] args) {
        NavigableSet<Integer> ns = new TreeSet<>();
        for (int v : new int[]{1, 3, 5, 7, 9, 11, 13}) ns.add(v);
        System.out.println("Conjunto: " + ns);

        // floor: mayor elemento <= x
        System.out.println("\nfloor(6)    → " + ns.floor(6));     // 5
        System.out.println("floor(5)    → " + ns.floor(5));       // 5 (coincide)
        System.out.println("ceiling(6)  → " + ns.ceiling(6));     // 7
        System.out.println("ceiling(7)  → " + ns.ceiling(7));     // 7 (coincide)

        System.out.println("\nheadSet(7)           → " + ns.headSet(7));          // {1,3,5}
        System.out.println("headSet(7, inclusive) → " + ns.headSet(7, true));    // {1,3,5,7}
        System.out.println("tailSet(9)            → " + ns.tailSet(9));          // {9,11,13}
        System.out.println("subSet(5, 11)         → " + ns.subSet(5, 11));       // {5,7,9}
        System.out.println("subSet(5,true,11,true)→ " + ns.subSet(5, true, 11, true)); // {5,7,9,11}

        // pollFirst y pollLast modifican el set
        System.out.println("\npollFirst() → " + ns.pollFirst()); // 1
        System.out.println("pollLast()  → " + ns.pollLast());   // 13
        System.out.println("Conjunto tras polls: " + ns);
    }
}
