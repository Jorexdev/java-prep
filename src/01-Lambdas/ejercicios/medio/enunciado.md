# Lambdas — Ejercicios Medio

Ejercicios de nivel intermedio para practicar composición de interfaces funcionales:
`BiFunction`, `UnaryOperator`, `BinaryOperator`, composición con `and`/`or`/`negate`, `andThen`/`compose`, e interfaces funcionales custom.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Interfaz funcional custom

Define una interfaz funcional genérica `Transformador<T, R>` con un método `transformar(T t)`. Crea dos instancias con lambda: una que convierta `String` a su longitud, y otra que convierta `Integer` a su representación en binario. Prueba ambas.

## Ejercicio 2 — Composición con and

Crea dos `Predicate<String>`: uno que evalúe si la longitud es mayor que 3, y otro que evalúe si el string empieza por vocal (a, e, i, o, u sin distinguir mayúsculas). Combínalos con `and` y filtra una lista de palabras que cumplan ambas condiciones.

## Ejercicio 3 — Composición con andThen

Crea dos `Function<String, String>`: una que aplique `trim()` y otra que aplique `toUpperCase()`. Combínalas con `andThen` para crear una función que primero limpie espacios y luego ponga en mayúsculas. Aplícala a una lista de strings con espacios.

## Ejercicio 4 — BiFunction

Crea un `BiFunction<String, Integer, String>` que repita un string N veces concatenándolo. Por ejemplo, `("ab", 3)` produce `"ababab"`. Pruébalo con varios casos.

## Ejercicio 5 — negate

Crea un `Predicate<Integer>` que evalúe si un número es par. Usa `negate()` para obtener el predicado de impares sin escribir una lambda nueva. Filtra una lista con ambos predicados y muestra los resultados.

## Ejercicio 6 — UnaryOperator Caesar cipher

Crea un `UnaryOperator<String>` que aplique un cifrado César con desplazamiento +3: cada letra avanza 3 posiciones en el alfabeto (solo letras, sin tocar otros caracteres). Pruébalo con la cadena `"Hola Mundo"`.

## Ejercicio 7 — Composición con or

Crea dos `Predicate<Integer>`: uno que evalúe si el número es mayor que 100, y otro si es múltiplo de 5. Combínalos con `or` y filtra una lista de enteros que cumplan al menos una condición.

## Ejercicio 8 — BinaryOperator máximo

Crea un `BinaryOperator<Integer>` que retorne el máximo de dos enteros. Úsalo con `reduce` sobre una lista de enteros para encontrar el valor máximo.
