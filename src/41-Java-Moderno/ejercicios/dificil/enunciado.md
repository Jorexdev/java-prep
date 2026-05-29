# Java Moderno — Ejercicios Difícil

Ejercicios avanzados que combinan todas las features modernas de Java para construir
sistemas no triviales: intérpretes, máquinas de estado, parsers y DSLs.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Intérprete de expresiones matemáticas

Construye un intérprete completo para expresiones matemáticas con:

**AST (sealed class hierarchy):**
```
sealed interface Expr permits Num, Var, Add, Sub, Mul, Div, Pow, Let
record Num(double value)
record Var(String name)
record Add(Expr left, Expr right)
record Sub(Expr left, Expr right)
record Mul(Expr left, Expr right)
record Div(Expr left, Expr right)
record Pow(Expr base, Expr exp)
record Let(String name, Expr value, Expr body)  // let x = 5 in x*x
```

**Implementar:**
1. `eval(Expr e, Map<String, Double> env)` — evalúa la expresión en un entorno de variables
2. `prettyPrint(Expr e)` — representación legible con paréntesis mínimos
3. `vars(Expr e)` — devuelve el `Set<String>` de variables libres (no ligadas por `Let`)

**Casos de prueba:**
- `let x = 3 in let y = 4 in sqrt(x^2 + y^2)` (usando `Pow` y `Sqrt` si lo añades)
- Variable libre: `x + y` con `env = {x: 10, y: 5}` → 15
- Variable no definida: debe lanzar `IllegalStateException`

## Ejercicio 2 — State machine con sealed classes

Implementa una máquina de estados para un proceso de pedido de e-commerce.

**Estados (sealed):**
```
sealed interface EstadoPedido permits
    Pendiente, Confirmado, Preparando, Enviado, Entregado, Cancelado
record Pendiente(java.time.Instant creadoEn)
record Confirmado(java.time.Instant confirmadoEn, String metodoPago)
record Preparando(java.time.Instant inicioEn)
record Enviado(java.time.Instant enviadoEn, String trackingCode)
record Entregado(java.time.Instant entregadoEn)
record Cancelado(java.time.Instant canceladoEn, String motivo)
```

**Transiciones (sealed):**
```
sealed interface Evento permits ConfirmarPago, IniciarPreparacion, Enviar, Entregar, Cancelar
record ConfirmarPago(String metodoPago)
record IniciarPreparacion()
record Enviar(String trackingCode)
record Entregar()
record Cancelar(String motivo)
```

Implementa `transicionar(EstadoPedido estado, Evento evento)` que devuelva
`Result<EstadoPedido>` (del ejercicio medio 1 o usa un tipo propio).
Transiciones inválidas deben devolver `Failure` con mensaje descriptivo.

Demuestra el happy path completo y al menos 2 transiciones inválidas.

## Ejercicio 3 — Mini JSON parser

Implementa un parser de JSON simplificado que produzca una jerarquía sealed.

**Tipos de valor JSON (sealed):**
```
sealed interface JsonValue permits JsonNull, JsonBool, JsonNumber, JsonString, JsonArray, JsonObject
record JsonNull()
record JsonBool(boolean value)
record JsonNumber(double value)
record JsonString(String value)
record JsonArray(java.util.List<JsonValue> elements)
record JsonObject(java.util.Map<String, JsonValue> fields)
```

**Implementar:**
1. `JsonValue parse(String json)` — parser completo (maneja anidamiento)
2. `String stringify(JsonValue v)` — serialización de vuelta a string JSON
3. `Optional<JsonValue> get(JsonValue obj, String key)` — acceso a campo de JsonObject

**Casos de prueba mínimos:**
- `null`, `true`, `false`, `42`, `3.14`, `"texto"`
- Array: `[1, "dos", true, null]`
- Objeto: `{"nombre": "Java", "version": 21, "activo": true}`
- Objeto anidado: `{"config": {"host": "localhost", "port": 8080}}`

## Ejercicio 4 — Visitor tradicional vs Pattern Matching: comparativa

Implementa una jerarquía de formas geométricas con **tres niveles de anidamiento**
(al menos 8 tipos de formas distintos, algunas con subtipos).

**Implementa el mismo conjunto de operaciones de dos formas:**
1. **Visitor tradicional**: interfaz `FormaVisitor<R>` con un método por tipo,
   cada forma implementa `accept(FormaVisitor<R> v)` (double dispatch)
2. **Pattern Matching**: método estático con switch expression

**Operaciones a implementar en ambas formas:**
- `calcularArea(Forma f)` → `double`
- `calcularPerimetro(Forma f)` → `double`
- `describir(Forma f)` → `String`

**Mide el tiempo** de 1.000.000 invocaciones de cada operación con JMH-style:
```java
long t1 = System.nanoTime();
for (int i = 0; i < 1_000_000; i++) { /* operacion */ }
long elapsed = System.nanoTime() - t1;
```

**Imprime una tabla comparativa** con tiempos y speedup. Explica en comentarios
qué ventajas/desventajas tiene cada enfoque desde el punto de vista de:
- Rendimiento
- Extensibilidad (añadir nuevo tipo vs nueva operación)
- Seguridad en compilación

## Ejercicio 5 — DSL de consultas con sealed classes

Diseña un DSL interno en Java para construir consultas sobre listas de objetos,
usando sealed classes para representar las operaciones de la pipeline.

**AST del DSL (sealed):**
```
sealed interface Query<T, R> permits
    Source, FilterOp, MapOp, ReduceOp, GroupByOp, LimitOp, SortOp
```

La implementación de cada operación debe usar el nodo anterior como fuente
(composición funcional).

**Ejemplo de uso del DSL:**
```java
List<String> resultado = Query.from(empleados)
    .filter(e -> e.salario() > 50000)
    .sortBy(Comparator.comparing(Empleado::nombre))
    .limit(5)
    .map(Empleado::nombre)
    .execute();
```

**Implementar:**
1. Las clases sealed para cada operación del AST
2. Un `Executor` que evalúa el AST produciendo el resultado
3. Métodos de construcción fluida (builder/fluent API)
4. Al menos 5 tipos de operación: `filter`, `map`, `reduce`, `groupBy`, `limit`

Demuestra con un dataset de empleados (record `Empleado(String nombre, String dept, double salario)`):
- Top 3 salarios por departamento
- Nombres de empleados con salario > media del dept
- Número de empleados por departamento
