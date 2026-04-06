package base.concurrencia;

/*
    CONCURRENCIA - Introducción

    ¿Qué es?
    La capacidad de ejecutar múltiples tareas de forma simultánea o paralela.
    En Java se basa en hilos (Threads) y en las utilidades del paquete java.util.concurrent.

    ¿Para qué sirve?
    - Aprovechar procesadores multinúcleo (tareas CPU-bound).
    - No bloquear la aplicación esperando I/O (tareas I/O-bound).
    - Mejorar la capacidad de respuesta en servidores y aplicaciones.

    Herramientas disponibles (de menor a mayor nivel de abstracción):

    synchronized
        Exclusión mutua básica. Bloquea el acceso a una sección crítica.

    volatile
        Garantiza visibilidad entre hilos. No da exclusión mutua.

    ReentrantLock
        Lock explícito con más control: tryLock, timeouts, condiciones.

    Semaphore
        Limita el número de hilos que acceden a un recurso simultáneamente.

    wait / notify
        Coordinación entre hilos usando el monitor de un objeto.

    ExecutorService
        Pool de hilos. Gestiona la creación y ciclo de vida de los threads.

    CompletableFuture
        Composición asíncrona con pipelines: thenApply, thenCompose, allOf...

    Virtual Threads (Java 21+)
        Hilos ligeros gestionados por la JVM. Ideales para I/O a gran escala.

    Problemas comunes en concurrencia:

    Race condition - dos hilos modifican un recurso compartido sin sincronización.
    Deadlock - dos hilos esperan indefinidamente por recursos que el otro tiene bloqueados.
    Starvation - un hilo nunca consigue acceso a CPU o a un recurso.
    Visibilidad - cambios en memoria no son visibles a otros hilos sin volatile o sincronización.

    Preguntas típicas de entrevista:
    - ¿Cuándo usas synchronized y cuándo ReentrantLock?
    - ¿Qué diferencia hay entre volatile y synchronized?
    - ¿Cómo evitas un deadlock?
    - ¿Qué es un race condition? Pon un ejemplo.
    - ¿Cuándo usarías CompletableFuture frente a un ExecutorService directo?
    - ¿Qué son los Virtual Threads y en qué se diferencian de los hilos de plataforma?
*/
