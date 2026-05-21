# Ejercicios — 39 Microservicios

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

## Ejercicio 1 — Circuit Breaker básico

Implementa un `CircuitBreaker` con tres estados: `CLOSED`, `OPEN` y `HALF_OPEN`.

El constructor recibe `failureThreshold` (número de fallos para abrir) y `waitMs` (tiempo de espera antes de pasar a HALF_OPEN).

Comportamiento:
- **CLOSED**: deja pasar las llamadas. Cuenta fallos consecutivos. Al llegar al threshold → OPEN.
- **OPEN**: rechaza todas las llamadas con `CircuitOpenException`. Tras `waitMs` → HALF_OPEN.
- **HALF_OPEN**: deja pasar una sola llamada de prueba. Si tiene éxito → CLOSED. Si falla → OPEN.

El `main` debe simular un servicio que falla 3 veces (abre el circuito), espera la ventana de tiempo (simulada con un campo de timestamp), realiza la prueba en HALF_OPEN y vuelve a CLOSED.

---

## Ejercicio 2 — Retry con backoff exponencial

Implementa `RetryPolicy(int maxAttempts, long initialDelayMs, double multiplier)` con un método `execute(Supplier<T> action)` que reintenta la acción aplicando backoff exponencial.

Los delays deben ser: `initialDelayMs`, `initialDelayMs * multiplier`, `initialDelayMs * multiplier²`…

Simula el delay con una variable acumulada (sin `Thread.sleep` real). Imprime cada intento indicando el número de intento y el delay acumulado. Lanza `MaxAttemptsExceededException` si se agotan los reintentos.

El `main` prueba un servicio que falla en los dos primeros intentos y tiene éxito en el tercero.

---

## Ejercicio 3 — Timeout

Implementa `TimeoutExecutor` con un método `execute(Supplier<T> task, long timeoutMs)`.

Si la tarea no completa en el tiempo dado, lanza `TimeoutException`. Usa `ExecutorService.submit()` y `Future.get(timeout, TimeUnit.MILLISECONDS)`.

El `main` prueba:
- Una tarea rápida que completa sin problema.
- Una tarea lenta que duerme más tiempo del timeout y provoca la excepción.

---

## Ejercicio 4 — Service Registry

Implementa `ServiceRegistry` con las operaciones:
- `register(String name, String host, int port)` → devuelve un `instanceId` único.
- `deregister(String name, String instanceId)`.
- `discover(String name)` → devuelve la lista de instancias con estado `UP`.

Cada instancia tiene campos: `instanceId`, `host`, `port`, `status` (enum `UP`/`DOWN`).

Implementa `HealthChecker` que puede marcar instancias como `DOWN`.

El `main` registra 3 instancias de `"inventario-service"`, marca una como `DOWN` y descubre las disponibles.

---

## Ejercicio 5 — Health Check

Define la interfaz `HealthIndicator` con método `HealthStatus check()`.

`HealthStatus` tiene: `component` (String), `status` (String: `"UP"`, `"DEGRADED"`, `"DOWN"`), `details` (Map<String, Object>).

Implementaciones:
- `DatabaseHealthIndicator` — simula un ping a la base de datos.
- `DiskSpaceIndicator` — simula la comprobación de espacio libre en disco.
- `DependencyHealthIndicator` — simula una llamada a un servicio externo.

`HealthEndpoint` agrega todos los indicadores: el estado global es `UP` si todos están `UP`, `DEGRADED` si alguno está `DEGRADED`, `DOWN` si alguno está `DOWN`.

El `main` muestra el informe completo de salud del sistema.

---

## Ejercicio 6 — Load Balancer

Implementa `LoadBalancer` con tres estrategias de selección de instancias:
- `RoundRobin` — índice rotativo circular.
- `Random` — selección aleatoria.
- `LeastConnections` — elige la instancia con menos conexiones activas.

Cada instancia tiene un contador de conexiones activas. El `main` simula 10 requests con cada estrategia e imprime la distribución de carga.
