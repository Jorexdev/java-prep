# Ejercicios — 38 Kafka

## Fácil

**Ejercicio 1 — Producer con callback**

Crea una clase `Topic` que encapsula un nombre y una `Queue<String>` interna. Crea una clase `KafkaProducer` con un método `send(String topic, String message, Callback callback)`. Define la interfaz funcional `Callback` con `onCompletion(boolean success, Exception ex)`. En el `main`, envía 5 mensajes; simula 1 fallo (el tercer mensaje) y el callback debe imprimir confirmación o error según el resultado.

---

**Ejercicio 2 — Consumer poll loop**

Crea una clase `KafkaConsumer` con `subscribe(String topic)` y `poll()` que devuelve `List<ConsumerRecord<String>>`. La clase `ConsumerRecord` tiene campos `topic`, `partition`, `offset` y `value`. En el `main`, produce 10 mensajes al topic, luego el consumer hace poll en bucle hasta que no queda nada, imprimiendo cada record con su offset.

---

**Ejercicio 3 — Consumer Group**

Crea una clase `ConsumerGroup` con 3 consumers y un `Topic` con 3 particiones (3 colas internas). Al suscribirse, el grupo asigna una partición por consumer usando round-robin. Produce 9 mensajes distribuyendo por partición (`índice % 3`). Cada consumer hace poll de su partición asignada. En el `main`, muestra qué consumer recibe qué mensajes.

---

**Ejercicio 4 — Offset management**

Crea una clase `ConsumerWithOffsets` que mantiene un `Map<Integer, Long>` de `partición → offset comprometido`. Implementa `commitOffset(int partition, long offset)` y `getLastCommittedOffset(int partition)`. Simula un crash: el consumer lee hasta offset 5, hace commit, "crashea" (nueva instancia), y retoma desde el offset comprometido + 1. En el `main`, demuestra at-least-once delivery con la recuperación tras el crash.

---

**Ejercicio 5 — Partición por key**

Crea una clase `Partitioner` con `getPartition(String key, int numPartitions)` usando `Math.abs(key.hashCode()) % numPartitions`. Crea `PartitionedProducer` que usa el partitioner. En el `main`, envía 12 mensajes con 4 keys distintas (`"usuario"`, `"pedido"`, `"producto"`, `"envio"`) a un topic de 3 particiones e imprime en qué partición acaba cada mensaje.

---

**Ejercicio 6 — Serialización**

Define la interfaz `Serializer<T>` con `byte[] serialize(T obj)` y `Deserializer<T>` con `T deserialize(byte[] data)`. Crea `Pedido(int id, String producto, double precio)`. Implementa `JsonSerializer<Pedido>` que usa el formato `id|producto|precio` sin librerías externas. En el `main`, serializa un pedido, imprime los bytes en hexadecimal, deserializa y verifica que los campos coinciden con el original.
