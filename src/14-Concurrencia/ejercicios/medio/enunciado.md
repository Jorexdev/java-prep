# Ejercicios — Concurrencia (Medio)

Las soluciones están en [soluciones/](soluciones/).

## Ejercicio 1 — ReentrantLock + Condition
Reimplementa el productor-consumidor del ejercicio fácil usando ReentrantLock con dos Conditions: `noLleno` y `noVacío`. El buffer tiene capacidad para 3 elementos.

## Ejercicio 2 — Semaphore
Controla el acceso a un "pool de conexiones" de 3 conexiones con Semaphore. Lanza 8 hilos que intentan adquirir una conexión, usarla 100ms y liberarla. Demuestra que nunca hay más de 3 conexiones activas simultáneas.

## Ejercicio 3 — CountDownLatch
Simula el arranque de una aplicación: 3 servicios (BD, Cache, Config) se inicializan en paralelo. El servicio principal espera con CountDownLatch(3) hasta que todos estén listos.

## Ejercicio 4 — CyclicBarrier
Simula una carrera de 4 atletas que corren en fases. Todos deben terminar cada fase antes de que empiece la siguiente. Usa CyclicBarrier para sincronizar al final de cada fase.

## Ejercicio 5 — ReadWriteLock
Implementa una caché thread-safe con ReentrantReadWriteLock: múltiples lectores simultáneos, escritores exclusivos. Lanza 5 lectores y 2 escritores concurrentemente y demuestra el comportamiento.

## Ejercicio 6 — Deadlock: detectar y corregir
Implementa un deadlock clásico con dos hilos y dos locks (A y B). Muestra cómo se congela. Corríge con lock ordering (adquirir siempre en el mismo orden) o tryLock con timeout.
