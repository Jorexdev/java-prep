# Ejercicios — 16 Virtual Threads (Java 21)
## Medio
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — I/O-bound benchmark**
100 tareas con `Thread.sleep(20ms)` cada una.
Mide el tiempo con:
- `Executors.newFixedThreadPool(10)`
- `Executors.newVirtualThreadPerTaskExecutor()`
Muestra una tabla comparativa con: número de threads del pool, tiempo total, throughput (tareas/seg).
El speedup esperado de virtual threads es ~10x sobre fixed(10).

---

**Ejercicio 2 — CPU-bound demo**
4 tareas que calculan primos hasta 100.000 (operación CPU-intensiva sin I/O).
Mide el tiempo con:
- 4 platform threads
- 4 virtual threads
Demuestra con los tiempos que los virtual threads NO mejoran el rendimiento en CPU-bound.
Añade un comentario explicando por qué.

---

**Ejercicio 3 — Pinning**
Crea 50 threads que ejecutan un bloque `synchronized` con `Thread.sleep(20)` dentro.
Con virtual threads, el sleep dentro de `synchronized` hace pinning del carrier thread.
Repite con `ReentrantLock` (sin pinning).
Compara el tiempo total de ambos enfoques con 50 threads concurrentes.
Imprime el speedup de usar `ReentrantLock` vs `synchronized`.

---

**Ejercicio 4 — StructuredTaskScope.ShutdownOnFailure**
Lanza 3 subtareas paralelas con `StructuredTaskScope.ShutdownOnFailure`.
La subtarea 2 lanza una excepción `RuntimeException("tarea-2 fallo")`.
Llama `scope.throwIfFailed()` para propagar la excepción.
Captura la excepción en el main y muestra qué tarea falló.
Demuestra que las otras subtareas fueron canceladas.

---

**Ejercicio 5 — Scoped Values**
Declara `ScopedValue<String> USER_ID = ScopedValue.newInstance()`.
En el main, ejecuta con `ScopedValue.where(USER_ID, "user-42").run(...)`.
Dentro del run, llama a 3 métodos anidados: `nivel1()` → `nivel2()` → `nivel3()`.
Cada nivel imprime el `USER_ID.get()` sin recibirlo como parámetro.
Demuestra que fuera del `run` el valor no está disponible (lanza `NoSuchElementException`).
