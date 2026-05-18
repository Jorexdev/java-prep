# Ejercicios — Concurrencia Asíncrona (Difícil)

Las soluciones están en [soluciones/](soluciones/).

## Ejercicio 1 — Pool personalizado con CompletableFuture
Crea un ExecutorService con Executors.newFixedThreadPool(4) y úsalo como executor en todas las llamadas CompletableFuture.supplyAsync(task, executor). Implementa un circuit breaker simple que bloquea nuevas tareas si hay más de 3 fallos seguidos.

## Ejercicio 2 — CompletableFuture: retry con backoff exponencial
Implementa un método `conRetry(Supplier<CompletableFuture<T>>, int maxIntentos)` que reintenta la operación con backoff exponencial (100ms, 200ms, 400ms...) si falla. Demuestra con un servicio que falla las 2 primeras veces.

## Ejercicio 3 — ForkJoinPool: MergeSort paralelo
Implementa MergeSort usando RecursiveAction (no RecursiveTask — es void). Divide arrays > 2000 elementos, ordena subarrays en paralelo con fork/join, fusiona secuencialmente. Compara con Arrays.sort().

## Ejercicio 4 — Semáforo de rate limiting asíncrono
Implementa un rate limiter basado en Semaphore que permite máximo N requests por segundo. Usa ScheduledExecutorService para reponer permisos cada segundo. Lanza 20 tareas y muestra que nunca se procesan más de N por segundo.
