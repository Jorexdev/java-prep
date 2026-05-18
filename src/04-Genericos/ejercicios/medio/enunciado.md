# Genéricos — Ejercicios Medio

Ejercicios de nivel intermedio para practicar PECS (Producer Extends Consumer Super),
wildcards acotados, Predicate genérico, bounded type parameters múltiples y repositorios genéricos.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — PECS: copiar listas

Implementa el método `<T> void copiar(List<? super T> dest, List<? extends T> src)` que copie
todos los elementos de `src` a `dest`. Demuestra su uso copiando una `List<Integer>` a una `List<Number>`.
Explica en comentarios por qué se usa `? extends T` para el origen y `? super T` para el destino.

## Ejercicio 2 — Sumar lista de números

Crea un método `<T extends Number> double suma(List<T> nums)` que sume todos los valores
de cualquier lista de subtipos de `Number`. Pruébalo con `List<Integer>`, `List<Double>` y `List<Long>`.

## Ejercicio 3 — Filtrar con Predicate

Implementa el método genérico `<T> List<T> filtrar(List<T> lista, Predicate<T> pred)` que
devuelva una nueva lista con solo los elementos que cumplen el predicado.
Úsalo para filtrar strings de longitud > 3 y para filtrar números pares de una lista de enteros.

## Ejercicio 4 — Resultado con valor o error

Crea una clase genérica `Resultado<T, E extends Exception>` que pueda contener un valor de tipo `T`
o un error de tipo `E`, pero nunca ambos. Incluye métodos `esExito()`, `getValor()` y `getError()`.
Demuestra su uso para modelar el resultado de parsear un entero desde un String.

## Ejercicio 5 — Mínimo de una lista

Implementa `<T extends Comparable<T>> T minimo(List<T> lista)` que recorra la lista y
devuelva el elemento menor. Lanza `IllegalArgumentException` si la lista está vacía.
Pruébalo con integers y con strings.

## Ejercicio 6 — Repositorio genérico

Crea una interfaz `Repositorio<T, ID>` con métodos `void save(T entidad)`, `Optional<T> findById(ID id)`
y `List<T> findAll()`. Implementa `RepositorioEnMemoria<T, ID>` usando un `HashMap`.
Crea una clase `Producto` (con campo id y nombre) y demuestra el repositorio.

## Ejercicio 7 — Sumar pesos con wildcard acotado superior

Modela una jerarquía: clase `Animal` con campo `pesoKg`, subclases `Perro` y `Gato`.
Crea el método `double sumaPesos(List<? extends Animal> animales)` que sume los pesos.
Demuestra que acepta tanto `List<Perro>` como `List<Gato>` como `List<Animal>`.
