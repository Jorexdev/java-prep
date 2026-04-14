package base.colecciones.conjunto.implementaciones;

import java.util.SortedSet;
import java.util.TreeSet;

public class ExpSortedSet {











    public static void main(String[] args) {
        // Crear un SortedSet con TreeSet
        SortedSet<String> sortedSet = new TreeSet<>();

        // add(E e): añade elementos en orden natural
        sortedSet.add("Spring");
        sortedSet.add("Java");
        sortedSet.add("Hibernate");
        System.out.println("SortedSet → " + sortedSet); // [Hibernate, Java, Spring]

        // first() / last(): primer y último elemento
        System.out.println("first(): " + sortedSet.first()); // Hibernate
        System.out.println("last(): " + sortedSet.last());   // Spring

        // headSet(toElement): menores que "Spring"
        System.out.println("headSet('Spring') → " + sortedSet.headSet("Spring"));

        // tailSet(fromElement): mayores o iguales a "Java"
        System.out.println("tailSet('Java') → " + sortedSet.tailSet("Java"));

        // subSet(from, to): elementos entre "Hibernate" y "Spring"
        System.out.println("subSet('Hibernate','Spring') → " + sortedSet.subSet("Hibernate", "Spring"));
    }
}
