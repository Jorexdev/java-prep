package base.colecciones.conjunto.implementaciones;

import java.util.SortedSet;
import java.util.TreeSet;

public class ExpSortedSet {

    /*
        En Java, SortedSet es una interfaz que extiende Set.
        Representa un conjunto en el que los elementos están almacenados en orden ASCENDENTE,
        ya sea según el orden natural de los elementos (Comparable) o mediante un Comparator.
     */


    /*                           KEY FEATURES                            */

    /*
        - Mantiene los elementos en orden ascendente.
        - No permite duplicados.
        - Permite null SOLO si está vacío (de lo contrario lanza NullPointerException).
        - Implementación más común: TreeSet.
     */


    /*                            MÉTODOS CLAVE                          */

    /*
        - comparator(): devuelve el Comparator usado o null si usa orden natural.
        - first(): devuelve el primer elemento (mínimo).
        - last(): devuelve el último elemento (máximo).
        - headSet(E toElement): elementos menores que toElement.
        - tailSet(E fromElement): elementos mayores o iguales que fromElement.
        - subSet(E fromElement, E toElement): elementos en el rango [from, to).
     */


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
