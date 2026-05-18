# Colecciones Utilidades — Ejercicios Medio

Collections avanzado, binarySearch, Comparator compuesto, iteradores personalizados.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Collections.sort con Comparator externo

Crea una lista de `Factura(id, cliente, importe)`. Ordénala de tres formas distintas
usando Comparator externo: por importe ascendente, por cliente alfabético, por id descendente.

## Ejercicio 2 — Collections.binarySearch

Ordena una lista de enteros con Collections.sort y luego busca un elemento con Collections.binarySearch.
Demuestra que binarySearch devuelve índice negativo si el elemento no existe (fórmula de inserción).

## Ejercicio 3 — nCopies, frequency, disjoint

Usa Collections.nCopies(5, "x") para crear una lista. Demuestra Collections.frequency() y
Collections.disjoint() con dos listas: una con elementos comunes y otra sin.

## Ejercicio 4 — Iterable personalizado

Implementa `Rango` que implementa `Iterable<Integer>` y devuelve números en un rango [inicio, fin].
El iterador debe soportar `hasNext()` y `next()` correctamente.

## Ejercicio 5 — Comparator.reversed y nullsFirst/nullsLast

Ordena una lista de strings que contiene nulls. Usa `Comparator.naturalOrder()` con
`Comparator.nullsFirst()` y `Comparator.nullsLast()`. Luego invierte con `reversed()`.

## Ejercicio 6 — Collections.rotate y fill

Usa Collections.rotate(lista, k) para rotar una lista k posiciones a la derecha.
Usa Collections.fill(lista, valor) para rellenar toda la lista con un valor.
Demuestra el resultado antes y después de cada operación.
