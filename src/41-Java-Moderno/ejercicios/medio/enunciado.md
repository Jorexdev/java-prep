# Java Moderno — Ejercicios Medio

Ejercicios de nivel intermedio que combinan Records, Sealed Classes y Pattern Matching
para modelar dominios reales: tipos resultado, máquinas de estado y jerarquías polimórficas.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Result<T> con sealed

Modela un tipo `Result<T>` sealed con dos variantes: `Success<T>(T value)` y `Failure<T>(String error)`.
Implementa los siguientes métodos en la interfaz:
- `boolean isSuccess()`
- `T getOrElse(T defaultValue)` — devuelve el valor o el defecto si es Failure
- `<U> Result<U> map(java.util.function.Function<T, U> fn)` — transforma el valor si es Success
- `static <T> Result<T> of(java.util.concurrent.Callable<T> fn)` — factory que captura excepciones

Demuestra el uso con: división entera (Failure si divisor es 0), parseo de entero desde String,
y encadenamiento de `map` para transformar `Result<String>` a `Result<Integer>` a `Result<String>`.

## Ejercicio 2 — Pedidos con guarded patterns

Crea records `Pedido(String id, String estado, int prioridad, double importe)` donde
`estado` puede ser `"PENDIENTE"`, `"PROCESANDO"`, `"ENVIADO"`, `"CANCELADO"`.

Escribe un método `clasificar(Pedido p)` que use switch expression con guarded patterns (`when`)
y devuelva una categoría:
- PENDIENTE con prioridad >= 8 y importe > 1000: `"URGENTE_ALTO_VALOR"`
- PENDIENTE con prioridad >= 8: `"URGENTE"`
- PENDIENTE: `"NORMAL"`
- PROCESANDO con importe > 500: `"PROCESANDO_ALTO_VALOR"`
- PROCESANDO: `"EN_CURSO"`
- ENVIADO: `"COMPLETADO"`
- CANCELADO: `"BAJA"`
- default: `"DESCONOCIDO"`

## Ejercicio 3 — Visitor sin double dispatch

Tienes la jerarquía:
```
sealed interface Expr permits Literal, Suma, Multiplicacion, Negacion
record Literal(double valor)
record Suma(Expr izq, Expr der)
record Multiplicacion(Expr izq, Expr der)
record Negacion(Expr expr)
```

Implementa sin clases Visitor (sin double dispatch) usando switch + pattern matching:
- `eval(Expr e)` — evalúa la expresión y devuelve `double`
- `prettyPrint(Expr e)` — devuelve la expresión como string legible (ej: `"(3.0 + 4.0)"`)
- `contar(Expr e)` — cuenta cuántos nodos tiene el árbol

Demuestra con `(3 + 4) * -(2)`.

## Ejercicio 4 — Record implementando comportamiento polimórfico

Define una interfaz `Serializable` (propia, no java.io) con método `String toJson()`.
Crea tres records que la implementen: `Usuario(String nombre, String email)`,
`Producto(String nombre, double precio)`, `Pedido(int id, Usuario usuario, Producto producto)`.

Cada record implementa `toJson()` produciendo JSON válido (sin librería).
El record `Pedido` reutiliza `toJson()` de sus componentes para el JSON anidado.

Crea una lista de `Serializable` con instancias mezcladas e imprímelas con su JSON.

## Ejercicio 5 — Deconstruction patterns anidados

Dada la jerarquía del Ejercicio 3 (`Expr` sealed), escribe un método
`simplificar(Expr e)` que aplique estas reglas de simplificación algebraica:
- `Suma(Literal(0), x)` → devuelve `x` directamente
- `Suma(x, Literal(0))` → devuelve `x`
- `Multiplicacion(Literal(1), x)` → devuelve `x`
- `Multiplicacion(x, Literal(1))` → devuelve `x`
- `Multiplicacion(Literal(0), x)` → devuelve `Literal(0)`
- `Negacion(Negacion(x))` → devuelve `x`
- Cualquier otro caso → devuelve el nodo sin cambios

Usa deconstruction patterns anidados en switch. Demuestra con varios árboles que contienen
estas formas simplificables.

## Ejercicio 6 — Option<T> como sealed discriminated union

Implementa `Option<T>` como sealed interface con `Some<T>(T value)` y `None<T>`.
Implementa en la interfaz:
- `boolean isPresent()`
- `T get()` — lanza `NoSuchElementException` si es None
- `T getOrElse(T defaultValue)`
- `<U> Option<U> map(java.util.function.Function<T, U> fn)`
- `Option<T> filter(java.util.function.Predicate<T> pred)` — devuelve None si no cumple
- `static <T> Option<T> of(T value)` — None si null, Some si no null
- `static <T> Option<T> empty()`

Demuestra con: parseo de entero que puede fallar, encadenamiento de map y filter,
y comparación de comportamiento con `Optional<T>` de Java.
