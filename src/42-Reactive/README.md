<div align="center">
  <a href="#"><img src="../../assets/modules/banner-42-reactive-v1.svg" width="100%" alt="42 - Reactive"/></a>
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

La **programación reactiva** es un paradigma de programación asíncrona orientado a flujos de datos y propagación de cambios. En vez de bloquear un hilo esperando un resultado, el código reacciona cuando los datos llegan: el productor emite, el consumidor recibe.

> Piensa en un periódico de suscripción: no llamas cada mañana para preguntar si hay noticias. Te suscribes y el periódico llega cuando está listo. Tú reaccionas cuando llega, no antes.

**Reactive Streams** es una especificación estándar (JDK 9+, `java.util.concurrent.Flow`) que define cuatro interfaces:

- `Publisher<T>` — produce elementos y acepta suscriptores
- `Subscriber<T>` — consume elementos: `onNext`, `onError`, `onComplete`
- `Subscription` — contrato entre publisher y subscriber: `request(n)` y `cancel()`
- `Processor<T,R>` — actúa como publisher y subscriber a la vez (transformación en medio)

**Project Reactor** (usado por Spring WebFlux) implementa esta spec con dos tipos principales:

```java
// Mono<T>: 0 o 1 elemento
Mono<String> mono = Mono.just("hola");

// Flux<T>: 0 a N elementos
Flux<Integer> flux = Flux.range(1, 10);
```

**Backpressure:** mecanismo que permite al subscriber controlar la velocidad del publisher. En vez de "empujar" todos los elementos a la vez, el subscriber pide exactamente cuántos puede procesar con `subscription.request(n)`. Evita desbordamientos cuando el productor es más rápido que el consumidor.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Mono vs Flux (Project Reactor):**

| Tipo | Cardinalidad | Uso típico |
|---|---|---|
| `Mono<T>` | 0 o 1 elemento | Llamada HTTP, búsqueda por ID, operación async que devuelve un valor |
| `Flux<T>` | 0 a N elementos | Lista de resultados, stream de eventos, paginación |

**Operadores principales:**

```java
// map: transformación síncrona 1:1
Flux.just("a", "b", "c")
    .map(String::toUpperCase)               // A, B, C

// flatMap: transformación async 1:N (concurrente)
Flux.just(1, 2, 3)
    .flatMap(n -> Flux.range(n * 10, 3))   // 10,11,12, 20,21,22, 30,31,32 (orden variable)

// concatMap: como flatMap pero mantiene orden (secuencial)
Flux.just(1, 2, 3)
    .concatMap(n -> Flux.range(n * 10, 3)) // 10,11,12, 20,21,22, 30,31,32 (orden garantizado)

// filter: descartar elementos que no cumplen predicado
Flux.range(1, 10).filter(n -> n % 2 == 0) // 2, 4, 6, 8, 10

// zip: combinar dos publishers elemento a elemento
Flux.zip(Flux.just("a","b"), Flux.just(1,2)) // [a,1], [b,2]
```

**Error handling reactivo:**

```java
Flux.just(1, 0, 2)
    .map(n -> 10 / n)
    .onErrorReturn(-1)           // si hay error, emitir -1 y completar
    .onErrorResume(e -> Flux.just(0, 0))  // si hay error, continuar con fallback flux
    .retry(3)                    // reintentar hasta 3 veces antes de propagar error
    .doOnError(e -> log(e))      // efecto secundario al error, sin cambiar el flujo
```

**Schedulers (gestión de hilos):**

- `subscribeOn(Schedulers.boundedElastic())` — cambia el hilo donde corre la suscripción (útil para I/O blocking)
- `publishOn(Schedulers.parallel())` — cambia el hilo de procesamiento de los operadores siguientes
- La diferencia: `subscribeOn` afecta desde el origen; `publishOn` afecta desde donde se coloca hacia abajo

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

- **Non-blocking I/O:** un hilo gestiona miles de operaciones I/O concurrentes sin bloquearse — ideal para APIs de alta carga.
- **Backpressure nativa:** el subscriber controla el ritmo; no hay riesgo de que el producer desborde la memoria.
- **Composabilidad:** los operadores son funciones puras encadenables — el pipeline es legible y testeable paso a paso.
- **Propagación de errores:** los errores viajan por el stream como señales, no como excepciones que explotan en algún hilo arbitrario.
- **Cancelación limpia:** `Subscription.cancel()` propaga hacia arriba, liberando recursos automáticamente.

**Cuándo NO usar programación reactiva:**
- Operaciones CPU-bound simples (Java 21 virtual threads son suficientes y más simples)
- Código con mucho estado mutable compartido (la asincronía lo complica aún más)
- Equipos sin experiencia reactiva (la curva de aprendizaje es real)

Ver ejemplos ejecutables:
- [ExpFlowAPI.java](ExpFlowAPI.java) — Java Flow API estándar (JDK)
- [ExpReactorPatterns.java](ExpReactorPatterns.java) — simulación de Mono/Flux con generics
- [ExpBackpressure.java](ExpBackpressure.java) — estrategias de backpressure
- [ExpOperators.java](ExpOperators.java) — operadores encadenables
- [ExpErrorHandling.java](ExpErrorHandling.java) — manejo de errores reactivo

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
