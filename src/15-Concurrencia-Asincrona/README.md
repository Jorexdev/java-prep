<div align="center">
  <a href="#"><img src="../../assets/modules/banner-15-concurrencia-asincrona-v1.svg" width="100%" alt=""/></a>
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

La programación asíncrona permite iniciar operaciones potencialmente lentas (I/O, llamadas de red, cálculos) sin bloquear el hilo actual. El hilo continúa con otro trabajo y el resultado se procesa cuando esté disponible.

`CompletableFuture` (Java 8+) es la pieza central: un future que puede completarse manualmente y sobre el que se encadenan transformaciones y combinaciones de forma fluida.

```java
CompletableFuture<String> futuro = CompletableFuture
    .supplyAsync(() -> llamadaLenta())       // ejecuta en pool
    .thenApply(resultado -> transformar(resultado))  // encadena
    .exceptionally(ex -> "valor por defecto");       // maneja error
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Tipos de pools de hilos (ExecutorService):**

| Pool | Comportamiento |
|---|---|
| `newFixedThreadPool(n)` | N hilos fijos. Cola ilimitada. |
| `newCachedThreadPool()` | Hilos crece según demanda, se reutilizan. |
| `newScheduledThreadPool(n)` | Tareas con delay o periódicas. |
| `newVirtualThreadPerTaskExecutor()` | Un virtual thread por tarea (Java 21+). |

**CompletableFuture — operaciones clave:**

```java
// Transformación síncrona del resultado
.thenApply(x -> x.toUpperCase())

// Transformación que devuelve otro CompletableFuture (aplanamiento)
.thenCompose(x -> otroFuture(x))

// Combina dos futures independientes
.thenCombine(otro, (a, b) -> a + b)

// Espera que todos terminen
CompletableFuture.allOf(f1, f2, f3).join()

// Manejo de errores
.exceptionally(ex -> defaultValue)
.handle((resultado, ex) -> ex != null ? default : resultado)
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Non-blocking: el hilo no queda bloqueado esperando resultados lentos.
- Composición fluida de operaciones asíncronas.
- Manejo de errores integrado (`exceptionally`, `handle`).
- `allOf` para paralelizar múltiples operaciones y esperar todas.

Ver [ExpAsync.java](ExpAsync.java) para ejemplos de `supplyAsync`, `thenApply`, `thenCompose`, `allOf` y manejo de errores.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
