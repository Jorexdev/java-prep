# Ejercicios — 12 Colecciones Concurrentes
## Medio
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — BlockingQueue producer-consumer**
Implementa un sistema de tareas con `ArrayBlockingQueue<String>` de capacidad 5.
2 threads productores generan 10 tareas cada uno (20 en total).
3 threads consumidores procesan las tareas con `take()`.
Los productores usan `put()` (se bloquean si la cola está llena).
Imprime cada producción y consumo con el nombre del thread. Espera a que se procesen los 20 items.

---

**Ejercicio 2 — ConcurrentHashMap vs HashMap**
Crea dos mapas: un `HashMap<Integer, Integer>` y un `ConcurrentHashMap<Integer, Integer>`.
4 threads hacen put() simultáneos de 1000 entradas en cada mapa.
Envuelve el acceso a `HashMap` en un try-catch `ConcurrentModificationException`.
Al final, imprime el número de entradas en cada mapa.
El `HashMap` puede tener un tamaño incorrecto o lanzar excepción; el `ConcurrentHashMap` siempre tiene el valor correcto.

---

**Ejercicio 3 — Cache con TTL**
Implementa una `SimpleCache<K,V>` usando `ConcurrentHashMap<K, Entry<V>>`.
`Entry` tiene el valor y el timestamp de inserción.
`get(key, ttlMs)` devuelve null si el entry expiró.
`put(key, value)` inserta con timestamp actual.
`evictExpired(ttlMs)` elimina todas las entradas caducadas.
Demo: insertar 5 entradas, esperar que algunas expiren, llamar `evictExpired`, mostrar estado.

---

**Ejercicio 4 — ReadWriteLock**
Implementa un `DataStore` con `ReentrantReadWriteLock`.
Tiene un `Map<String, String>` interno.
`read(key)` adquiere el read lock (permite lecturas concurrentes).
`write(key, value)` adquiere el write lock (exclusivo).
Lanza 8 reader threads y 2 writer threads simultáneamente.
Imprime qué tipo de operación ejecuta cada thread y el tiempo total.

---

**Ejercicio 5 — ConcurrentSkipListMap leaderboard**
Crea un leaderboard de jugadores con `ConcurrentSkipListMap<Integer, String>` ordenado por score descendente (usa `Comparator.reverseOrder()` como clave).
5 threads actualizan scores de 10 jugadores (valores aleatorios 1-100) usando `put`.
1 thread lee el top-3 continuamente durante 200ms.
Imprime el top-3 cada 50ms mostrando que siempre está ordenado.
