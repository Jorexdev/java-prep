# Lambdas — Ejercicios Fácil

Ejercicios de nivel básico para practicar la sintaxis de lambdas y las interfaces funcionales estándar:
`Runnable`, `Comparator`, `Predicate`, `Function`, `Consumer`, `Supplier`.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Ordenar por longitud

Dada una lista de strings, usa `Collections.sort` con una lambda como `Comparator` para ordenarla por la longitud de cada string (de menor a mayor). Imprime el resultado.

## Ejercicio 2 — Predicate de mayúscula inicial

Crea un `Predicate<String>` que devuelva `true` si el string empieza por letra mayúscula. Pruébalo con al menos 4 strings distintos imprimiendo el resultado de cada evaluación.

## Ejercicio 3 — Consumer con longitud

Crea un `Consumer<String>` que, dado un string, lo imprima junto con su longitud en el formato `"palabra -> 7"`. Aplícalo a una lista de palabras con `forEach`.

## Ejercicio 4 — Runnable con lambda

Crea un `Runnable` usando una lambda que imprima el mensaje `"Hola desde un Runnable"`. Ejecútalo llamando a `run()`.

## Ejercicio 5 — Supplier de lista

Crea un `Supplier<List<String>>` que retorne una lista predefinida de tres nombres. Llámalo dos veces e imprime ambas listas para demostrar que cada llamada produce el valor.

## Ejercicio 6 — Function de String a entero

Crea un `Function<String, Integer>` que convierta un string a su longitud. Aplícala a una lista de palabras con `stream().map()` y recoge los resultados en una lista de enteros.

## Ejercicio 7 — Filtrar pares con Predicate

Crea un `Predicate<Integer>` que evalúe si un número es par. Úsalo en un `stream().filter()` sobre una lista de enteros del 1 al 10 para imprimir solo los pares.

## Ejercicio 8 — Reemplazar clase anónima por lambda

Tienes un `Comparator` implementado como clase anónima que ordena `Empleado` por nombre alfabéticamente. Reescríbelo usando una lambda y ordena la lista con `Collections.sort`. Incluye ambas versiones (comentada la anónima, activa la lambda) para que se vea la equivalencia.
