# Genéricos — Ejercicios Difícil

Ejercicios avanzados: type erasure, pipelines genéricos encadenados, Either<L,R>, Stack thread-safe.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Type erasure en runtime

Crea código que demuestre que `List<String>` y `List<Integer>` son el mismo tipo en runtime. Usa `getClass()` para obtener el tipo y muestra que son idénticos. Explica en comentarios por qué no se puede hacer `instanceof List<String>` y cómo hacerlo correctamente con wildcard.

## Ejercicio 2 — Pipeline de conversión encadenado

Crea clase genérica `Conversor<I, O>` con método `convertir(I input)`. Añade método `andThen(Conversor<O, N> siguiente)` que retorne un nuevo `Conversor<I, N>` que aplique los dos en secuencia. Encadena tres conversores: `String → Integer → Double → String`.

## Ejercicio 3 — Optional genérico con Predicate

Implementa método `<T> Optional<T> primerQueCompleta(List<T> lista, Predicate<T> pred)` que retorne el primer elemento que cumple el predicado. Prueba buscando el primer número par en una lista, y el primer string que empieza por "J".

## Ejercicio 4 — Stack<T> thread-safe

Crea clase `Stack<T>` genérica con métodos `push(T)`, `pop()`, `peek()` y `isEmpty()` usando `ArrayList` internamente y `synchronized` en cada método. Demuestra su uso desde dos hilos simultáneos (usa `Thread` y `join`).

## Ejercicio 5 — Either<L, R>

Implementa clase `Either<L, R>` que representa exactamente uno de dos posibles valores: izquierdo (`Left<L>`) o derecho (`Right<R>`). Convención: Left representa error, Right representa éxito. Métodos: `isLeft()`, `isRight()`, `getLeft()`, `getRight()`, `map(Function<R,N>)`. Usa para modelar resultado de división (Left si divisor es cero, Right con el resultado).
