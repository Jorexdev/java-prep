# Colecciones Lista — Ejercicios Difícil

Ejercicios avanzados: LRU Cache, inversión in-place, power set, binary search manual, rendimiento ArrayList vs LinkedList, particionado.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — LRU Cache
Implementa una LRU Cache (Least Recently Used) de capacidad N usando `LinkedHashMap` con `removeEldestEntry` sobreescrito. Métodos: `get(K key)` y `put(K key, V value)`. Demuestra que al superar la capacidad se elimina el elemento menos recientemente usado.

## Ejercicio 2 — Invertir lista in-place
Invierte una `List<Integer>` in-place usando dos punteros (sin colecciones auxiliares ni Collections.reverse()). Complejidad O(n).

## Ejercicio 3 — Power set
Dado un `List<Integer>` de tamaño pequeño (≤ 5), genera todos sus subconjuntos posibles (power set). Para [1,2,3] el resultado tiene 8 subconjuntos incluyendo el vacío.

## Ejercicio 4 — Binary search manual
Implementa `binarySearch(List<Empleado> lista, int salarioBuscado)` que busque el índice de un empleado por salario en una lista ordenada, sin usar Collections.binarySearch(). Retorna -1 si no existe.

## Ejercicio 5 — Rendimiento ArrayList vs LinkedList
Mide con `System.nanoTime()` el tiempo de insertar 10.000 elementos al inicio (índice 0) de un ArrayList vs un LinkedList. Muestra los tiempos. (Esperado: LinkedList mucho más rápido para inserciones al inicio.)

## Ejercicio 6 — Particionado en sublistas de N
Implementa `<T> List<List<T>> particionar(List<T> lista, int n)` que divida la lista en sublistas de tamaño máximo n. La última puede ser más pequeña si no es divisible exactamente.
