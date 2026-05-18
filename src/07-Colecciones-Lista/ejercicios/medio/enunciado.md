# Colecciones Lista — Ejercicios Medio

Ordenación con Comparator, eliminar duplicados, LinkedList como pila/cola, fusión de listas, ListIterator.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Ordenar objetos con Comparator
Dada una lista de `Empleado(nombre, salario)`, ordénala por salario descendente usando Comparator.

## Ejercicio 2 — Eliminar duplicados conservando orden
Implementa un método que elimine duplicados de una `List<String>` conservando el orden de la primera aparición, sin usar Set directamente en la solución (usa un Set auxiliar para rastrear vistos).

## Ejercicio 3 — LinkedList como pila y como cola
Demuestra el uso de LinkedList como stack (push/pop/peek) y como queue (offer/poll/peek). Muestra el orden LIFO vs FIFO con los mismos 5 elementos.

## Ejercicio 4 — Rotación de lista
Implementa `rotar(List<Integer> lista, int n)` que mueva los últimos n elementos al principio. Ejemplo: [1,2,3,4,5] rotada 2 → [4,5,1,2,3].

## Ejercicio 5 — Segundo elemento más grande
Dado un List<Integer>, encuentra el segundo elemento más grande sin ordenar la lista. Lanza excepción si la lista tiene menos de 2 elementos distintos.

## Ejercicio 6 — Collections.frequency y disjoint
Usa `Collections.frequency()` para contar ocurrencias de un elemento y `Collections.disjoint()` para verificar si dos listas no tienen elementos comunes. Demuestra ambos con ejemplos concretos.

## Ejercicio 7 — Fusionar dos listas ordenadas
Implementa `fusionar(List<Integer> a, List<Integer> b)` que una dos listas ya ordenadas en una sola lista ordenada sin usar Collections.sort() en el resultado.

## Ejercicio 8 — ListIterator para reemplazar en-place
Usa `ListIterator` para recorrer una `List<String>` y reemplazar cada elemento por su versión en mayúsculas, sin crear una lista nueva.
