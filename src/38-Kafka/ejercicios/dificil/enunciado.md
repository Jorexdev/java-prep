# Ejercicios — 38 Kafka

## Difícil

**Ejercicio 1 — Transactional Outbox Pattern**

Crea `Database` que simula una base de datos con `save(Pedido)` y una tabla `outbox_events` en memoria. Crea `OutboxPublisher`, un hilo que lee eventos pendientes del outbox y los publica en un topic Kafka simulado, marcando cada uno como publicado. En el `main`, guarda 3 pedidos de forma transaccional (BD + outbox en el mismo "commit"), luego arranca el publisher y verifica que todos los eventos llegan al topic exactamente una vez.

---

**Ejercicio 2 — Saga coreografiada con Kafka**

Implementa 4 servicios que se comunican mediante eventos Kafka:
- `ServicioPedido` publica `PedidoCreado` → `ServicioInventario` escucha
- `ServicioInventario` publica `StockReservado` o `StockInsuficiente` → `ServicioPago` escucha
- `ServicioPago` publica `PagoAprobado` o `PagoRechazado` → `ServicioEnvio` escucha
- `ServicioEnvio` publica `EnvioCreado` o `PedidoCancelado`

Si cualquier paso falla, se publica un evento de compensación para revertir los pasos anteriores. El `main` simula 3 flujos: éxito completo, fallo en pago (con compensación), fallo en stock.

---

**Ejercicio 3 — Proyección desde eventos**

Crea un `EventLog` append-only con los tipos de evento: `UsuarioCreado`, `EmailCambiado`, `PlanUpgraded` y `CuentaBloqueada`. Implementa `UsuarioProjection` que consume el log y mantiene la vista actual de cada usuario, e `HistorialProjection` que mantiene la lista de cambios por usuario. En el `main`, publica 10 eventos para 3 usuarios distintos, luego imprime el estado actual de cada uno y su historial de cambios.

---

**Ejercicio 4 — Log Compaction simulado**

Crea `CompactedTopic` con un `Map<String, String>` de `key → último valor` y una lista interna de todos los mensajes. Implementa `compact()` que elimina mensajes antiguos con la misma key, dejando solo el más reciente. Soporta tombstones: un mensaje con `value = null` borra la key del mapa compactado. En el `main`, produce 20 mensajes (5 keys distintas, cada una actualizada varias veces, más 1 tombstone), ejecuta compaction y verifica el estado final.

---

**Ejercicio 5 — Kafka Streams con stateful aggregation y ventana de tiempo**

Implementa un pipeline de Kafka Streams simulado con agregación stateful. Define `StreamRecord(String key, long value, long timestamp)`. Crea `TumblingWindow(long windowMs)` que agrupa records por clave y ventana de tiempo (los timestamps se dividen en ventanas fijas de `windowMs` ms). Implementa `KafkaStreamsAggregator` con `aggregate(List<StreamRecord>)` que devuelve un `Map<String, Map<Long, Long>>` de `clave → (inicio de ventana → suma)`. El `main` procesa 15 records de 3 claves distintas distribuidos en tres ventanas de 10 s cada una, y muestra la suma acumulada por clave y ventana.

---
