package base.colecciones;

/*
    JAVA COLLECTIONS FRAMEWORK - Introducción

    ¿Qué es?
    Conjunto de clases e interfaces que proveen estructuras de datos listas para usar.
    A diferencia de los arrays, las colecciones se redimensionan dinámicamente
    y ofrecen operaciones de búsqueda, ordenación e iteración de serie.

    ¿Para qué sirve?
    Para elegir la estructura de datos correcta según el caso de uso:
    orden, unicidad, acceso por clave, rendimiento en inserción/búsqueda, thread-safety...

    Interfaces principales:

    List - colección ordenada que permite duplicados y acceso por índice.
        ArrayList, LinkedList, Vector, Stack

    Set - colección que no permite duplicados.
        HashSet, LinkedHashSet, TreeSet, EnumSet

    Queue / Deque - colección diseñada para procesamiento en orden (FIFO/LIFO/prioridad).
        PriorityQueue, ArrayDeque, LinkedList

    Map - pares clave-valor. No hereda de Collection pero forma parte del framework.
        HashMap, LinkedHashMap, TreeMap, WeakHashMap

    Colecciones concurrentes - versiones thread-safe optimizadas.
        ConcurrentHashMap, CopyOnWriteArrayList, BlockingQueue

    Árbol de decisión básico:

    - ¿Necesitas duplicados y orden de inserción?  - List (ArrayList)
    - ¿Necesitas unicidad?                          - Set (HashSet)
    - ¿Necesitas buscar por clave?                  - Map (HashMap)
    - ¿Necesitas procesar en orden/prioridad?       - Queue (PriorityQueue)
    - ¿Necesitas orden natural o personalizado?     - TreeSet / TreeMap
    - ¿Acceso concurrente?                          - ConcurrentHashMap / CopyOnWriteArrayList

    Preguntas típicas de entrevista:
    - ¿Cuándo usarías ArrayList vs LinkedList?
    - ¿Cuándo usarías HashMap vs TreeMap vs LinkedHashMap?
    - ¿Qué diferencia hay entre Comparable y Comparator?
    - ¿Por qué HashMap no garantiza orden?
    - ¿Qué pasa si modificas una colección mientras la iteras?
*/
