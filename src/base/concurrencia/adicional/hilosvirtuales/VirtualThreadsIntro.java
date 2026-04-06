package base.concurrencia.adicional.hilosvirtuales;

/*
    VIRTUAL THREADS - Introducción (Java 21+)

    ¿Qué es?
    Hilos ligeros gestionados por la JVM, no mapeados 1:1 a hilos del sistema operativo.
    Permiten crear miles o millones de hilos sin el coste de los hilos de plataforma.

    ¿Para qué sirve?
    Para escalar aplicaciones con muchas tareas I/O-bound (HTTP, bases de datos, archivos)
    sin necesidad de programación reactiva o callbacks.
    Escribes código bloqueante normal y la JVM lo gestiona de forma eficiente.

    ¿Cuándo usarlo?
    - Aplicaciones con muchas operaciones I/O concurrentes (servidores, microservicios).
    - Cuando quieres simplicidad de código bloqueante con escalabilidad de reactivo.
    - Reemplazar grandes pools de hilos en aplicaciones I/O-bound.

    ¿Cuándo NO usarlo?
    - Tareas CPU-bound intensivas: aquí los hilos de plataforma y ForkJoinPool siguen siendo mejores.
    - Si usas synchronized en secciones que bloquean I/O: puede causar "pinning" (el carrier thread se bloquea).

    API:
    - Thread.ofVirtual().start(runnable)
    - Executors.newVirtualThreadPerTaskExecutor()

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre un Virtual Thread y un hilo de plataforma?
    - ¿Qué es el "carrier thread" y qué es el "pinning"?
    - ¿Cuándo NO deberías usar Virtual Threads?
    - ¿Cómo se relacionan los Virtual Threads con el Project Loom?
    - ¿Qué ventaja tienen sobre CompletableFuture para I/O?
*/
