package base.colecciones.mapaa.implementaciones;

import java.util.TreeMap;

public class ExpTreeMap {

    /*
        TreeMap es una implementación de Map basada en un árbol rojo-negro.
        Mantiene las claves ordenadas (Comparable o Comparator).
        No permite claves null.
     */


    /*                             KEY FEATURES                            */

    /*
        - Claves ordenadas ascendentemente (o con Comparator).
        - O(log n) en inserción, búsqueda y eliminación.
        - No permite claves null.
        - Permite múltiples valores null.
     */


    public static void main(String[] args) {

        TreeMap<Integer, String> map = new TreeMap<>();

        map.put(3, "Hibernate");
        map.put(1, "Java");
        map.put(2, "Spring");
        System.out.println("TreeMap → " + map); // {1=Java, 2=Spring, 3=Hibernate}

        // firstKey / lastKey
        System.out.println("firstKey(): " + map.firstKey());
        System.out.println("lastKey(): " + map.lastKey());

        // higherKey / lowerKey
        System.out.println("higherKey(2): " + map.higherKey(2));
        System.out.println("lowerKey(2): " + map.lowerKey(2));

        // subMap / headMap / tailMap
        System.out.println("subMap(1,3): " + map.subMap(1, 3)); // {1=Java, 2=Spring}
    }
}
