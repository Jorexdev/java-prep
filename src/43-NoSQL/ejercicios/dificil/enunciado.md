# Ejercicios — 43 NoSQL

## Difícil

**Ejercicio 1 — Distributed cache con locking (anti-stampede)**

Implementa un `DistributedCache` que evita cache stampede. Cuando hay un miss, solo un thread puede ir a DB (el que adquiere el lock con `ReentrantLock.tryLock()`); los demás esperan y leen el resultado cuando el primero termina. Usa `CountDownLatch` o `CompletableFuture` para sincronizar la espera. Simula 10 threads intentando obtener la misma key simultáneamente. Muestra que solo 1 llamada va a DB aunque todos lean el resultado.

---

**Ejercicio 2 — MongoDB $lookup (join)**

Simula el stage `$lookup` de MongoDB (equivalente a LEFT JOIN). Tienes dos colecciones: `pedidos` (id, clienteId, total) y `clientes` (id, nombre, email). Implementa `lookup(foreignCollection, localField, foreignField, as)` que enriquece cada documento de la colección local con el documento relacionado de la colección extranjera. En el `main`, une pedidos con sus clientes y muestra los documentos enriquecidos.

---

**Ejercicio 3 — Write-through vs Write-back cache**

Implementa dos estrategias de escritura: `WriteThroughCache` (escribe en cache Y DB de forma síncrona) y `WriteBackCache` (escribe solo en cache, acumula cambios, flush periódico a DB). Ambas extienden `CacheStrategy`. Añade contador de "DB writes". En el `main`, haz 10 escrituras con cada estrategia; muestra las DB writes de cada una. Discute (en comentario) cuándo cada estrategia es preferible.
