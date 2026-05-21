# Ejercicios — 26 Spring Boot: Logging
## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Log levels**

Implementa `Logger` con 5 niveles: `TRACE(0)`, `DEBUG(1)`, `INFO(2)`, `WARN(3)`, `ERROR(4)`.
Solo loguea si el nivel del mensaje es mayor o igual al nivel configurado.
Demo: con nivel configurado a `INFO`: loguear TRACE, DEBUG, INFO, WARN y ERROR.
Verificar que solo INFO, WARN y ERROR aparecen en la salida.

---

**Ejercicio 2 — Logger hierarchy**

Implementa un árbol de loggers con herencia: si un logger no tiene nivel configurado,
hereda el del padre. Árbol: `root(INFO)` → `com.app(DEBUG)` → `com.app.service(no level)`.
`com.app.service` hereda DEBUG de `com.app`. Un cuarto logger `com.other(no level)` hereda INFO de root.
Demo con 4 loggers mostrando qué nivel efectivo usa cada uno.

---

**Ejercicio 3 — MDC básico**

Implementa `MDC` con `ThreadLocal<Map<String, String>>`.
Métodos: `put(key, value)`, `get(key)`, `clear()`.
Implementa `Logger` que incluye el contenido del MDC actual en cada línea.
Demo: poner `userId=42` y `requestId=abc` en el MDC y loguear 3 mensajes.
Mostrar que el MDC se incluye automáticamente en cada línea.

---

**Ejercicio 4 — Log format**

Implementa un formatter que produce líneas con el formato exacto:
`"2024-01-15 10:23:45.123 [INFO ] [main  ] c.a.Service - mensaje"`
- Timestamp: `yyyy-MM-dd HH:mm:ss.SSS`
- Level: 5 chars con padding izquierdo
- Thread: 6 chars con padding derecho
- Logger: abreviado (primera letra de cada paquete, último segmento completo)
Demo con 4 mensajes de distintos niveles y threads.

---

**Ejercicio 5 — Performance logging**

Implementa `timedRun(String name, Runnable r)` que ejecuta `r` y loguea
`"[PERF] nombre: Xms"` con el tiempo transcurrido.
Demo wrapeando 3 operaciones: una rápida (<1ms), una media (con Thread.sleep 50ms),
y una lenta (Thread.sleep 200ms).

---

**Ejercicio 6 — Lazy logging**

Implementa `logger.debug(Supplier<String> msgSupplier)` que solo evalúa el Supplier
si el nivel DEBUG está activo. Define `computeExpensive()` que imprime "EVALUANDO..."
y devuelve una cadena.
Demo mostrando la diferencia: con lazy (DEBUG activo → evalúa, DEBUG inactivo → no evalúa)
vs sin lazy (siempre evalúa aunque no loguee).
