import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Patrones de Spring Kafka implementados con Java puro.
 *
 * Cada bloque muestra el patrón Spring Kafka en comentarios
 * y debajo su equivalente en Java estándar que demuestra el mismo concepto.
 *
 * Patrones cubiertos:
 *  1. KafkaTemplate.send() con callback de confirmación
 *  2. @KafkaListener con groupId
 *  3. Manual commit con Acknowledgment
 *  4. @RetryableTopic y Dead Letter Topic (DLT)
 *  5. Error handling con DeadLetterPublishingRecoverer
 */
public class ExpKafkaPatterns {

    // ─────────────────────────────────────────────
    // INFRAESTRUCTURA SIMULADA
    // ─────────────────────────────────────────────

    record Mensaje(String topic, String key, String valor, int intentos) {
        Mensaje incrementarIntento() {
            return new Mensaje(topic, key, valor, intentos + 1);
        }
    }

    static class BrokerSimulado {
        private final Map<String, Queue<Mensaje>> topics = new ConcurrentHashMap<>();

        void publicar(String topic, String key, String valor) {
            topics.computeIfAbsent(topic, k -> new LinkedList<>())
                  .add(new Mensaje(topic, key, valor, 0));
        }

        Optional<Mensaje> poll(String topic) {
            Queue<Mensaje> q = topics.get(topic);
            return q == null ? Optional.empty() : Optional.ofNullable(q.poll());
        }

        void publicarMensaje(Mensaje msg) {
            topics.computeIfAbsent(msg.topic(), k -> new LinkedList<>()).add(msg);
        }

        boolean hayMensajes(String topic) {
            Queue<Mensaje> q = topics.get(topic);
            return q != null && !q.isEmpty();
        }
    }

    // ─────────────────────────────────────────────
    // PATRÓN 1: KafkaTemplate.send() con callback
    // ─────────────────────────────────────────────

    static class KafkaTemplateSimulado {
        private final BrokerSimulado broker;

        KafkaTemplateSimulado(BrokerSimulado broker) {
            this.broker = broker;
        }

        /*
         * Spring Kafka:
         *   kafkaTemplate.send("pedidos", key, mensaje)
         *     .whenComplete((result, ex) -> {
         *         if (ex != null) log.error("Error enviando", ex);
         *         else log.info("Enviado a partition={} offset={}",
         *                  result.getRecordMetadata().partition(),
         *                  result.getRecordMetadata().offset());
         *     });
         */
        CompletableFuture<String> enviar(String topic, String key, String valor) {
            return CompletableFuture.supplyAsync(() -> {
                broker.publicar(topic, key, valor);
                String resultado = String.format("partition=0 offset=%d", (int)(Math.random() * 1000));
                return resultado;
            }).whenComplete((resultado, ex) -> {
                if (ex != null) {
                    System.out.printf("[KafkaTemplate] ERROR enviando a '%s': %s%n", topic, ex.getMessage());
                } else {
                    System.out.printf("[KafkaTemplate] ENVIADO a topic='%s' key='%s' → %s%n",
                            topic, key, resultado);
                }
            });
        }
    }

    // ─────────────────────────────────────────────
    // PATRÓN 2: @KafkaListener básico
    // ─────────────────────────────────────────────

    /*
     * Spring Kafka:
     *   @KafkaListener(topics = "pedidos", groupId = "procesador",
     *                  containerFactory = "kafkaListenerContainerFactory")
     *   public void escuchar(String mensaje) {
     *       procesarPedido(mensaje);
     *   }
     *
     * Equivalente Java puro: poll loop registrado en un executor
     */
    static class ListenerSimulado implements Runnable {
        private final String topic;
        private final String groupId;
        private final BrokerSimulado broker;
        private final Consumer<Mensaje> handler;
        private volatile boolean activo = true;

        ListenerSimulado(String topic, String groupId, BrokerSimulado broker, Consumer<Mensaje> handler) {
            this.topic = topic;
            this.groupId = groupId;
            this.broker = broker;
            this.handler = handler;
        }

        @Override
        public void run() {
            System.out.printf("[KafkaListener] group='%s' escuchando topic='%s'%n", groupId, topic);
            while (activo && broker.hayMensajes(topic)) {
                broker.poll(topic).ifPresent(msg -> {
                    System.out.printf("[KafkaListener] Recibido: %s%n", msg.valor());
                    handler.accept(msg);
                });
            }
        }

        void detener() { activo = false; }
    }

    // ─────────────────────────────────────────────
    // PATRÓN 3: Manual commit con Acknowledgment
    // ─────────────────────────────────────────────

    /*
     * Spring Kafka (AckMode.MANUAL):
     *   @KafkaListener(topics = "pedidos")
     *   public void escuchar(String mensaje, Acknowledgment ack) {
     *       try {
     *           procesarPedido(mensaje);
     *           ack.acknowledge(); // commit manual
     *       } catch (Exception e) {
     *           // NO hacer ack → el mensaje se reprocesará
     *       }
     *   }
     */
    static class ManualAckListener {
        private final BrokerSimulado broker;
        private final Map<String, Integer> offsetsCommitted = new HashMap<>();

        ManualAckListener(BrokerSimulado broker) {
            this.broker = broker;
        }

        void procesarConAck(String topic) {
            System.out.printf("%n[ManualAck] Procesando topic='%s' con commit manual:%n", topic);
            int offset = 0;
            Optional<Mensaje> msgOpt;
            while ((msgOpt = broker.poll(topic)).isPresent()) {
                Mensaje msg = msgOpt.get();
                try {
                    // Simular procesamiento que puede fallar
                    if (msg.valor().contains("FALLA")) {
                        throw new RuntimeException("Error procesando: " + msg.valor());
                    }
                    System.out.printf("  Procesado: '%s'%n", msg.valor());
                    // ack.acknowledge() — commit manual del offset
                    offsetsCommitted.put(topic, offset);
                    System.out.printf("  [ACK] Offset %d committed%n", offset);
                } catch (Exception e) {
                    System.out.printf("  [NO ACK] Error en offset %d: %s → mensaje pendiente de reproceso%n",
                            offset, e.getMessage());
                }
                offset++;
            }
        }
    }

    // ─────────────────────────────────────────────
    // PATRÓN 4: @RetryableTopic y Dead Letter Topic
    // ─────────────────────────────────────────────

    /*
     * Spring Kafka:
     *   @RetryableTopic(
     *       attempts = "3",
     *       backoff = @Backoff(delay = 1000, multiplier = 2.0),
     *       dltTopicSuffix = "-dlt"
     *   )
     *   @KafkaListener(topics = "pedidos")
     *   public void escuchar(String mensaje) {
     *       if (mensaje.contains("ERROR")) throw new RuntimeException("Fallo");
     *   }
     *   // Si falla 3 veces → va automáticamente a "pedidos-dlt"
     *
     *   @KafkaListener(topics = "pedidos-dlt")
     *   public void handleDlt(String mensaje) {
     *       log.error("Mensaje en DLT: {}", mensaje);
     *       // alertar, almacenar para revisión manual, etc.
     *   }
     */
    static class RetryableTopicSimulado {
        private static final int MAX_INTENTOS = 3;
        private final BrokerSimulado broker;

        RetryableTopicSimulado(BrokerSimulado broker) {
            this.broker = broker;
        }

        void procesarConRetry(String topic) {
            String dltTopic = topic + "-dlt";
            System.out.printf("%n[RetryableTopic] Procesando topic='%s' (max %d intentos, DLT='%s'):%n",
                    topic, MAX_INTENTOS, dltTopic);

            Optional<Mensaje> msgOpt;
            while ((msgOpt = broker.poll(topic)).isPresent()) {
                Mensaje msg = msgOpt.get();
                procesarConRetryRecursivo(msg, dltTopic);
            }

            // Procesar DLT
            if (broker.hayMensajes(dltTopic)) {
                System.out.printf("%n[DLT Handler] Mensajes en dead-letter topic '%s':%n", dltTopic);
                while ((msgOpt = broker.poll(dltTopic)).isPresent()) {
                    Mensaje dltMsg = msgOpt.get();
                    System.out.printf("  [DLT] Revisión manual requerida: '%s' (falló %d veces)%n",
                            dltMsg.valor(), dltMsg.intentos());
                }
            }
        }

        private void procesarConRetryRecursivo(Mensaje msg, String dltTopic) {
            if (msg.intentos() >= MAX_INTENTOS) {
                System.out.printf("  [DLT] Enviando a dead-letter tras %d intentos fallidos: '%s'%n",
                        msg.intentos(), msg.valor());
                broker.publicarMensaje(new Mensaje(dltTopic, msg.key(), msg.valor(), msg.intentos()));
                return;
            }

            try {
                System.out.printf("  Intento %d/%d para: '%s'%n",
                        msg.intentos() + 1, MAX_INTENTOS, msg.valor());
                if (msg.valor().contains("ERROR")) {
                    throw new RuntimeException("Procesamiento fallido");
                }
                System.out.printf("  OK: '%s' procesado correctamente%n", msg.valor());
            } catch (Exception e) {
                System.out.printf("  RETRY: fallo en intento %d — %s%n", msg.intentos() + 1, e.getMessage());
                // Backoff simulado (en Spring Kafka sería delay real entre topics de retry)
                procesarConRetryRecursivo(msg.incrementarIntento(), dltTopic);
            }
        }
    }

    // ─────────────────────────────────────────────
    // MAIN: demo de todos los patrones
    // ─────────────────────────────────────────────

    public static void main(String[] args) throws Exception {

        BrokerSimulado broker = new BrokerSimulado();

        // ── Patrón 1: KafkaTemplate con callback ──────────────────────
        System.out.println("═".repeat(60));
        System.out.println("  PATRÓN 1: KafkaTemplate.send() con callback");
        System.out.println("═".repeat(60));
        KafkaTemplateSimulado template = new KafkaTemplateSimulado(broker);
        template.enviar("pedidos", "usuario-1", "pedido-ABC").get();
        template.enviar("pedidos", "usuario-2", "pedido-DEF").get();

        // ── Patrón 2: @KafkaListener básico ───────────────────────────
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  PATRÓN 2: @KafkaListener básico (poll loop)");
        System.out.println("═".repeat(60));
        ListenerSimulado listener = new ListenerSimulado("pedidos", "procesador", broker,
                msg -> System.out.printf("  [Handler] Procesando pedido: %s%n", msg.valor()));
        listener.run();

        // ── Patrón 3: Manual Acknowledgment ───────────────────────────
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  PATRÓN 3: Manual commit (Acknowledgment)");
        System.out.println("═".repeat(60));
        broker.publicar("pagos", "tx-1", "pago-OK-100€");
        broker.publicar("pagos", "tx-2", "pago-FALLA-200€");
        broker.publicar("pagos", "tx-3", "pago-OK-50€");
        ManualAckListener ackListener = new ManualAckListener(broker);
        ackListener.procesarConAck("pagos");

        // ── Patrón 4: RetryableTopic + DLT ────────────────────────────
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  PATRÓN 4: @RetryableTopic + Dead Letter Topic");
        System.out.println("═".repeat(60));
        broker.publicar("notificaciones", "n-1", "notif-OK");
        broker.publicar("notificaciones", "n-2", "notif-ERROR-fallo-externo");
        broker.publicar("notificaciones", "n-3", "notif-OK-final");
        RetryableTopicSimulado retryable = new RetryableTopicSimulado(broker);
        retryable.procesarConRetry("notificaciones");

        System.out.println("\n" + "═".repeat(60));
        System.out.println("  RESUMEN DE PATRONES");
        System.out.println("═".repeat(60));
        System.out.println("  KafkaTemplate.send()  → fire-and-forget con callback async");
        System.out.println("  @KafkaListener        → poll loop gestionado por el framework");
        System.out.println("  Acknowledgment.ack()  → commit manual (at-least-once controlado)");
        System.out.println("  @RetryableTopic       → reintentos automáticos + DLT final");
        System.out.println("═".repeat(60));
    }
}
