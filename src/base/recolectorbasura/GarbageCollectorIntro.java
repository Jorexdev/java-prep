package base.recolectorbasura;

public class GarbageCollectorIntro {

    /*
       En Java, el Garbage Collector (GC) es el proceso automático encargado de
       liberar memoria en el heap eliminando objetos que ya no son accesibles
       por ninguna referencia activa.

       Gracias al GC, el programador no necesita liberar memoria manualmente
       (como en C o C++), lo que reduce fugas de memoria y errores.

       No te aprendas nada de memoria, solo debes conocer que es el GC y para que sirve
    */


    /*                             KEY FEATURES                            */

    /*
        - Gestiona la memoria automáticamente.
        - Solo recolecta objetos que ya no son alcanzables.
        - Se centra en la memoria del heap (no en la stack).
        - El proceso puede causar pausas ("stop-the-world").
        - Algoritmos modernos reducen estas pausas (ej: G1, ZGC, Shenandoah).
    */


    /*                            FUNCIONAMIENTO                           */

    /*
        Java divide la memoria heap en generaciones:

        - Young Generation:
            * Donde nacen los objetos.
            * Se divide en Eden y Survivor Spaces.
            * Se limpia frecuentemente → "Minor GC".
            * Objetos que sobreviven varias limpiezas pasan a la Old Gen.

        - Old Generation (Tenured):
            * Contiene objetos de larga vida.
            * Se limpia con "Major GC" (más costoso).

        - Metaspace (antes PermGen):
            * Contiene la metadata de las clases cargadas por la JVM.

        Proceso general:
            1. Se crean objetos en Eden.
            2. Los que sobreviven pasan a Survivor Spaces.
            3. Después de cierto número de ciclos, pasan a Old Gen.
            4. El GC elimina los objetos que ya no son accesibles.
    */


    /*                            VENTAJAS                                 */

    /*
        - Automatiza la gestión de memoria (menos fugas y errores).
        - Permite al desarrollador centrarse en la lógica del programa.
        - Algoritmos avanzados para minimizar pausas.
    */


    /*                            LIMITACIONES                             */

    /*
        - No hay control exacto de cuándo se ejecuta.
        - Las pausas pueden afectar el rendimiento en apps críticas.
        - No limpia recursos externos (archivos, sockets, conexiones DB).
          → Para eso usamos try-with-resources o cerrar manualmente.
    */


    /*                            TUNING BÁSICO                            */

    /*
        - Se puede influir en el comportamiento del GC con flags de la JVM:
            * -Xms / -Xmx : tamaño inicial y máximo del heap.
            * -XX:+UseG1GC : usar Garbage Collector G1.
            * -XX:+UseZGC  : usar Z Garbage Collector (bajas pausas).
            * -verbose:gc  : log de actividad del GC.

        - Herramientas de monitoreo:
            * jconsole, jvisualvm, jstat, Mission Control.
    */


    /*                            EJEMPLOS                                 */

    /*
        System.gc(); // Sugiere al JVM que ejecute el GC (no garantiza nada)

        Runtime rt = Runtime.getRuntime();
        System.out.println("Memoria total: " + rt.totalMemory());
        System.out.println("Memoria libre: " + rt.freeMemory());
        System.out.println("Memoria usada: " + (rt.totalMemory() - rt.freeMemory()));
    */
}
