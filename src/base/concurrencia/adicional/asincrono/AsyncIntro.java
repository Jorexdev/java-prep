package base.concurrencia.adicional.asincrono;

/*
    EXECUTORSERVICE Y COMPLETABLEFUTURE - Introducción

    ¿Qué es?
    Abstracciones de alto nivel para manejar concurrencia sin gestionar hilos manualmente.

    ExecutorService
    Pool de hilos reutilizables. Envías tareas (Runnable/Callable) y el pool decide
    qué hilo las ejecuta. Gestiona el ciclo de vida de los hilos por ti.

    Tipos de pool:
    - newFixedThreadPool(n): N hilos fijos, las tareas extra esperan en cola.
    - newCachedThreadPool(): crea hilos según demanda, reutiliza los disponibles.
    - newScheduledThreadPool(n): para tareas periódicas o con delay.
    - newVirtualThreadPerTaskExecutor(): un Virtual Thread por tarea (Java 21+).

    CompletableFuture
    Composición asíncrona con pipelines encadenados.
    Permite expresar dependencias entre tareas de forma declarativa.

    Operaciones principales:
    - supplyAsync / runAsync: ejecuta una tarea asíncrona.
    - thenApply: transforma el resultado (como map en streams).
    - thenAccept: consume el resultado sin devolver nada.
    - thenCompose: encadena otra tarea asíncrona (como flatMap).
    - thenCombine: combina dos futuros independientes.
    - allOf / anyOf: coordina múltiples futuros.
    - exceptionally / handle: gestiona errores en el pipeline.

    ¿Cuándo usar ExecutorService vs CompletableFuture?
    - ExecutorService: cuando gestionas tareas independientes y necesitas control del pool.
    - CompletableFuture: cuando necesitas encadenar o combinar resultados de tareas asíncronas.

    Preguntas típicas de entrevista:
    - ¿Qué diferencia hay entre submit() y execute() en ExecutorService?
    - ¿Qué diferencia hay entre Future.get() y CompletableFuture.join()?
    - ¿Qué pasa si no llamas shutdown() en un ExecutorService?
    - ¿Qué diferencia hay entre thenApply y thenCompose?
    - ¿Cómo manejas errores en un pipeline de CompletableFuture?
*/
