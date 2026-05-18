# Colecciones Utilidades — Ejercicios Fácil

Collections utility methods, Comparable, Comparator básico, Iterator manual.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Collections: min, max, frequency

Usa Collections.min(), max() y frequency() sobre una lista de enteros. Muestra los resultados.

## Ejercicio 2 — Collections.shuffle y swap

Usa Collections.shuffle() para mezclar aleatoriamente una lista y Collections.swap() para intercambiar dos posiciones.

## Ejercicio 3 — Comparable en Producto

Crea clase Producto(nombre, precio) que implementa Comparable<Producto> (comparar por precio). Usa Collections.sort() y Collections.min() con la lista.

## Ejercicio 4 — Comparator.comparing básico

Crea Comparator.comparing(Persona::getEdad) para ordenar una lista de Persona por edad. Usa List.sort().

## Ejercicio 5 — thenComparing encadenado

Crea comparador que ordene Empleado: primero por departamento alfabético, luego por nombre.

## Ejercicio 6 — Collections.unmodifiableList

Envuelve una lista en unmodifiableList. Intenta añadir un elemento y captura UnsupportedOperationException.

## Ejercicio 7 — Iterator manual con remove

Usa Iterator manualmente para recorrer una lista y eliminar todos los strings que empiecen por "a", de forma segura (sin ConcurrentModificationException).
