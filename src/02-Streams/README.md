<div align="center">
  <a href="#"><img src="../../assets/modules/banner-02-streams-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Un **Stream** es un pipeline de operaciones sobre una secuencia de datos. No es una estructura de datos: no almacena elementos, los procesa de forma **lazy** (la ejecución se pospone hasta que se invoca una operación terminal).

Introducidos en Java 8 junto con las lambdas, los Streams son la base de la programación declarativa en Java: describes *qué* quieres hacer, no *cómo* iterar.

> Piensa en un Stream como una cadena de montaje: los datos entran por un extremo, pasan por distintas estaciones de transformación y salen por el otro en la forma que necesitas.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

Las operaciones se dividen en dos tipos:

**Intermedias** (devuelven Stream, son lazy — no se ejecutan hasta la terminal):

| Operación | Descripción |
|---|---|
| `filter(Predicate)` | Filtra elementos |
| `map(Function)` | Transforma elementos |
| `flatMap(Function)` | Aplana streams anidados |
| `sorted()` | Ordena elementos |
| `distinct()` | Elimina duplicados |
| `limit(n)` | Toma los primeros N |
| `skip(n)` | Salta los primeros N |

**Terminales** (disparan la ejecución y consumen el Stream):

| Operación | Descripción |
|---|---|
| `collect(Collector)` | Recoge en colección |
| `forEach(Consumer)` | Itera y consume |
| `count()` | Cuenta elementos |
| `reduce(BinaryOp)` | Reduce a un valor |
| `findFirst()` / `findAny()` | Primer / cualquier elemento |
| `anyMatch` / `allMatch` / `noneMatch` | Evaluaciones booleanas |
| `min()` / `max()` | Mínimo / máximo |

**Streams paralelos:** `list.parallelStream()` divide el procesamiento en múltiples hilos (ForkJoinPool). Útil para colecciones grandes sin estado compartido; puede ser contraproducente en operaciones pequeñas.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

- Código declarativo: expresa *qué* sin especificar *cómo* iterar.
- Composición fluida de operaciones.
- Evaluación lazy: solo se procesan los elementos necesarios.
- Soporte paralelo sin gestión manual de hilos.
- Integración natural con lambdas y referencias a métodos.

Ver [ExpStreams.java](ExpStreams.java) para ejemplos de `filter`, `map`, `flatMap`, `collect`, `reduce`, `groupingBy` y streams paralelos.

Los ejercicios están en [ejercicios/](ejercicios/) con tres niveles de dificultad.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
