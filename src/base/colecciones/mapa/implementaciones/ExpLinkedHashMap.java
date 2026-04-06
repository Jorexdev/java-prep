package base.colecciones.mapa.implementaciones;

import java.util.LinkedHashMap;

public class ExpLinkedHashMap {

    /*
        LinkedHashMap mantiene los pares clave-valor en orden de inserción
        (o de acceso, si se configura).
        Útil para cachés (ej: LRU).
     */


    /*                             KEY FEATURES                            */

    /*
        - Orden predecible (inserción o acceso).
        - O(1) en operaciones básicas.
        - Permite null (una clave y múltiples valores).
     */


    public static void main(String[] args) {

        // LinkedHashMap con orden de inserción
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();

        map.put(1, "Java");
        map.put(2, "Spring");
        map.put(3, "Hibernate");
        System.out.println("LinkedHashMap → " + map);

        // get(K): acceso normal
        System.out.println("get(1): " + map.get(1));

        // containsKey / containsValue
        System.out.println("containsKey(2): " + map.containsKey(2));
        System.out.println("containsValue(\"Hibernate\"): " + map.containsValue("Hibernate"));

        // remove(K)
        map.remove(3);
        System.out.println("Tras remove(3): " + map);

        // iteration → respeta orden de inserción
        map.forEach((k, v) -> System.out.println(k + " → " + v));
    }
}
