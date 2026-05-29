# Ejercicios — 39 Microservicios

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Saga orquestada

Implementa `SagaOrchestrator` que coordina una secuencia de pasos con compensación automática.

Define `SagaStep(String name, Runnable action, Runnable compensation)`.

Si un paso lanza excepción, el orquestador ejecuta las compensaciones de todos los pasos ya completados en orden inverso.

La saga simula: `CrearPedido` → `ReservarStock` → `CobrarPago` → `CrearEnvio`.

El `main` ejecuta dos flujos:
1. Todos los pasos tienen éxito.
2. El paso `CobrarPago` falla → se ejecutan compensaciones de `ReservarStock` y `CrearPedido` en ese orden.

---

## Ejercicio 2 — API Gateway

Implementa `Gateway` con las siguientes responsabilidades:
- **Routing**: `addRoute(String prefix, String target)` — mapea prefijos de ruta a servicios destino.
- **Autenticación**: valida que el request tenga una API key registrada.
- **Rate limiting**: token bucket por `clientId` (configurable: tokens por ventana de tiempo).
- **Logging**: registra cada request con timestamp, cliente, ruta y resultado.

`GatewayRequest` tiene: `clientId`, `apiKey`, `path`, `method`.

El `main` prueba:
- Request válido y autenticado dentro del límite de rate.
- Request sin API key válida.
- Cliente que supera el rate limit.
- Ruta no encontrada en el routing.

---

## Ejercicio 3 — Bulkhead

Implementa `Bulkhead(String name, int maxConcurrent)` usando `Semaphore` para limitar las llamadas concurrentes a un servicio.

`BulkheadRegistry` gestiona un bulkhead por nombre de servicio.

Si no hay permisos disponibles en el semáforo, lanza `BulkheadFullException` inmediatamente (sin bloquear).

El `main` lanza 10 llamadas concurrentes con `ExecutorService` a un bulkhead de capacidad 3. Muestra cuántas se ejecutan con éxito y cuántas son rechazadas.

---

## Ejercicio 4 — Correlation ID y trazado distribuido

Implementa `TraceContext(String traceId, String spanId, String parentSpanId)`.

`TraceContextHolder` almacena el contexto en un `ThreadLocal`.

`TracingClient` propaga el contexto simulando cabeceras HTTP.

Simula una cadena de llamadas: `OrderService` → `InventoryService` → `PaymentService`. Cada servicio crea un nuevo span hijo pero mantiene el mismo `traceId`.

El `main` inicia una traza y muestra el árbol de spans con su jerarquía.

---

## Ejercicio 5 — Distributed Lock con TTL

Implementa `DistributedLockManager` con `ConcurrentHashMap<String, LockEntry>`.

`LockEntry` tiene: `owner` (String) y `expiryMs` (timestamp de expiración).

Métodos:
- `tryLock(String resource, String owner, long ttlMs)` → `boolean`.
- `unlock(String resource, String owner)`.

Un hilo de fondo limpia periódicamente los locks expirados.

El `main` lanza 3 hilos compitiendo por el mismo recurso, demuestra exclusión mutua y que un lock expira si el propietario no lo libera a tiempo.

---

## Ejercicio 6 — Circuit Breaker con estados CLOSED/OPEN/HALF_OPEN

Implementa `CircuitBreaker(String name, int failureThreshold, long openTimeoutMs)` con tres estados: `CLOSED` (tráfico normal), `OPEN` (rechaza todas las llamadas con `CircuitOpenException`) y `HALF_OPEN` (permite una llamada de prueba para verificar recuperación). Transiciones: si los fallos superan el threshold en estado `CLOSED` → pasa a `OPEN`; tras `openTimeoutMs` → pasa a `HALF_OPEN`; si la llamada de prueba en `HALF_OPEN` tiene éxito → vuelve a `CLOSED`; si falla → vuelve a `OPEN`. Usa un reloj simulado con `AtomicLong`. El `main` lanza 10 llamadas con 3 fallos consecutivos, muestra la transición a `OPEN`, avanza el reloj para pasar a `HALF_OPEN`, y demuestra la recuperación exitosa.

---
