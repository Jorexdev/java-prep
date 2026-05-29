# Ejercicios — 38 Kafka

## Medio

**Ejercicio 1 — Retry con Dead Letter Topic**

Crea un `ReliableConsumer` que intenta procesar cada mensaje hasta 3 veces (sin delay). Si falla las 3 veces, reenvía el mensaje al topic `nombre-original.DLT`. La clase `Procesador` lanza excepción para mensajes que contienen la cadena `"ERROR"`. En el `main`, produce 5 mensajes (2 válidos, 2 con `"ERROR"`, 1 válido) y muestra qué mensajes acaban en el DLT.

---

**Ejercicio 2 — Productor idempotente (exactly-once simulado)**

Crea un `IdempotentProducer` que añade un `sequenceNumber` incremental a cada mensaje. Crea `IdempotentBroker` que rechaza mensajes cuyo sequence ya fue visto (usando un `Map<String, Long>` de `producerId → lastSeq`). En el `main`, simula reenvío tras fallo enviando el mismo mensaje dos veces: el broker acepta el primero y rechaza el duplicado. Imprime la secuencia completa de eventos.

---

**Ejercicio 3 — Kafka Streams simulado**

Crea `StreamPipeline<In, Out>` que encadena operaciones: `filter(Predicate)`, `map(Function)` y `forEach(Consumer)`. Los datos fluyen desde un topic fuente. Procesa el topic `"pedidos-raw"` con strings en formato `"id:precio"`: filtra los de precio > 100, transforma cada uno a un objeto `Pedido` y los entrega al topic `"pedidos-validos"`. En el `main`, demuestra el pipeline con 8 mensajes variados.

---

**Ejercicio 4 — Consumer lag**

Crea `LagMonitor` para un consumer group: compara `latestOffset` (último offset producido en cada partición) con `committedOffset` (último offset confirmado por el consumer). `lag = latestOffset - committedOffset`. En el `main`, simula 3 particiones, produce mensajes a distintas velocidades, consume parcialmente en cada una e imprime una tabla de lag por partición y el lag total.

---

**Ejercicio 5 — Rebalancing**

Crea `ConsumerGroup` con un método `rebalance()` que redistribuye particiones equitativamente entre consumers activos usando round-robin. Empieza con 2 consumers y 4 particiones (2 cada uno). Añade un tercer consumer → rebalance → distribución (2,1,1). Elimina un consumer → rebalance → distribución (2,2). El `main` imprime la asignación antes y después de cada cambio.

---

**Ejercicio 6 — Consumer group rebalancing con listener**

Extiende el ejercicio anterior añadiendo un mecanismo de notificación: define la interfaz `RebalanceListener` con `onPartitionsRevoked(String consumerId, List<Integer> partitions)` y `onPartitionsAssigned(String consumerId, List<Integer> partitions)`. Al rebalancear, el `ConsumerGroup` notifica primero la revocación de las asignaciones actuales y luego las nuevas asignaciones. Implementa `OffsetCommitListener` que, al recibir la revocación, hace un "commit" de los offsets actuales (simulados como `Map<Integer, Long>`) antes de que las particiones sean reasignadas. El `main` parte de 2 consumers, añade un tercero y elimina uno, mostrando los commits automáticos durante cada rebalanceo.

---
