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

---

## Ejercicio 5 — CompletableFuture pipeline con timeout, retry y circuit breaker

Implementa `AsyncPipeline` con un `ExecutorService` de 4 hilos.
`fetchData(id)`: operación asíncrona que falla aleatoriamente un 40% de las veces; simula latencia de 50-150ms.
`withRetry(supplier, maxAttempts)`: reintenta con backoff exponencial (50ms, 100ms, 200ms) usando `CompletableFuture` encadenados; si agota los intentos propaga la excepción.
`withTimeout(cf, ms)`: devuelve un nuevo `CompletableFuture` que completa con `TimeoutException` si el original tarda más de `ms` milisegundos (usa `orTimeout` de Java 9+).
`CircuitBreaker`: contador de fallos consecutivos; si supera 3, lanza `CircuitOpenException` sin ejecutar la tarea; se resetea al primer éxito.
Demo: lanza 10 llamadas en paralelo pasando por el pipeline completo (retry + timeout + circuit breaker), muestra el estado del circuit breaker y el resultado de cada llamada.
