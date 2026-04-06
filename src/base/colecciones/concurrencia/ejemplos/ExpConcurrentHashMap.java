package base.colecciones.concurrencia.ejemplos;

import java.util.concurrent.ConcurrentHashMap;

public class ExpConcurrentHashMap {

    /*
        ConcurrentHashMap es un Map concurrente de alto rendimiento.
        Permite accesos y actualizaciones simultáneas sin bloquear toda la tabla.
        Usa segmentación interna/locks finos y operaciones atómicas en algunas APIs.
     */

    /*                             KEY FEATURES                            */

    /*
        - Thread-safe con alto rendimiento (mejor que Hashtable/synchronizedMap).
        - No permite claves ni valores null.
        - Operaciones atómicas útiles: putIfAbsent, computeIfAbsent, merge, etc.
        - Iteradores "débilmente consistentes": no lanzan ConcurrentModificationException.
     */

    /*                            VENTAJAS                                 */

    /*
        - Excelente rendimiento en lectura/escritura concurrente.
        - Métodos compuestos atómicos que evitan race conditions comunes.
     */

    /*                           DESVENTAJAS                               */

    /*
        - No ordena las claves (como HashMap).
        - No es una estructura transaccional (operaciones compuestas deben ser atómicas).
     */

    public static void main(String[] args) {

        ConcurrentHashMap<String, Integer> mapa = new ConcurrentHashMap<>();

        // put(K,V): inserta o reemplaza el valor de una clave
        mapa.put("Java", 1);
        mapa.put("Spring", 2);

        // getOrDefault(K, default): obtiene valor o por defecto si no existe
        System.out.println("getOrDefault('JPA', 0) → " + mapa.getOrDefault("JPA", 0));

        // putIfAbsent(K,V): inserta solo si la clave no existe (atómico)
        mapa.putIfAbsent("Hibernate", 3);

        // computeIfAbsent(K, mappingFn): calcula e inserta si falta (atómico)
        mapa.computeIfAbsent("JPA", k -> 4);

        // compute(K, remappingFn): recalcula valor existente (puede borrar si retorna null)
        mapa.compute("Spring", (k, v) -> v == null ? 1 : v + 10); // 2 → 12

        // merge(K, value, remappingFn): combina valor existente con el nuevo (atómico)
        mapa.merge("Java", 5, Integer::sum); // 1 + 5 → 6

        // replace(K, oldV, newV): reemplaza condicionalmente si coincide el valor actual
        mapa.replace("Hibernate", 3, 33);

        // forEach: recorre weakly-consistent, sin C.M.E.
        mapa.forEach(1, (k, v) -> System.out.println(k + " → " + v));

        // keySet/values/entrySet: vistas para recorrer
        System.out.println("keys → " + mapa.keySet());
        System.out.println("values → " + mapa.values());
        System.out.println("entries → " + mapa.entrySet());

        // size/isEmpty
        System.out.println("size → " + mapa.size() + ", isEmpty → " + mapa.isEmpty());

        // clear(): borra todo
        mapa.clear();
        System.out.println("Tras clear → size: " + mapa.size());

        // ========= MÉTODOS QUE SOLO MENCIONO / MATICES IMPORTANTES =========
        // - replaceAll(BiFunction): reemplaza todos los valores de forma atómica por entrada.
        // - search / reduce / forEach (con parallelismThreshold): operaciones paralelas internas.
    }
}
