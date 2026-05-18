# Lambdas — Ejercicios Difícil

Ejercicios de nivel avanzado para practicar referencias a métodos, currying, composición genérica, memoización y pipelines de transformación complejos.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Referencias a métodos (4 formas)

Tienes código que usa lambdas en cuatro contextos distintos. Reescribe cada uno usando la referencia a método correspondiente e indica de qué tipo es cada una:

1. Un `Consumer<String>` con `s -> System.out.println(s)` (referencia a método de instancia de objeto concreto)
2. Un `Function<String, String>` con `s -> s.toUpperCase()` (referencia a método de instancia de tipo arbitrario)
3. Un `Function<String, Integer>` con `s -> Integer.parseInt(s)` (referencia a método estático)
4. Un `Supplier<List<String>>` construido con `() -> new ArrayList<>()` (referencia a constructor)

Muestra la salida para confirmar que el comportamiento es idéntico antes y después del cambio.

## Ejercicio 2 — Currying

Implementa currying manual usando `Function<Integer, Function<Integer, Integer>>`. Crea una función `sumar` que, dado un primer entero, retorne otra función que espera el segundo entero y devuelve la suma. Usa aplicación parcial para crear `sumar5` (suma 5 a cualquier número) y aplícala a una lista de enteros.

## Ejercicio 3 — Composición genérica de N funciones

Implementa un método estático genérico `componerTodas(List<Function<T, T>> funciones)` que encadene todas las funciones de la lista en orden (la primera se aplica primero). Pruébalo con una lista de 4 transformaciones sobre strings: trim, lowercase, reemplazar espacios por guiones, y añadir sufijo `"-processed"`.

## Ejercicio 4 — Memoización

Implementa una función de memoización: un método estático `memoizar(Function<T, R> fn)` que retorne una nueva `Function<T, R>` respaldada por un `Map`. Si la clave ya fue calculada, devuelve el resultado cacheado sin volver a invocar la función original. Demuéstralo con una función que imprima un mensaje cuando se calcula (para verificar que la segunda llamada no lo imprime).

## Ejercicio 5 — Comparator encadenado con referencias a métodos

Dada una lista de `Empleado` con nombre, departamento y salario, ordénala usando `Comparator.comparing().thenComparing().thenComparing()` con referencias a métodos (sin lambdas) aplicando: primero por departamento alfabético, luego por salario descendente, finalmente por nombre alfabético. Imprime el resultado.

## Ejercicio 6 — Pipeline de 4 pasos con andThen

Construye un pipeline de transformación para procesar strings usando `Function.andThen()` encadenado en 4 pasos:
1. Eliminar caracteres no alfabéticos (solo letras y espacios)
2. Convertir a minúsculas
3. Capitalizar la primera letra de cada palabra
4. Añadir el prefijo `"Procesado: "`

Aplica el pipeline a una lista de strings con caracteres especiales y números.
