<div align="center">
  <a href="#"><img src="../../assets/modules/banner-38-kafka-v1.svg" width="100%" alt=""/></a>
</div>
<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a></div>
<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Qué es un offset en Kafka y qué diferencia hay entre at-least-once y exactly-once?**

El offset es el índice secuencial e inmutable de un mensaje dentro de una partición. El consumer group almacena su offset actual en el topic interno `__consumer_offsets`, lo que permite retomar el procesamiento tras un reinicio sin perder mensajes.

- `at-least-once`: el consumer hace commit del offset solo después de procesar el mensaje. Si falla antes del commit, Kafka re-entregará el mensaje. El consumer puede recibir el mismo mensaje más de una vez — hay que diseñarlo para ser idempotente.
- `exactly-once`: combina producers idempotentes (`enable.idempotence=true`) con transacciones Kafka. El broker numera las escrituras del producer y descarta duplicados. Garantiza que cada mensaje se procesa exactamente una vez, pero requiere que toda la pipeline (producer → broker → consumer → sink) participe en la transacción.

---

**¿Cómo funciona un consumer group? ¿Qué pasa si hay más consumers que particiones?**

Un consumer group es un conjunto de consumers que cooperan para leer un topic. Kafka asegura que **cada partición es asignada a exactamente un consumer del grupo** en cada momento. Si el grupo tiene 3 consumers y el topic tiene 3 particiones, cada consumer lee una partición. Si el grupo tiene 5 consumers y el topic tiene 3 particiones, 2 consumers quedan sin partición asignada y permanecen inactivos — no procesan ningún mensaje. Si un consumer activo falla, Kafka desencadena un **rebalance**: reasigna las particiones huérfanas a los consumers disponibles. Múltiples grupos independientes pueden leer el mismo topic simultáneamente, cada uno con sus propios offsets.

---

**¿Qué significa `acks=all` en un producer y qué garantías da?**

`acks=all` (equivalente a `acks=-1`) indica que el broker líder espera a que **todos los réplicas en el ISR** (In-Sync Replicas) confirmen haber escrito el mensaje antes de responder al producer. Esto garantiza que aunque el líder falle inmediatamente después del ack, al menos una réplica tiene el mensaje. Combinado con `min.insync.replicas=2` se asegura que al menos 2 brokers tienen el mensaje antes del ack. Es la configuración más segura en durabilidad, con la contrapartida de mayor latencia de escritura.

---

**¿Cuándo usarías Kafka en lugar de una cola como RabbitMQ?**

Kafka cuando: necesitas alto throughput (millones de eventos/segundo), retención de mensajes para replay, múltiples consumers independientes leyendo el mismo stream, orden garantizado por key, o event sourcing/audit trail. RabbitMQ cuando: necesitas routing complejo (exchange con bindings, dead-letter, priority queues), el mensaje debe desaparecer tras consumirse, el volumen es moderado, o necesitas RPC request-reply. RabbitMQ tiene menor latencia para mensajes individuales; Kafka escala mejor en volumen y es más adecuado para arquitecturas event-driven.

---

**¿Qué es un Dead Letter Topic y para qué sirve?**

Un Dead Letter Topic (DLT) es un topic especial donde van los mensajes que no pudieron procesarse después de agotar todos los reintentos. Evita que un mensaje "envenenado" bloquee el procesamiento del resto de la partición. En Spring Kafka, `@RetryableTopic` crea automáticamente topics intermedios de retry y el DLT final. Los mensajes en el DLT pueden inspeccionarse manualmente, reinjectarse al topic original tras corregir el bug, o archivarse para análisis. Sin DLT, un mensaje que siempre lanza excepción haría que el consumer se quede en bucle reintentando indefinidamente.

---

**¿Cómo evitas que mensajes duplicados causen problemas (idempotencia en el consumer)?**

Estrategias de idempotencia en el consumer: (1) **Idempotency key en base de datos**: el mensaje lleva un ID único; antes de procesar se hace `INSERT ... ON CONFLICT DO NOTHING` o se verifica si ya fue procesado. (2) **Operaciones naturalmente idempotentes**: `PUT` en lugar de `POST`, `SET saldo=X` en lugar de `incrementar saldo en X`. (3) **Upsert en lugar de insert**: actualizar si ya existe, crear si no. (4) **Tabla de mensajes procesados**: almacenar el offset o message-id en la misma transacción que el efecto del negocio (requiere transacción atómica entre el sink y el registro de offset). El enfoque correcto depende del contexto, pero lo clave es: **no asumir que cada mensaje llega exactamente una vez** aunque uses `acks=all`.

---

**¿Qué es el Schema Registry y por qué es importante con Avro?**

El Schema Registry (Confluent) es un servicio centralizado que almacena los esquemas Avro/Protobuf/JSON Schema de los mensajes Kafka. El producer registra el esquema la primera vez y solo envía el **schema ID** (4 bytes) + payload binario, no el esquema completo. El consumer descarga el esquema del Registry por ID. Ventajas: (1) **Validación en tiempo de producción**: si el producer intenta enviar un mensaje incompatible con el esquema registrado, el `KafkaAvroSerializer` lanza excepción antes de enviar. (2) **Evolución de esquemas**: el Registry valida que los cambios sean compatibles (backwards/forwards). (3) **Mensajes más pequeños**: Avro binario sin nombres de campo. Sin Schema Registry, Avro no tiene sentido ya que el consumer no sabría cómo deserializar el payload.


<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>
<div align="center"><a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a></div>
