# Ejercicios — 16 Virtual Threads (Java 21)
## Difícil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — 10000 virtual threads**
Simula un servidor con 10000 peticiones concurrentes.
Cada petición es un virtual thread que hace `Thread.sleep(100)` (simula I/O de red).
Mide el tiempo total y calcula el throughput en peticiones/segundo.
Asegúrate de que no hay `OutOfMemoryError` y que todos los threads completan.
Imprime: threads lanzados, tiempo, throughput, memoria heap usada antes/después.

---

**Ejercicio 2 — Semaphore throttling**
Envía 10000 virtual threads pero limita la concurrencia activa a 100 con un `Semaphore(100)`.
Cada thread adquiere el semáforo antes de hacer su trabajo (sleep 5ms) y lo libera al terminar.
Monitoriza el máximo de threads activos simultáneamente (debe ser <= 100).
Imprime: max activos observados, tiempo total, throughput.

---

**Ejercicio 3 — Virtual vs platform comparison**
Benchmark con 4 configuraciones. 500 tareas × 50ms I/O simulado:
- `newFixedThreadPool(10)`
- `newFixedThreadPool(100)`
- `newCachedThreadPool()`
- `newVirtualThreadPerTaskExecutor()`
Muestra una tabla con: nombre, tiempo, threads creados (estimado), throughput.
Incluye un análisis de cuándo usar cada uno.

---

**Ejercicio 4 — Structured concurrency tree**
Implementa una jerarquía de tareas con `StructuredTaskScope`:
- Raíz lanza 3 hijos (A, B, C) en paralelo con `ShutdownOnFailure`.
- Cada hijo lanza 2 nietos en paralelo con otro `ShutdownOnFailure` anidado.
- El nieto B2 lanza una `RuntimeException` tras 50ms.
- Demuestra que: B2 cancela B y sus otros nietos, B falla y cancela A y C desde la raíz.
- Imprime el árbol de ejecución con el estado final de cada nodo.

---

**Ejercicio 5 — Connection pool con VTs y Semaphore**
Implementa un `ConnectionPool` que gestiona un pool de 3 conexiones simuladas a BD. Usa un `Semaphore(3, fair=true)` para controlar el acceso y una `LinkedBlockingQueue` para las conexiones disponibles. `acquire()` bloquea el VT si no hay conexiones libres; `release(conn)` la devuelve al pool. Lanza 15 Virtual Threads cliente concurrentes, cada uno adquiere, ejecuta una query (sleep 50-100ms) y libera. Muestra: max conexiones prestadas simultáneamente (debe ser ≤ 3), total atendidos, tiempo total y throughput.

---
