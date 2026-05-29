# Ejercicios — 26 Spring Boot: Logging
## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Structured JSON logging**

Implementa `JsonLogger` que produce una línea JSON por cada mensaje:
`{"ts":"2024-01-15T10:23:45.123","level":"INFO","logger":"c.a.Service","msg":"texto","userId":"42"}`
Los campos del MDC se incluyen como campos adicionales en el JSON.
Demo con 3 log entries, 2 de ellas con MDC populado (`userId` y `requestId`).

---

**Ejercicio 2 — Request tracing**

Al inicio de cada request, generar un `traceId` único (UUID recortado a 8 chars)
y guardarlo en el MDC. Al final del request, limpiar el MDC.
Demo con 3 threads simultáneos: cada thread simula un request con su propio traceId.
Verificar que cada thread tiene su traceId y no hay contaminación entre threads.

---

**Ejercicio 3 — Log sampling**

Implementa `SamplingLogger(double rate)` que solo loguea aproximadamente `rate * 100%`
de los mensajes (usando `Math.random()`). Con `rate=0.1`, loguea ~10%.
Demo: llamar `info()` 1000 veces y mostrar cuántos mensajes se loguearon realmente
(debería ser ~100). Mostrar el porcentaje real.

---

**Ejercicio 4 — Correlation ID chain**

Implementa `CorrelationContext` con `ThreadLocal<String>` para guardar el correlationId.
Simula una cadena de llamadas A→B→C: cada nivel lee el correlationId del contexto
y lo incluye en su log. Demo mostrando la propagación del mismo ID en los 3 niveles.

---

**Ejercicio 5 — Hot reload log level**

Implementa `LogLevelManager` que permite cambiar el nivel de cualquier logger en runtime
sin reiniciar la aplicación. Los loggers consultan el manager en cada llamada.
Demo: cambiar `com.app.service` de INFO a DEBUG (aparecen mensajes DEBUG),
luego volver a INFO (desaparecen). Mostrar el efecto en tiempo real.

---

**Ejercicio 6 — Structured logging con campos JSON automáticos via MDC**
Implementa `MDC` (Mapped Diagnostic Context) usando `ThreadLocal<Map<String,String>>`. `StructuredJsonLogger` emite cada log como una línea JSON con campos fijos (`ts`, `level`, `logger`, `msg`, `thread`) más todos los campos presentes en el MDC del hilo actual automáticamente, más campos extra opcionales pasados al método de log. `RequestContext(requestId, userId)` implementa `AutoCloseable`: puebla el MDC al construirse y lo limpia en `close()`. Demo con 5 escenarios: request normal (MDC con requestId+userId), pedido de alto valor (añade campo `role`), pedido fallido con error, 3 Virtual Threads concurrentes con MDC aislado por hilo, y log sin MDC activo.

---
