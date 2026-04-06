package base.colecciones.mapaa.implementaciones;

import java.util.IdentityHashMap;

public class ExpIdentityHashMap {

    /*
        IdentityHashMap compara las claves usando == en vez de equals().
        Útil cuando quieres distinción entre objetos diferentes aunque sean "iguales".
     */


    /*                             KEY FEATURES                            */

    /*
        - Compara referencias (==) en lugar de equals().
        - Permite null (una clave).
        - No garantiza orden.
     */


    public static void main(String[] args) {

        IdentityHashMap<String, String> map = new IdentityHashMap<>();

        String k1 = new String("Java");
        String k2 = new String("Java");

        map.put(k1, "uno");
        map.put(k2, "dos"); // distinto porque compara ==, no equals()
        System.out.println("IdentityHashMap → " + map);
    }
}
