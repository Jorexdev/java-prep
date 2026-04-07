package base.colecciones.mapa.implementaciones;

import java.util.WeakHashMap;

public class ExpWeakHashMap {

    /*
        WeakHashMap usa referencias débiles para las claves.
        Cuando una clave ya no tiene referencias fuertes, el recolector de basura
        puede eliminar su entrada automáticamente.
        Útil para cachés y estructuras de memoria temporales.
     */


    /*                             KEY FEATURES                            */

    /*
        - Claves almacenadas como referencias débiles.
        - Elementos eliminados automáticamente por el GC.
        - No sincronizado.
     */


    public static void main(String[] args) {

        WeakHashMap<Object, String> map = new WeakHashMap<>();

        Object clave1 = new Object();
        Object clave2 = new Object();

        map.put(clave1, "valor1");
        map.put(clave2, "valor2");
        System.out.println("WeakHashMap inicial: " + map);

        // Liberamos una referencia
        clave1 = null;
        System.gc(); // sugerencia al GC

        // Entrada con clave sin referencias puede ser eliminada
        System.out.println("Tras GC: " + map);
    }
}
