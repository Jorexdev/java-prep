# Ejercicios — Concurrencia (Difícil)

Las soluciones están en [soluciones/](soluciones/).

## Ejercicio 1 — BlockingQueue: productor-consumidor múltiple
Implementa el patrón productor-consumidor con LinkedBlockingQueue. 2 productores generan ítems cada 50ms, 3 consumidores los procesan. Usa poison pill para detener los consumers elegantemente.

## Ejercicio 2 — Algoritmo con acceso concurrente: tabla de frecuencias
Construye una tabla de frecuencias de palabras procesando 1000 strings en paralelo. Usa ConcurrentHashMap con merge() o compute() para garantizar atomicidad. Compara con HashMap en un stream paralelo.

## Ejercicio 3 — Barrera de fases con Phaser
Simula un pipeline de procesamiento con 3 fases (carga, transformación, almacenamiento). Usa Phaser en lugar de CyclicBarrier para poder variar el número de participantes entre fases.

## Ejercicio 4 — ThreadLocal: contexto por hilo
Implementa un sistema de logging con ThreadLocal<String> para almacenar el requestId de cada hilo. Demuestra que cada hilo ve su propio valor y que se limpia al final de la request.

---

## Ejercicio 5 — ReadWriteLock propio con wait/notifyAll

Implementa `MiReadWriteLock` usando solo `synchronized`, `wait()` y `notifyAll()`.
Campos internos: `lectoresActivos` (int), `escritorActivo` (boolean), `escritoresEsperando` (int).
`lockRead()`: espera si hay escritor activo o escritores esperando; luego incrementa `lectoresActivos`.
`unlockRead()`: decrementa `lectoresActivos`; si llega a 0, `notifyAll()`.
`lockWrite()`: incrementa `escritoresEsperando`; espera si hay lectores activos o escritor activo; decrementa `escritoresEsperando` y activa `escritorActivo`.
`unlockWrite()`: desactiva `escritorActivo` y llama `notifyAll()`.
Demo: `DataStore` protegido con `MiReadWriteLock`; 6 reader threads y 2 writer threads durante 500ms.
Imprime operaciones de cada tipo y verifica que nunca hay lectores y escritor simultáneos.
