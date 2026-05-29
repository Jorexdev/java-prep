# Reactive — Ejercicios Difícil

Ejercicios avanzados: Scheduler propio con Virtual Threads, simulación de WebFlux, rate limiter, event sourcing, pub/sub con routing.

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Scheduler propio con Virtual Threads
Implementa dos schedulers usando pools de Virtual Threads:
- `SchedulerIO`: para operaciones I/O bloqueantes (unbounded VT pool)
- `SchedulerCompute`: para cómputo (pool de N VTs = número de CPUs)

Implementa `publishOn(Scheduler)` y `subscribeOn(Scheduler)` sobre un pipeline reactivo propio:
- `publishOn` cambia el hilo de los operadores siguientes (downstream)
- `subscribeOn` cambia el hilo del origen del stream (upstream)

Demo con un pipeline que lee datos en `SchedulerIO`, los transforma en `SchedulerCompute` y los imprime mostrando en qué hilo corre cada etapa.

## Ejercicio 2 — Simulación de WebFlux request handling
Simula el ciclo completo de una petición no-bloqueante al estilo Spring WebFlux:
1. Recibir request `GET /usuarios/{id}` con un `id`
2. "Buscar en DB" de forma async (CompletableFuture, simula 50ms de latencia)
3. "Transformar" el resultado: añadir campo `"role"` y `"lastSeen"` al usuario
4. Responder: imprimir el JSON del usuario enriquecido

Todo debe ser non-blocking: ningún hilo debe quedar bloqueado esperando la "DB". Maneja el caso de usuario no encontrado (404 simulado) y error de DB (500 simulado). Procesa 5 requests concurrentes y mide el tiempo total (debe ser ~50ms, no 250ms).

## Ejercicio 3 — Rate limiter reactivo
Implementa un `RateLimiter` reactivo que limita el flujo a **N elementos por segundo** con backpressure correcto.
El rate limiter debe:
- Aceptar hasta N tokens por segundo (token bucket o sliding window)
- Si llegan más elementos de los permitidos, aplicar backpressure (no DROP ni ERROR, sino esperar)
- Cuando se liberen tokens, continuar procesando los elementos en espera
Demo: 20 eventos a emitir lo más rápido posible, rate limit = 5/segundo. Debe tardar ~4 segundos en procesar todos. Muestra el timestamp de cada elemento procesado.

## Ejercicio 4 — Event sourcing reactivo
Implementa un sistema de event sourcing simplificado:
- Define los tipos de evento: `CuentaCreada(id, titular)`, `DepositoRealizado(id, importe)`, `RetiroRealizado(id, importe)`
- Define el estado actual de una cuenta: `Cuenta(id, titular, saldo)`
- Implementa `fold(List<Evento>, Cuenta inicial)` que recorre el stream de eventos y aplica cada uno para reconstruir el estado actual
- Crea un stream de 10 eventos para una cuenta y reconstruye el estado final
- Muestra el estado después de cada evento (snapshot intermedio)
- Verifica que el saldo nunca sea negativo; si un retiro lo dejaría negativo, genera un evento `RetiroRechazado` y no aplica el retiro

## Ejercicio 5 — Pub/Sub con múltiples topics y routing
Implementa un sistema de pub/sub reactivo con routing por tipo de evento:
- `EventBus` central que acepta eventos `{topic, payload}`
- Los subscribers se registran con un predicado de filtrado: `bus.subscribe("logs.*", handler)`, `bus.subscribe("user.created", handler)`
- Soporta wildcards en topics: `"logs.*"` casa con `"logs.error"`, `"logs.info"`, etc.
- Implementa `publish(String topic, Object payload)` que enruta al subscriber correcto
- Demo con 3 topics: `"user.created"`, `"user.deleted"`, `"logs.error"`, `"logs.info"`, `"payment.done"`
- 4 subscribers con distintos patrones; muestra qué subscriber recibió qué evento
- El EventBus debe ser thread-safe (múltiples publishers concurrentes)
