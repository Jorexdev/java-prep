<div align="center">
  <a href="#"><img src="../../assets/modules/banner-38-kafka-v1.svg" width="100%" alt=""/></a>
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

Kafka es un **log distribuido e inmutable**. Los mensajes se escriben al final del log y los consumidores los leen avanzando su propio puntero (offset). A diferencia de las colas tradicionales, **los mensajes no se borran al consumir** — permanecen durante el tiempo de retención configurado, lo que permite replay y múltiples consumers independientes leyendo el mismo topic.

La unidad de organización es el **topic**, dividido en **particiones** (shards paralelos). Cada mensaje dentro de una partición tiene un **offset** secuencial e inmutable. El flujo es siempre: `Producer → Broker (líder de partición) → Consumer`.

El commit log actúa como **fuente de verdad**: cualquier estado del sistema puede reconstruirse reproduciendo los eventos del log desde el inicio. Esto lo diferencia fundamentalmente de un sistema de colas donde el mensaje desaparece al ser procesado.

```
Topic "pedidos" con 3 particiones:
  Partition 0: [offset 0: pedido-A] [offset 1: pedido-D] [offset 2: pedido-G]
  Partition 1: [offset 0: pedido-B] [offset 1: pedido-E] [offset 2: pedido-H]
  Partition 2: [offset 0: pedido-C] [offset 1: pedido-F] [offset 2: pedido-I]

Consumer Group "procesador" (3 consumers):
  Consumer-1 → Partition 0 (offset actual: 2)
  Consumer-2 → Partition 1 (offset actual: 1)
  Consumer-3 → Partition 2 (offset actual: 2)
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Topics y particiones**

Las particiones son la unidad de paralelismo. Más particiones = más consumers en paralelo (hasta el número de particiones). Los mensajes con la misma key siempre van a la misma partición, garantizando orden por key. Sin key, la distribución es round-robin entre particiones.

**Consumer Groups**

Cada consumer group lee el topic de forma independiente. Dentro de un grupo, cada partición es asignada a exactamente un consumer. Si hay más consumers que particiones, los consumers sobrantes quedan inactivos. Si un consumer cae, Kafka hace **rebalance** y redistribuye las particiones entre los consumers restantes.

**Offsets y semánticas de entrega**

- `at-most-once`: commit antes de procesar. Si el proceso falla, el mensaje se pierde.
- `at-least-once`: commit después de procesar. Si el proceso falla antes del commit, el mensaje se re-procesa (posibles duplicados).
- `exactly-once`: requiere transacciones Kafka + idempotencia en el producer. Garantía más fuerte pero mayor overhead.

**Producers**

```java
// acks=0 → el producer no espera confirmación del broker (máximo throughput, puede perder mensajes)
// acks=1 → espera confirmación del líder (riesgo de pérdida si el líder cae antes de replicar)
// acks=all → espera confirmación de todos los réplicas en ISR (máxima durabilidad)

// Idempotencia: enable.idempotence=true → el producer numera sus mensajes,
// el broker descarta duplicados si el producer reintenta

// batch.size=16384 (bytes) → acumula mensajes antes de enviar
// linger.ms=5 → espera hasta 5ms para llenar el batch (throughput vs latencia)
```

**Consumers**

```java
// Poll loop estándar:
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        procesar(record);
    }
    consumer.commitSync(); // manual commit → at-least-once
}

// auto.commit.enable=true (default) → at-most-once, commit periódico en background
```

**Serialización**

- `StringSerializer/StringDeserializer`: para mensajes de texto plano
- `JsonSerializer`: serializa POJOs a JSON (Spring Kafka)
- `KafkaAvroSerializer`: Avro binario + Schema Registry para validación de esquema en tiempo de producción/consumo

**Spring Kafka**

```java
// Producer
@Autowired KafkaTemplate<String, String> kafkaTemplate;
kafkaTemplate.send("pedidos", key, mensaje)
    .whenComplete((result, ex) -> {
        if (ex != null) log.error("Error enviando", ex);
        else log.info("Enviado a offset {}", result.getRecordMetadata().offset());
    });

// Consumer
@KafkaListener(topics = "pedidos", groupId = "procesador")
public void escuchar(String mensaje, Acknowledgment ack) {
    procesar(mensaje);
    ack.acknowledge(); // manual commit
}
```

**Retries y Dead Letter Topics**

```java
@RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000))
@KafkaListener(topics = "pedidos")
public void escuchar(String mensaje) { ... }
// Si falla 3 veces → mensajes van automáticamente a "pedidos-dlt"
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Throughput masivo** — Kafka escala a millones de mensajes por segundo gracias a escritura secuencial en disco, batching y zero-copy (sendfile syscall). Es órdenes de magnitud más rápido que una base de datos o cola tradicional para flujos de eventos.

**Retención configurable** — Los mensajes no desaparecen al consumirse. Pueden conservarse por tiempo (7 días por defecto) o por tamaño. Esto permite que nuevos consumers lean histórico o que un consumer retrase su procesamiento sin perder datos.

**Replay de eventos** — Cualquier consumer puede hacer `seek(offset)` a cualquier punto del pasado y reprocesar eventos. Útil para debugging, migración de datos, reconstrucción de estado o incorporar un nuevo servicio que necesita el histórico.

**Desacoplamiento productor/consumidor** — El producer no sabe nada del consumer. Pueden escalar, desplegarse y fallar de forma completamente independiente. El broker actúa como buffer infinito entre ambos.

Ver [ExpKafkaSimulation.java](ExpKafkaSimulation.java), [ExpConsumerGroups.java](ExpConsumerGroups.java), [ExpPartitioning.java](ExpPartitioning.java), [ExpExactlyOnce.java](ExpExactlyOnce.java), [ExpKafkaPatterns.java](ExpKafkaPatterns.java) y [ExpSchemaRegistry.java](ExpSchemaRegistry.java) para ejemplos ejecutables con producers, consumers, particionado, semánticas de entrega y Schema Registry.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
