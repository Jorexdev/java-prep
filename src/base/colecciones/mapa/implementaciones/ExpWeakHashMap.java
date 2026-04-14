package base.colecciones.mapa.implementaciones;

import java.util.WeakHashMap;

public class ExpWeakHashMap {

    public static void main(String[] args) {

        // WeakHashMap almacena claves como referencias débiles
        // cuando una clave ya no tiene referencias fuertes, el GC puede eliminar su entrada
        WeakHashMap<Object, String> map = new WeakHashMap<>();

        Object clave1 = new Object();
        Object clave2 = new Object();

        map.put(clave1, "valor1");
        map.put(clave2, "valor2");
        System.out.println("WeakHashMap inicial: " + map.size() + " entradas");

        clave1 = null; // eliminamos la única referencia fuerte a clave1
        System.gc();   // sugerencia al GC — puede eliminar la entrada de clave1

        // tras el GC la entrada con clave1 puede haber desaparecido del mapa
        System.out.println("Tras GC: " + map.size() + " entradas (puede ser 1)");
    }
}
