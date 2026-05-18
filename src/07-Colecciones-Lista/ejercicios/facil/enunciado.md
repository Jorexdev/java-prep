# 07 — Colecciones Lista — Ejercicios Fácil

Practica las operaciones fundamentales de `ArrayList` y `List`: añadir, eliminar, buscar, iterar, ordenar y combinar listas. Cada ejercicio usa la API estándar del JDK sin lógica compleja.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Añadir y eliminar elementos

Crea un `ArrayList<String>` con 5 nombres. Elimina el elemento en el índice 2 y luego elimina por valor el nombre "Ana". Imprime la lista en cada paso.

## Ejercicio 2 — contains() e indexOf()

Dado un `ArrayList<String>` con frutas (incluyendo alguna repetida), usa `contains()` para comprobar si "mango" está en la lista e `indexOf()`/`lastIndexOf()` para encontrar la primera y última posición de "pera".

## Ejercicio 3 — Tres formas de iterar

Crea una lista de enteros del 1 al 5. Itera con: (a) for-each, (b) `Iterator`, (c) `ListIterator` recorriendo hacia atrás desde el final.

## Ejercicio 4 — sort() y reverse()

Crea un `ArrayList<Integer>` con valores desordenados. Usa `Collections.sort()` para ordenar ascendente, imprime el resultado. Luego usa `Collections.reverse()` para invertir el orden e imprime de nuevo.

## Ejercicio 5 — subList() y su efecto en la lista original

Crea una lista de 8 enteros. Obtén la sublista del índice 2 al 5 (exclusive) con `subList()`. Llama a `clear()` sobre esa sublista y observa el estado de la lista original. Explica en un comentario por qué ocurre.

## Ejercicio 6 — removeIf() con lambda

Crea una lista de strings con palabras de distintas longitudes. Usa `removeIf()` para eliminar todas las palabras con longitud menor a 4. Imprime antes y después.

## Ejercicio 7 — Combinar listas con addAll()

Crea dos listas de strings. Combínalas en una tercera lista usando `addAll()`. Luego comprueba con `containsAll()` que la tercera contiene todos los elementos de ambas. Imprime los resultados.

## Ejercicio 8 — Arrays.asList() vs List.of(): diferencias de mutabilidad

Crea una lista con `Arrays.asList("a", "b", "c")` y otra con `List.of("a", "b", "c")`. Muestra que en la primera puedes modificar elementos (set) pero no añadir, y que en la segunda ninguna modificación está permitida. Captura las excepciones con try-catch para demostrarlo.
