# Genéricos — Ejercicios Fácil

Ejercicios de nivel básico para practicar clases genéricas, métodos genéricos,
wildcards sin acotación y bounded type parameters sencillos.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Clase genérica Caja

Crea una clase genérica `Caja<T>` con un campo privado `valor`, un constructor, y métodos `get()` y `set()`.
En el `main`, crea una `Caja<String>` y una `Caja<Integer>`, asígnales valores e imprímelos.

## Ejercicio 2 — Método genérico primero

Crea un método estático genérico `<T> T primero(List<T> lista)` que retorne el primer elemento
de la lista. Pruébalo con una `List<String>` y una `List<Integer>`.

## Ejercicio 3 — Método genérico imprimirTodos

Crea un método estático genérico `<T> void imprimirTodos(List<T> lista)` que recorra la lista
e imprima cada elemento. Úsalo con listas de tipos distintos (String, Double).

## Ejercicio 4 — Clase Par con dos tipos

Crea una clase genérica `Par<A, B>` que almacene dos valores de tipos distintos.
Incluye un método `toString()` que muestre ambos. Usa `Par<String, Integer>` y `Par<Double, Boolean>` en el main.

## Ejercicio 5 — Máximo con bounded type parameter

Crea un método estático `<T extends Comparable<T>> T maximo(T a, T b)` que retorne el mayor.
Pruébalo con enteros y con strings (comparación lexicográfica).

## Ejercicio 6 — Wildcard sin acotación

Crea un método `void imprimirLista(List<?> lista)` que imprima todos los elementos.
Demuestra que acepta `List<String>`, `List<Integer>` y `List<Double>` sin necesidad de sobrecargar.

## Ejercicio 7 — Pila genérica

Crea una clase `Pila<T>` con métodos `push(T elemento)`, `pop()`, `peek()` e `isEmpty()`,
implementados internamente con un `ArrayList<T>`. Demuestra su uso con una pila de Strings.
