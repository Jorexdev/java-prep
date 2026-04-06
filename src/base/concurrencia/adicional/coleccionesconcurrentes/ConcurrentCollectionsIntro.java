package base.concurrencia.adicional.coleccionesconcurrentes;

public class ConcurrentCollectionsIntro {

    /*
       En Java, las colecciones concurrentes proporcionan seguridad de hilo y alto rendimiento
       sin necesidad de sincronizar manualmente. Dos muy usadas:
       - ConcurrentHashMap: mapa concurrente con alto throughput y iteradores weakly-consistent.
       - CopyOnWriteArrayList: lista segura para lectura intensiva, copia en escrituras.
    */

    /*                             KEY FEATURES                            */

    /*
        ► ConcurrentHashMap
        - Operaciones thread-safe sin bloqueos globales.
        - Iteradores "weakly-consistent": no lanzan ConcurrentModificationException.
        - Operaciones atómicas útiles: putIfAbsent, compute, computeIfAbsent, merge, replace.
        - No permite claves ni valores null.

        ► CopyOnWriteArrayList
        - En cada mutación (add, set, remove) crea una COPIA del array.
        - Iteradores snapshot: nunca C.M.E., ideales en lectura intensiva.
        - Escrituras costosas (O(n)), lecturas muy baratas.
    */

    /*                            VENTAJAS                                 */

    /*
        - Simplicidad de uso y seguridad de hilo sin synchronized manual.
        - Buen rendimiento en escenarios correctos:
          * CHM: alto paralelismo de lecturas/escrituras.
          * COWAL: muchas lecturas, pocas escrituras.
    */

    /*                            EJEMPLOS (idea)                          */

    /*
        ConcurrentHashMap<String,Integer> mapa = new ConcurrentHashMap<>();
        mapa.putIfAbsent("A", 1);
        mapa.compute("A", (k,v) -> v == null ? 1 : v+1);
        mapa.forEach( (k,v) -> System.out.println(k + "=" + v) ); // sin C.M.E.

        CopyOnWriteArrayList<String> lista = new CopyOnWriteArrayList<>();
        lista.add("uno");
        lista.forEach(System.out::println); // snapshot estable
    */
}
