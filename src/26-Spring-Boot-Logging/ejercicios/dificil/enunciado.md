# Ejercicios — 26 Spring Boot: Logging
## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Async logger**

Implementa `AsyncLogger` con `LinkedBlockingQueue<LogEvent>` y un thread escritor dedicado
(daemon thread). El hilo principal solo encola eventos (no bloquea en IO).
Demo: encolar 50 eventos desde el hilo principal y mostrar que el encolado termina
rápidamente mientras el writer thread procesa en segundo plano.
Incluye un flush/shutdown limpio.

---

**Ejercicio 2 — Log aggregator**

Implementa `LogAggregator` que recibe logs de 3 loggers distintos.
Procesa los logs en batches: cada 200ms, vacía el buffer y emite
`"Batch #N: X events"` seguido de las entradas del batch.
Demo: enviar 100 eventos en total (distribuidos entre 3 fuentes) y
verificar que se procesan en varios batches.

---

**Ejercicio 3 — Error budget**

Implementa `ErrorBudgetLogger` que usa una ventana deslizante de 60 segundos.
Cuenta el porcentaje de logs de nivel ERROR en esa ventana.
Si el porcentaje supera el 5%, imprime una alerta `"[ALERT] Error budget exceeded: X%"`.
Demo: simular un burst de errores que hace superar el umbral, mostrando la alerta.

---

**Ejercicio 4 — Span tracing**

Define `Span(traceId, spanId, parentSpanId, name, startMs, endMs)`.
Implementa `Tracer` que crea spans hijos automáticamente: el span activo
se convierte en el padre del siguiente.
Al finalizar la traza, imprime el árbol de spans con indentación y duraciones.
Demo: operación raíz con 2 hijos y cada hijo con un nieto.
