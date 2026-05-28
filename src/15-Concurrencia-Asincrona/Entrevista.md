<div align="center">
  <a href="#"><img src="../../assets/modules/banner-15-concurrencia-asincrona-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Cuál es la diferencia entre `Future` y `CompletableFuture`?**
`Future` (Java 5) solo permite recuperar el resultado con `get()` (bloqueante) o cancelar. No se puede encadenar ni combinar. `CompletableFuture` añade: encadenamiento fluido (`thenApply`, `thenCompose`), combinación de múltiples futures (`allOf`, `anyOf`), manejo de errores (`exceptionally`) y completado manual (`complete()`).

---

**¿Qué diferencia hay entre `thenApply()` y `thenCompose()`?**
`thenApply()` aplica una función síncrona al resultado: `T → U`. `thenCompose()` aplica una función que devuelve otro `CompletableFuture<U>` y aplana el resultado: evita `CompletableFuture<CompletableFuture<U>>`. Es el flatMap de los futures.

---

**¿Cuándo usarías `thenCombine()` vs `allOf()`?**
`thenCombine()` combina el resultado de exactamente dos futures independientes con una función BiFunction. `allOf()` espera a que un número arbitrario de futures terminen todos (no devuelve resultados directamente — hay que extraerlos después con `join()`).

---

**¿Cómo manejas errores en CompletableFuture?**
Con `exceptionally(ex -> defaultValue)` para recuperación simple, o con `handle((result, ex) -> ...)` para manejar tanto el éxito como el error en una función. `whenComplete` también permite ejecutar efectos secundarios en ambos casos sin transformar el resultado.

---

**¿Qué pool de hilos usa CompletableFuture por defecto?**
`ForkJoinPool.commonPool()` — el pool común de la JVM. Se puede sobreescribir pasando un `Executor` como segundo argumento a `supplyAsync(task, executor)`. En producción con muchas tareas I/O, es recomendable usar un pool dedicado para no saturar el common pool.

---

**¿Cuál es la diferencia entre `exceptionally`, `handle` y `whenComplete`?**
`exceptionally(fn)` solo se ejecuta si el future falló — recibe la excepción y devuelve un valor de recuperación, transformando el error en éxito. `handle(fn)` recibe tanto el resultado (puede ser null) como la excepción (puede ser null) y siempre se ejecuta — permite transformar el resultado en ambos casos. `whenComplete(fn)` también recibe ambos, pero no puede transformar el resultado: solo sirve para efectos secundarios (logging, métricas) y propaga el resultado/excepción original sin cambios.

---

**¿Cuándo usar `thenCompose` en lugar de `thenCombine`?**
`thenCompose` es para encadenar futures dependientes: el segundo future necesita el resultado del primero para crearse (flatMap asíncrono). Evita `CompletableFuture<CompletableFuture<T>>`. `thenCombine` es para combinar dos futures independientes que se ejecutan en paralelo: espera a que ambos terminen y aplica una función con los dos resultados. Si los futures son dependientes, `thenCompose`; si son independientes y quieres paralelismo, `thenCombine`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
