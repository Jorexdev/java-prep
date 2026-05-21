# Ejercicios — 17 Recolector de Basura

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Reference Queue**
Crea 5 objetos y envuélvelos cada uno en una `WeakReference` asociada a la misma `ReferenceQueue`. Elimina las referencias fuertes y llama a `System.gc()`. Vacía la queue con `poll()` en un bucle y muestra cuántas referencias fueron encoladas. Explica qué significa que una referencia aparezca en la queue.

---

**Ejercicio 2 — WeakHashMap**
Crea un `WeakHashMap<String, String>` donde las claves son objetos `String` creados explícitamente con `new String(...)` (no internados). Añade 5 entradas. Elimina la referencia fuerte a 3 claves y llama a `System.gc()`. Imprime el mapa antes y después. Observa cómo las entradas desaparecen automáticamente cuando la clave no tiene más referencias.

---

**Ejercicio 3 — Heap pressure simulation**
En un bucle, crea arrays de 512KB y acumúlalos en una lista. Después de cada iteración imprime la memoria libre con `Runtime.getRuntime().freeMemory()`. Detecta cuándo la memoria libre cae por debajo del 20% del total. Cuando ocurra, llama a `System.gc()`, espera, e imprime si la memoria se recuperó. Para el bucle si la memoria libre se mantiene bajo el 20% tras el GC.

---

**Ejercicio 4 — Memory leak detection**
Implementa dos versiones de un cache que mapea claves `String` a valores `String`:
- `HashMapCache`: usa `HashMap` (puede causar leak porque retiene las claves)
- `WeakMapCache`: usa `WeakHashMap` (no retiene las claves)

Simula 100 accesos donde las claves se crean con `new String(...)` y se descartan. Después de cada 20 accesos llama a `System.gc()` y muestra el tamaño de cada cache. El HashMap debe crecer; el WeakHashMap debe reducirse.

---

**Ejercicio 5 — Object pool**
Implementa un `ObjectPool<T>` genérico que mantiene una lista de objetos disponibles. Los métodos son `acquire()` (devuelve un objeto del pool o crea uno nuevo) y `release(T obj)` (devuelve el objeto al pool). Mide en nanosegundos el tiempo promedio de 10000 operaciones acquire+release vs 10000 operaciones new+descarte. Imprime la diferencia de rendimiento.
