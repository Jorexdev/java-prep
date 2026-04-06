package base.colecciones.mapaa.implementaciones;

import java.util.HashMap;

public class ExpHashMap {

    /*
        HashMap almacena pares clave-valor en una tabla hash.
        No garantiza orden de inserción ni orden natural.
        Permite una clave null y múltiples valores null.
     */


    /*                             KEY FEATURES                            */

    /*
        - O(1) promedio en inserción/búsqueda/eliminación.
        - No sincronizado.
        - Permite null en una clave y múltiples valores.
     */


    public static void main(String[] args) {

        HashMap<Integer, String> map = new HashMap<>();

        // put(K,V): inserta o reemplaza un par clave-valor
        map.put(1, "Java");
        map.put(2, "Spring");
        map.put(3, "Hibernate");
        System.out.println("HashMap → " + map);

        // get(K): obtiene valor asociado
        System.out.println("get(2): " + map.get(2)); // Spring

        // containsKey / containsValue
        System.out.println("containsKey(1): " + map.containsKey(1));
        System.out.println("containsValue(\"Java\"): " + map.containsValue("Java"));

        // remove(K): elimina entrada por clave
        map.remove(3);
        System.out.println("Tras remove(3): " + map);

        // keySet / values / entrySet
        System.out.println("Keys: " + map.keySet());
        System.out.println("Values: " + map.values());
        System.out.println("Entries: " + map.entrySet());

        // replace(K,V)
        map.replace(2, "SpringBoot");
        System.out.println("Tras replace: " + map);

        // size / isEmpty
        System.out.println("size: " + map.size() + ", isEmpty: " + map.isEmpty());
    }
}
