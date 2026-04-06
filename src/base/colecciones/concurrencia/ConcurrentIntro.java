package base.colecciones.concurrencia;

/*
    COLECCIONES CONCURRENTES - Introducción

    ¿Qué son?
    Implementaciones de colecciones diseñadas para entornos multihilo.
    A diferencia de Collections.synchronizedX(), están optimizadas para concurrencia:
    evitan bloqueos globales usando segmentación, locks finos o algoritmos lock-free.

    ¿Por qué no usar Collections.synchronizedList/Map?
    - Sincronizan en el objeto completo: un hilo bloqueando bloquea a todos.
    - No son seguras para iteración concurrente (hay que sincronizar manualmente).
    - Las colecciones concurrentes del paquete java.util.concurrent son más eficientes.

    Implementaciones principales:

    ConcurrentHashMap
    - Mapa concurrente de alto rendimiento.
    - Divide el acceso en segmentos (Java 7) o usa CAS (Java 8+).
    - Múltiples hilos pueden leer y escribir simultáneamente en distintos segmentos.
    - No permite claves ni valores null.
    - La alternativa a Collections.synchronizedMap() o Hashtable.

    CopyOnWriteArrayList
    - Lista segura para lectura concurrente.
    - Cada escritura (add, remove, set) crea una nueva copia del array subyacente.
    - Las lecturas son lock-free y nunca lanzan ConcurrentModificationException.
    - Ideal cuando hay muchas más lecturas que escrituras.

    ConcurrentLinkedQueue
    - Cola concurrente y no bloqueante (lock-free).
    - Basada en nodos enlazados con operaciones CAS atómicas.
    - Alta concurrencia sin bloqueos.

    BlockingQueue
    - Cola con operaciones bloqueantes: put() espera si está llena, take() si está vacía.
    - Base del patrón productor-consumidor sin wait/notify manual.
    - Implementaciones: ArrayBlockingQueue (tamaño fijo), LinkedBlockingQueue (opcional).

    Preguntas típicas de entrevista:
    - ¿Cuándo usarías ConcurrentHashMap vs Collections.synchronizedMap()?
    - ¿Por qué CopyOnWriteArrayList es eficiente para lecturas pero costosa para escrituras?
    - ¿Qué diferencia hay entre ConcurrentLinkedQueue y LinkedBlockingQueue?
    - ¿Cómo implementarías productor-consumidor con BlockingQueue?
*/
