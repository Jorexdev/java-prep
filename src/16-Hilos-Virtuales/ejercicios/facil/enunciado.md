# Ejercicios — 16 Virtual Threads (Java 21)
## Fácil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Primer virtual thread**
Crea un virtual thread con `Thread.ofVirtual().start(...)` que imprima su nombre, `isVirtual()` e id.
Crea también un platform thread con `Thread.ofPlatform().start(...)` con los mismos datos.
Espera a que ambos terminen y muestra las diferencias en una comparativa.

---

**Ejercicio 2 — Virtual thread factory**
Usa `Thread.ofVirtual().name("vt-", 0).factory()` para obtener una `ThreadFactory`.
Crea 10 threads con esa factory.
Cada thread imprime su nombre, si es virtual y su id.
Espera a que todos terminen.

---

**Ejercicio 3 — I/O simulado**
Lanza 1000 virtual threads, cada uno hace `Thread.sleep(50)` (simula I/O de 50ms).
Mide el tiempo total desde el lanzamiento hasta que termina el último.
Imprime: número de threads, sleep por thread, tiempo total real.
Demuestra que el tiempo total es ~50ms, no 50 segundos.

---

**Ejercicio 4 — ExecutorService virtual**
Crea dos ExecutorService: `Executors.newVirtualThreadPerTaskExecutor()` y `Executors.newFixedThreadPool(10)`.
Envía 200 tareas a cada uno; cada tarea hace `Thread.sleep(10)`.
Mide el tiempo total de cada executor.
Imprime una comparativa: tiempo con virtual threads vs fixed pool de 10.

---

**Ejercicio 5 — Thread states**
Crea un thread (platform o virtual) y observa sus estados: `NEW`, `RUNNABLE`, `TIMED_WAITING`, `TERMINATED`.
Antes de llamar `start()`: imprime el estado (`NEW`).
Justo después de `start()` y antes del sleep interno: imprime el estado (`RUNNABLE`).
Durante el `sleep` del thread: imprime el estado (`TIMED_WAITING`).
Tras `join()`: imprime el estado (`TERMINATED`).

---

**Ejercicio 6 — Nombres y metadatos**
Crea 5 virtual threads con nombres descriptivos (`"worker-0"` hasta `"worker-4"`).
Cada thread imprime: su nombre (`Thread.currentThread().getName()`), si es daemon, y si es virtual.
Espera a que todos terminen y verifica que todos son virtuales.
