# Ejercicios — Concurrencia Asíncrona (Medio)

Las soluciones están en [soluciones/](soluciones/).

## Ejercicio 1 — CompletableFuture.allOf + anyOf
allOf: lanza 4 llamadas HTTP simuladas y espera a que TODAS terminen. anyOf: lanza 3 cachés con distintas latencias y usa el primer resultado disponible.

## Ejercicio 2 — CompletableFuture.thenCombine
Realiza dos consultas independientes en paralelo (obtenerPrecio y obtenerDescuento) y combina sus resultados con thenCombine para calcular el precio final.

## Ejercicio 3 — ScheduledExecutorService
Programa 3 tipos de tareas: una que se ejecuta una vez tras 500ms, una que se repite cada 200ms (5 veces), y una que tiene un delay inicial de 100ms y periodo de 300ms.

## Ejercicio 4 — CompletableFuture con timeout (Java 9+)
Implementa un servicio que puede tardar más de lo esperado. Usa orTimeout() y completeOnTimeout() para manejar el caso en que la llamada supera 300ms.

## Ejercicio 5 — Pipeline asíncrono con manejo de errores
Implementa un pipeline de 4 etapas: validar → enriquecer → persistir → notificar. Cada etapa puede fallar. Usa handle() para recuperarte de errores intermedios sin cortar el pipeline.

## Ejercicio 6 — ForkJoinPool y RecursiveTask
Implementa suma de un array de enteros usando ForkJoinPool y RecursiveTask: divide el array si tiene más de 1000 elementos, suma directamente si es pequeño.
