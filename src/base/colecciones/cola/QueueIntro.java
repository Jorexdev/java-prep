package base.colecciones.cola;

/*
    QUEUE Y DEQUE - Introducción

    ¿Qué es?
    Queue: colección diseñada para mantener elementos en orden para su procesamiento.
    Por defecto FIFO (First-In, First-Out), aunque PriorityQueue rompe este orden.

    Deque (Double Ended Queue): extiende Queue con operaciones en ambos extremos.
    Puede usarse como cola (FIFO) o como pila (LIFO).

    ¿Para qué sirve?
    Para modelar flujos de trabajo donde el orden de procesamiento importa:
    colas de tareas, algoritmos BFS, pilas de llamadas, productores-consumidores.

    Operaciones clave (Queue):

    Lanza excepción si falla    |    Devuelve null/false si falla
    add(e)                      |    offer(e)        - insertar
    remove()                    |    poll()           - extraer
    element()                   |    peek()           - consultar sin extraer

    Implementaciones principales:

    PriorityQueue
    - Basada en heap binario.
    - Los elementos se ordenan por orden natural o Comparator.
    - No garantiza FIFO: siempre extrae el elemento de mayor prioridad.
    - Operaciones: O(log n) para inserción/extracción, O(1) para peek.

    ArrayDeque
    - Implementación de Deque basada en array circular.
    - Más rápida que LinkedList como cola o pila.
    - No permite nulls.
    - Recomendada como pila en lugar de Stack (legacy).

    ConcurrentLinkedQueue
    - Cola concurrente no bloqueante (lock-free).
    - Basada en nodos enlazados con punteros atómicos.
    - Thread-safe sin bloqueos.

    BlockingQueue
    - Subinterfaz con operaciones bloqueantes: put() bloquea si está llena, take() si está vacía.
    - Base del patrón productor-consumidor.
    - Implementaciones: ArrayBlockingQueue, LinkedBlockingQueue, PriorityBlockingQueue.

    Preguntas típicas de entrevista:
    - ¿Cuándo usarías PriorityQueue?
    - ¿Qué diferencia hay entre ArrayDeque y Stack?
    - ¿Cómo implementarías un productor-consumidor con BlockingQueue?
    - ¿Qué diferencia hay entre poll() y remove()?
*/
