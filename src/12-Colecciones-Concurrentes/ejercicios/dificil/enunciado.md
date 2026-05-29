# Ejercicios — 12 Colecciones Concurrentes
## Difícil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — LRU cache thread-safe**
Implementa una `LRUCache<K,V>` con capacidad máxima configurable.
Usa `LinkedHashMap(capacity, 0.75f, true)` internamente (access-order=true) como estructura base.
Protégela con `ReentrantReadWriteLock`.
8 threads hacen operaciones `get` y `put` simultáneas.
Verifica que el tamaño nunca supera el máximo y que el evict ocurre correctamente.
Imprime: operaciones totales, hits, misses, evictions, tamaño final.

---

**Ejercicio 2 — Lock striping**
Implementa `StripedMap<K,V>` con 8 buckets de `HashMap` internos, cada uno con su propio `ReentrantLock`.
`put(key, value)` y `get(key)` solo bloquean el bucket correspondiente: `bucket = Math.abs(key.hashCode()) % 8`.
Compara el throughput contra un mapa con un único lock global:
8 threads hacen 10000 operaciones mixtas (70% get, 30% put).
Imprime operaciones por ms para ambas implementaciones.

---

**Ejercicio 3 — LongAdder vs AtomicLong**
8 threads incrementan un contador 100.000 veces cada uno (800.000 total).
Mide el tiempo con `AtomicLong` y con `LongAdder`.
Ejecuta 3 rondas de cada uno para tener una media.
Imprime una tabla con los tiempos y el speedup de LongAdder.
Explica en un comentario por qué LongAdder es más rápido bajo alta contención.

---

**Ejercicio 4 — Work stealing**
Implementa un work-stealing scheduler con 4 workers.
Cada worker tiene su propio `ArrayDeque<Runnable>`.
Cuando un worker termina su deque, busca el worker con más tareas y roba la mitad.
Distribuye 40 tareas inicialmente (10 por worker), algunas muy rápidas y otras lentas (sleep variable).
Imprime: qué worker ejecutó cada tarea, cuántos robos ocurrieron, tiempo total.

---

**Ejercicio 5 — Phaser multi-fase para pipeline paralelo**
Implementa un pipeline de procesamiento con `Phaser` de 3 fases: carga, transformación y almacenamiento.
4 workers procesan 12 ítems en paralelo (3 por worker).
Usa el `Phaser` para sincronizar el avance de fase: ningún worker puede entrar en transformación hasta que todos hayan completado la carga; idem para almacenamiento.
En la fase de transformación, el worker-0 abandona el Phaser (`arriveAndDeregister`) simulando un fallo parcial.
Imprime: fase actual, worker que avanza, tiempo de cada fase y tiempo total.
Usa `phaser.getPhase()` para verificar el número de fase en cada sincronización.
