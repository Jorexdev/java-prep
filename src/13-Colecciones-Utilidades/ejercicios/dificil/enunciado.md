# Colecciones Utilidades — Ejercicios Difícil

Iteradores genéricos, Comparable/Comparator avanzado, colecciones con orden personalizado.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — SortedList genérica

Implementa `ListaOrdenada<T>` que recibe un `Comparator<T>` en el constructor y mantiene
los elementos siempre ordenados al insertar. Soporta `add(T)`, `get(int)`, `size()`, `toList()`.

## Ejercicio 2 — FilterIterator genérico

Implementa `IteradorFiltro<T>` que envuelve un `Iterator<T>` y un `Predicate<T>`.
Solo avanza a elementos que cumplen el predicado. Implementa `hasNext()` y `next()`.
Demuestra con una lista de enteros filtrando solo los pares.

## Ejercicio 3 — Ranking de empleados con múltiples criterios

Crea `Empleado(nombre, departamento, salario, antiguedad)` que implementa `Comparable<Empleado>`.
El orden natural es: primero por salario descendente, luego por antigüedad descendente, luego por nombre.
Proporciona también tres `Comparator` estáticos: por departamento, por nombre, por salario.
Ordena la misma lista con cada criterio y muestra los resultados.

## Ejercicio 4 — Fibonacci lazy Iterator

Implementa `IteradorFibonacci` que implementa `Iterator<Long>` y genera la secuencia de Fibonacci
de forma lazy (sin precomputar). `hasNext()` devuelve siempre true (secuencia infinita).
Usa `limit()` de Stream para tomar los primeros N términos.
Envuelve el iterador en un `Spliterator` con `StreamSupport.stream()` para integrarlo con streams.

---

## Ejercicio 5 — Comparator chain con tiebreakers y sort estable

Crea `Producto(nombre, categoria, precio, stock)` sin orden natural.
Construye un `Comparator<Producto>` encadenado con `thenComparing`: primero por categoría ascendente, luego por precio descendente, luego por nombre ascendente como tiebreaker final.
Genera una lista de 12 productos con nombres deliberadamente iguales en distintas categorías para forzar todos los niveles del tiebreaker.
Ordena con `Collections.sort()` (sort estable) y con `List.sort()`.
Demuestra estabilidad: dos productos con categoría, precio y nombre idénticos mantienen su orden relativo original.
Imprime la lista ordenada, indicando en cada ítem qué criterio fue determinante.
