import java.util.*;

/**
 * Simulación de exactly-once semantics en Kafka con Java puro.
 *
 * Niveles de garantía demostrados:
 *  - At-least-once: puede haber duplicados si el producer hace retry tras un fallo
 *  - Idempotent producer: el broker deduplicaba por (producerId, partition, sequenceNumber)
 *  - Exactly-once (transaccional): produce + commitOffset en una transacción atómica
 *
 * Patrón read-process-write: consume de topicA, procesa, produce a topicB,
 * commit de offset — todo atómicamente.
 */
public class ExpExactlyOnce {

    // ─────────────────────────────────────────────
    // BROKER SIMULADO con deduplicación por sequence
    // ─────────────────────────────────────────────

    static class Broker {
        // topicPartition → lista de mensajes recibidos
        private final Map<String, List<String>> topics = new LinkedHashMap<>();

        // (producerId, topicPartition) → último sequence aceptado
        private final Map<String, Integer> sequenceTracker = new HashMap<>();

        // transactionId → estado de la transacción en curso
        private final Map<String, String> transacciones = new HashMap<>();

        void publicar(String topicPartition, String mensaje) {
            topics.computeIfAbsent(topicPartition, k -> new ArrayList<>()).add(mensaje);
        }

        // Publicación idempotente: el broker rechaza si ya vio este (producerId, seq)
        boolean publicarIdempotente(String producerId, String topicPartition,
                                    int sequenceNum, String mensaje) {
            String clave = producerId + "|" + topicPartition;
            Integer ultimoSeq = sequenceTracker.get(clave);

            if (ultimoSeq != null && sequenceNum <= ultimoSeq) {
                // Duplicado detectado: ya se procesó este sequence
                System.out.printf("  [Broker] DUPLICADO IGNORADO pid='%s' seq=%d (último aceptado=%d) → '%s'%n",
                        producerId, sequenceNum, ultimoSeq, mensaje);
                return false;
            }

            // Sequence número fuera de orden: gap demasiado grande
            if (ultimoSeq != null && sequenceNum > ultimoSeq + 1) {
                System.out.printf("  [Broker] ERROR: gap en sequence pid='%s' esperado=%d recibido=%d%n",
                        producerId, ultimoSeq + 1, sequenceNum);
                return false;
            }

            sequenceTracker.put(clave, sequenceNum);
            topics.computeIfAbsent(topicPartition, k -> new ArrayList<>()).add(mensaje);
            System.out.printf("  [Broker] ACEPTADO pid='%s' seq=%d → '%s' en '%s'%n",
                    producerId, sequenceNum, mensaje, topicPartition);
            return true;
        }

        // Operaciones transaccionales
        void beginTransaction(String transactionId) {
            transacciones.put(transactionId, "OPEN");
            System.out.printf("  [Broker] TX '%s' BEGIN%n", transactionId);
        }

        // Commit: hace efectivas todas las escrituras del batch transaccional
        void commitTransaction(String transactionId, List<String[]> writes, Map<String, Integer> offsets) {
            if (!"OPEN".equals(transacciones.get(transactionId))) {
                throw new IllegalStateException("No hay transacción abierta: " + transactionId);
            }
            // Aplicar escrituras atómicamente
            for (String[] w : writes) {
                topics.computeIfAbsent(w[0], k -> new ArrayList<>()).add(w[1]);
                System.out.printf("  [Broker] TX WRITE → '%s': %s%n", w[0], w[1]);
            }
            // Commit de offsets junto con las escrituras (atómico)
            offsets.forEach((group, offset) ->
                System.out.printf("  [Broker] TX COMMIT OFFSET group='%s' offset=%d%n", group, offset));
            transacciones.put(transactionId, "COMMITTED");
            System.out.printf("  [Broker] TX '%s' COMMITTED ✓%n", transactionId);
        }

        void abortTransaction(String transactionId) {
            transacciones.put(transactionId, "ABORTED");
            System.out.printf("  [Broker] TX '%s' ABORTED — ninguna escritura es visible%n", transactionId);
        }

        void mostrarTopics() {
            System.out.println("  Estado de topics en el broker:");
            if (topics.isEmpty()) {
                System.out.println("    (vacío)");
                return;
            }
            topics.forEach((topic, msgs) ->
                System.out.printf("    %-25s → %s%n", topic, msgs));
        }
    }

    // ─────────────────────────────────────────────
    // AT-LEAST-ONCE PRODUCER: simula duplicados en retry
    // ─────────────────────────────────────────────

    static class AtLeastOnceProducer {
        private final Broker broker;
        private final String producerId;

        AtLeastOnceProducer(Broker broker, String producerId) {
            this.broker = broker;
            this.producerId = producerId;
        }

        // Simula crash: el ACK del broker se pierde y el producer reintenta
        void enviarConFallo(String topic, String mensaje) {
            System.out.printf("  [AtLeastOnce '%s'] Enviando '%s'...%n", producerId, mensaje);
            broker.publicar(topic, mensaje); // llega al broker

            // Simula: ACK se pierde en la red → producer no sabe si llegó
            System.out.printf("  [AtLeastOnce '%s'] ACK perdido — reintentando...%n", producerId);
            broker.publicar(topic, mensaje); // DUPLICADO: el broker lo acepta de nuevo

            System.out.printf("  [AtLeastOnce '%s'] Reintento enviado (posible duplicado)%n", producerId);
        }
    }

    // ─────────────────────────────────────────────
    // IDEMPOTENT PRODUCER: evita duplicados con sequence numbers
    // ─────────────────────────────────────────────

    static class IdempotentProducer {
        private final Broker broker;
        private final String producerId;
        // partition → próximo sequence number
        private final Map<String, Integer> sequences = new HashMap<>();

        IdempotentProducer(Broker broker, String producerId) {
            this.broker = broker;
            this.producerId = producerId;
        }

        // Asigna sequence number único por (producerId, topicPartition)
        void enviar(String topicPartition, String mensaje) {
            int seq = sequences.merge(topicPartition, 0, (old, zero) -> old + 1);
            System.out.printf("  [Idempotent '%s'] Enviando seq=%d '%s'...%n",
                    producerId, seq, mensaje);
            broker.publicarIdempotente(producerId, topicPartition, seq, mensaje);
        }

        // Simula crash y retry: mismo mensaje con mismo sequence → broker deduplica
        void enviarConFalloYRetry(String topicPartition, String mensaje) {
            int seq = sequences.merge(topicPartition, 0, (old, zero) -> old + 1);
            System.out.printf("  [Idempotent '%s'] Enviando seq=%d '%s'...%n",
                    producerId, seq, mensaje);
            broker.publicarIdempotente(producerId, topicPartition, seq, mensaje);

            // ACK perdido → retry con el MISMO sequence number
            System.out.printf("  [Idempotent '%s'] ACK perdido — reintentando con mismo seq=%d...%n",
                    producerId, seq);
            // El broker rechaza: ya procesó este sequence
            broker.publicarIdempotente(producerId, topicPartition, seq, mensaje);
        }
    }

    // ─────────────────────────────────────────────
    // TRANSACTIONAL PRODUCER: exactly-once con transacciones
    // ─────────────────────────────────────────────

    static class TransactionalProducer {
        private final Broker broker;
        private final String transactionId;
        private final List<String[]> writes = new ArrayList<>(); // [topic, mensaje]
        private final Map<String, Integer> offsetsACommitar = new LinkedHashMap<>();
        private boolean txAbierta = false;

        TransactionalProducer(Broker broker, String transactionId) {
            this.broker = broker;
            this.transactionId = transactionId;
        }

        void beginTransaction() {
            writes.clear();
            offsetsACommitar.clear();
            txAbierta = true;
            broker.beginTransaction(transactionId);
        }

        // Produce dentro de la transacción (buffered, no visible hasta commit)
        void send(String topic, String mensaje) {
            if (!txAbierta) throw new IllegalStateException("No hay transacción activa");
            writes.add(new String[]{topic, mensaje});
            System.out.printf("  [Transactional] Buffered → '%s': %s%n", topic, mensaje);
        }

        // Incluir commit de offset en la misma transacción (read-process-write atómico)
        void sendOffsetsToTransaction(String consumerGroup, int offset) {
            offsetsACommitar.put(consumerGroup, offset);
            System.out.printf("  [Transactional] Buffered offset commit → group='%s' offset=%d%n",
                    consumerGroup, offset);
        }

        void commitTransaction() {
            broker.commitTransaction(transactionId, writes, offsetsACommitar);
            txAbierta = false;
        }

        void abortTransaction() {
            broker.abortTransaction(transactionId);
            writes.clear();
            offsetsACommitar.clear();
            txAbierta = false;
        }
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  EXACTLY-ONCE SEMANTICS — Java puro");
        System.out.println("═".repeat(65));

        // ── Fase 1: At-least-once — duplicados visibles ────────────────
        System.out.println("\n─ FASE 1: At-least-once (duplicados con retry) ─");
        Broker broker1 = new Broker();
        AtLeastOnceProducer aloProducer = new AtLeastOnceProducer(broker1, "producer-1");
        aloProducer.enviarConFallo("pedidos-P0", "pedido-AAA");
        aloProducer.enviarConFallo("pedidos-P0", "pedido-BBB");
        System.out.println();
        broker1.mostrarTopics();
        System.out.println("  → pedido-AAA y pedido-BBB aparecen 2 veces cada uno (duplicados)");

        // ── Fase 2: Idempotent producer — deduplicación por sequence ──
        System.out.println("\n─ FASE 2: Idempotent producer (no hay duplicados) ─");
        Broker broker2 = new Broker();
        IdempotentProducer idempotentProducer = new IdempotentProducer(broker2, "producer-2");
        idempotentProducer.enviar("pedidos-P0", "pedido-CCC");
        idempotentProducer.enviarConFalloYRetry("pedidos-P0", "pedido-DDD"); // retry → ignorado
        idempotentProducer.enviar("pedidos-P0", "pedido-EEE");
        System.out.println();
        broker2.mostrarTopics();
        System.out.println("  → pedido-DDD aparece exactamente 1 vez (retry deduplicado por broker)");

        // ── Fase 3: Transaccional — read-process-write atómico ────────
        System.out.println("\n─ FASE 3: Exactly-once transaccional (read-process-write) ─");
        Broker broker3 = new Broker();
        TransactionalProducer txProducer = new TransactionalProducer(broker3, "tx-producer-1");

        // TX exitosa: consume de 'input', procesa, produce a 'output', commit offset
        System.out.println("  [TX 1: exitosa]");
        txProducer.beginTransaction();
        txProducer.send("output-topic-P0", "resultado-de-pedido-FFF");
        txProducer.sendOffsetsToTransaction("consumer-group-1", 5);
        txProducer.commitTransaction();

        // TX abortada: simula crash a mitad — nada es visible
        System.out.println("\n  [TX 2: abortada por crash a mitad]");
        txProducer.beginTransaction();
        txProducer.send("output-topic-P0", "resultado-INCOMPLETO-GGG");
        txProducer.sendOffsetsToTransaction("consumer-group-1", 6);
        txProducer.abortTransaction(); // crash simulado

        System.out.println();
        broker3.mostrarTopics();
        System.out.println("  → resultado-INCOMPLETO-GGG NO aparece: la TX fue abortada");
        System.out.println("  → output-topic contiene solo 'resultado-de-pedido-FFF' (TX committed)");

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN GARANTÍAS");
        System.out.println("═".repeat(65));
        System.out.println("  At-least-once   → duplicados posibles. Requiere idempotencia en consumer.");
        System.out.println("  Idempotent      → enable.idempotence=true. Deduplica por (pid, seq).");
        System.out.println("  Exactly-once    → transactional.id + isolation.level=read_committed.");
        System.out.println("                    produce + commitOffset en una sola transacción atómica.");
        System.out.println("  Coste: exactamente-once tiene mayor latencia (2-phase commit en broker).");
        System.out.println("═".repeat(65));
    }
}
