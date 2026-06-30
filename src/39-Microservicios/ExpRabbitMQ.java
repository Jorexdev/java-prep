import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

// Simula RabbitMQ con Java puro — sin cliente AMQP ni Spring AMQP.
//
// Arquitectura AMQP:
//   Publisher → Exchange → (binding + routing key) → Queue → Consumer
//
// Los 4 tipos de exchange:
//   Direct  → routing key exacta (un mensaje a una cola específica)
//   Fanout  → ignora la routing key; envía a TODAS las queues enlazadas
//   Topic   → routing key con wildcards (* = una palabra, # = cero o más)
//   Headers → enruta por atributos de cabecera, no por routing key
public class ExpRabbitMQ {

    // ── Message ───────────────────────────────────────────────────────────────

    static class Message {
        private final String     id;
        private final String     body;
        private final String     routingKey;
        private final Map<String, String> headers;
        private       boolean    acked  = false;
        private       boolean    nacked = false;
        private       int        deliveryAttempt = 1;

        Message(String body, String routingKey, Map<String, String> headers) {
            this.id         = "msg-" + UUID.randomUUID().toString().substring(0, 8);
            this.body       = body;
            this.routingKey = routingKey;
            this.headers    = headers != null ? headers : Map.of();
        }

        Message(String body, String routingKey) {
            this(body, routingKey, null);
        }

        // ACK: mensaje procesado correctamente → se elimina de la queue
        void ack() {
            acked = true;
            System.out.printf("  [ACK]  %s procesado%n", id);
        }

        // NACK requeue=true: fallo temporal → vuelve al frente de la queue
        // NACK requeue=false: fallo permanente → va a DLQ si está configurada
        void nack(boolean requeue) {
            nacked = true;
            System.out.printf("  [NACK] %s — requeue=%b (intento #%d)%n", id, requeue, deliveryAttempt);
        }

        void incrementDeliveryAttempt() { deliveryAttempt++; }

        String id()         { return id; }
        String body()       { return body; }
        String routingKey() { return routingKey; }
        Map<String, String> headers() { return headers; }
        boolean isAcked()   { return acked; }
        boolean isNacked()  { return nacked; }
        int deliveryAttempt() { return deliveryAttempt; }
    }

    // ── Queue ─────────────────────────────────────────────────────────────────

    // prefetchCount: máximo de mensajes en vuelo (pendientes de ACK) por consumidor.
    // Evita que un consumer lento acapare todos los mensajes de la queue.
    // En Spring AMQP: @RabbitListener(queues = "my-queue", concurrency = "1-3")
    static class Queue {
        private final String name;
        private final Queue  dlq;                     // Dead Letter Queue (null si no tiene)
        private final int    maxDeliveryAttempts;
        // LinkedBlockingQueue garantiza orden FIFO y thread-safety
        private final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();

        Queue(String name) {
            this(name, null, 3);
        }

        Queue(String name, Queue dlq, int maxDeliveryAttempts) {
            this.name                = name;
            this.dlq                 = dlq;
            this.maxDeliveryAttempts = maxDeliveryAttempts;
        }

        void enqueue(Message msg) {
            messages.offer(msg);
        }

        // Entrega hasta prefetchCount mensajes al consumer.
        // En AMQP real, el broker lleva cuenta de mensajes sin ACK por canal.
        void consume(Consumer<Message> handler, int prefetchCount) {
            int consumed = 0;
            while (consumed < prefetchCount && !messages.isEmpty()) {
                Message msg = messages.poll();
                if (msg == null) break;
                try {
                    handler.accept(msg);
                    if (!msg.isAcked() && !msg.isNacked()) {
                        // Si el handler no llamó a ack/nack, auto-ack para la demo
                        msg.ack();
                    } else if (msg.isNacked()) {
                        // NACK sin requeue o demasiados intentos → DLQ
                        if (msg.deliveryAttempt() >= maxDeliveryAttempts) {
                            sendToDlq(msg);
                        } else {
                            msg.incrementDeliveryAttempt();
                            messages.offer(msg); // requeue al final
                        }
                    }
                } catch (Exception e) {
                    System.out.printf("  [%s] Excepción no capturada: %s → requeue%n", name, e.getMessage());
                    msg.incrementDeliveryAttempt();
                    if (msg.deliveryAttempt() > maxDeliveryAttempts) {
                        sendToDlq(msg);
                    } else {
                        messages.offer(msg);
                    }
                }
                consumed++;
            }
        }

        private void sendToDlq(Message msg) {
            if (dlq != null) {
                System.out.printf("  [DLQ ← %s] %s enviado a DLQ tras %d intentos%n",
                        name, msg.id(), msg.deliveryAttempt());
                dlq.enqueue(msg);
            } else {
                System.out.printf("  [DROP] %s descartado (sin DLQ configurada)%n", msg.id());
            }
        }

        int size() { return messages.size(); }
        String name() { return name; }
    }

    // ── Exchanges ─────────────────────────────────────────────────────────────

    interface Exchange {
        String name();
        void bind(Queue queue, String bindingKey);
        void publish(Message message);
    }

    // ── Direct Exchange ───────────────────────────────────────────────────────

    // Routing key del mensaje debe coincidir EXACTAMENTE con el binding key.
    // Uso típico: RPC, tareas dirigidas a un worker específico.
    static class DirectExchange implements Exchange {
        private final String name;
        private final Map<String, List<Queue>> bindings = new LinkedHashMap<>();

        DirectExchange(String name) { this.name = name; }

        @Override public String name() { return name; }

        @Override
        public void bind(Queue queue, String bindingKey) {
            bindings.computeIfAbsent(bindingKey, k -> new ArrayList<>()).add(queue);
            System.out.printf("  [Direct:%s] bind queue='%s' key='%s'%n", name, queue.name(), bindingKey);
        }

        @Override
        public void publish(Message msg) {
            List<Queue> targets = bindings.getOrDefault(msg.routingKey(), List.of());
            System.out.printf("  [Direct:%s] publish key='%s' → %d queue(s)%n",
                    name, msg.routingKey(), targets.size());
            targets.forEach(q -> q.enqueue(msg));
        }
    }

    // ── Fanout Exchange ───────────────────────────────────────────────────────

    // Ignora la routing key. Envía a TODAS las queues enlazadas (broadcast).
    // Uso típico: notificaciones, invalidación de caché en múltiples servicios.
    static class FanoutExchange implements Exchange {
        private final String name;
        private final List<Queue> queues = new ArrayList<>();

        FanoutExchange(String name) { this.name = name; }

        @Override public String name() { return name; }

        @Override
        public void bind(Queue queue, String bindingKey) {
            queues.add(queue); // binding key ignorada
            System.out.printf("  [Fanout:%s] bind queue='%s' (key ignorada)%n", name, queue.name());
        }

        @Override
        public void publish(Message msg) {
            System.out.printf("  [Fanout:%s] broadcast → %d queues%n", name, queues.size());
            queues.forEach(q -> q.enqueue(new Message(msg.body(), msg.routingKey())));
        }
    }

    // ── Topic Exchange ────────────────────────────────────────────────────────

    // Routing key con wildcards:
    //   * → exactamente UNA palabra (a.*.c coincide con a.b.c pero no a.b.d.c)
    //   # → CERO O MÁS palabras   (a.# coincide con a, a.b, a.b.c, etc.)
    // Uso típico: logs por severidad + servicio ("orders.error", "payments.#")
    static class TopicExchange implements Exchange {
        private final String name;
        private final Map<String, List<Queue>> bindings = new LinkedHashMap<>();

        TopicExchange(String name) { this.name = name; }

        @Override public String name() { return name; }

        @Override
        public void bind(Queue queue, String pattern) {
            bindings.computeIfAbsent(pattern, k -> new ArrayList<>()).add(queue);
            System.out.printf("  [Topic:%s] bind queue='%s' pattern='%s'%n", name, queue.name(), pattern);
        }

        @Override
        public void publish(Message msg) {
            List<Queue> matched = new ArrayList<>();
            bindings.forEach((pattern, queues) -> {
                if (topicMatches(pattern, msg.routingKey())) matched.addAll(queues);
            });
            System.out.printf("  [Topic:%s] publish key='%s' → %d queue(s) coinciden%n",
                    name, msg.routingKey(), matched.size());
            matched.forEach(q -> q.enqueue(msg));
        }

        // Convierte el patrón AMQP a regex y compara
        static boolean topicMatches(String pattern, String routingKey) {
            String regex = pattern
                    .replace(".", "\\.")
                    .replace("*", "[^.]+")       // * = una sola palabra
                    .replace("#", ".*");          // # = cualquier número de palabras
            return routingKey.matches(regex);
        }
    }

    // ── Headers Exchange ──────────────────────────────────────────────────────

    // Enruta por cabeceras del mensaje, no por routing key.
    // x-match: "all" → todas las cabeceras del binding deben coincidir
    // x-match: "any" → al menos una cabecera debe coincidir
    static class HeadersExchange implements Exchange {
        private final String name;
        private final List<HeaderBinding> bindings = new ArrayList<>();

        HeadersExchange(String name) { this.name = name; }

        @Override public String name() { return name; }

        @Override
        public void bind(Queue queue, String bindingKey) {
            // bindingKey en formato "key1=v1,key2=v2,x-match=all"
            Map<String, String> criteria = new LinkedHashMap<>();
            String matchType = "all";
            for (String pair : bindingKey.split(",")) {
                String[] kv = pair.split("=", 2);
                if ("x-match".equals(kv[0])) { matchType = kv[1]; }
                else { criteria.put(kv[0], kv[1]); }
            }
            bindings.add(new HeaderBinding(queue, criteria, matchType));
            System.out.printf("  [Headers:%s] bind queue='%s' criteria=%s x-match=%s%n",
                    name, queue.name(), criteria, matchType);
        }

        @Override
        public void publish(Message msg) {
            List<Queue> matched = new ArrayList<>();
            for (HeaderBinding b : bindings) {
                if (b.matches(msg.headers())) matched.add(b.queue);
            }
            System.out.printf("  [Headers:%s] publish headers=%s → %d queue(s)%n",
                    name, msg.headers(), matched.size());
            matched.forEach(q -> q.enqueue(msg));
        }

        static class HeaderBinding {
            final Queue queue;
            final Map<String, String> criteria;
            final String matchType; // "all" o "any"

            HeaderBinding(Queue queue, Map<String, String> criteria, String matchType) {
                this.queue = queue;
                this.criteria = criteria;
                this.matchType = matchType;
            }

            boolean matches(Map<String, String> msgHeaders) {
                if ("all".equals(matchType)) {
                    return criteria.entrySet().stream()
                            .allMatch(e -> e.getValue().equals(msgHeaders.get(e.getKey())));
                } else { // any
                    return criteria.entrySet().stream()
                            .anyMatch(e -> e.getValue().equals(msgHeaders.get(e.getKey())));
                }
            }
        }
    }

    // ── Competing Consumers ───────────────────────────────────────────────────

    // Varios consumers en la misma queue: cada mensaje va a UN solo consumer.
    // Distribuye carga automáticamente (round-robin por defecto en RabbitMQ).
    // Usa prefetchCount para que un consumer lento no acapare la queue.
    static class CompetingConsumerPool {
        private final String name;
        private final int    prefetch;
        private final AtomicInteger processed = new AtomicInteger(0);

        CompetingConsumerPool(String name, int prefetch) {
            this.name = name;
            this.prefetch = prefetch;
        }

        // Simula N consumers concurrentes sobre la misma queue
        void start(Queue queue, int consumerCount) throws InterruptedException {
            ExecutorService pool = Executors.newFixedThreadPool(consumerCount);
            CountDownLatch  done = new CountDownLatch(consumerCount);

            for (int i = 0; i < consumerCount; i++) {
                final int consumerId = i + 1;
                pool.submit(() -> {
                    try {
                        queue.consume(msg -> {
                            System.out.printf("  [Consumer-%d] procesando %s: '%s'%n",
                                    consumerId, msg.id(), msg.body());
                            busyWaitMs(20);
                            msg.ack();
                            processed.incrementAndGet();
                        }, prefetch);
                    } finally {
                        done.countDown();
                    }
                });
            }
            done.await(5, TimeUnit.SECONDS);
            pool.shutdown();
        }

        int processed() { return processed.get(); }
    }

    static void busyWaitMs(long ms) {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { /* spin */ }
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        System.out.println("═".repeat(65));
        System.out.println("  RABBITMQ — Exchanges, ACK/NACK, DLQ, Competing Consumers");
        System.out.println("═".repeat(65));

        // ── 1. Direct Exchange ───────────────────────────────────────────────
        System.out.println("\n=== Direct Exchange ===");
        Queue emailQueue  = new Queue("email-queue");
        Queue smsQueue    = new Queue("sms-queue");
        DirectExchange direct = new DirectExchange("notifications.direct");
        direct.bind(emailQueue, "email");
        direct.bind(smsQueue,   "sms");

        direct.publish(new Message("Bienvenido, Jorge!", "email"));
        direct.publish(new Message("Tu código es 1234", "sms"));
        direct.publish(new Message("Push notification", "push")); // sin binding → 0 queues

        emailQueue.consume(msg -> { System.out.println("  [EmailWorker] " + msg.body()); msg.ack(); }, 10);
        smsQueue.consume(msg -> { System.out.println("  [SmsWorker] "   + msg.body()); msg.ack(); }, 10);

        // ── 2. Fanout Exchange ───────────────────────────────────────────────
        System.out.println("\n=== Fanout Exchange (broadcast) ===");
        Queue serviceA = new Queue("service-a-events");
        Queue serviceB = new Queue("service-b-events");
        Queue serviceC = new Queue("service-c-events");
        FanoutExchange fanout = new FanoutExchange("user.events.fanout");
        fanout.bind(serviceA, "");
        fanout.bind(serviceB, "");
        fanout.bind(serviceC, "");

        fanout.publish(new Message("user.created: jorge@example.com", ""));

        for (Queue q : List.of(serviceA, serviceB, serviceC)) {
            q.consume(msg -> { System.out.printf("  [%s] recibido: %s%n", q.name(), msg.body()); msg.ack(); }, 1);
        }

        // ── 3. Topic Exchange ────────────────────────────────────────────────
        System.out.println("\n=== Topic Exchange (wildcards) ===");
        Queue allLogs    = new Queue("all-logs");
        Queue orderLogs  = new Queue("order-logs");
        Queue errorLogs  = new Queue("error-logs");
        TopicExchange topic = new TopicExchange("logs.topic");
        topic.bind(allLogs,   "#");              // todos los mensajes
        topic.bind(orderLogs, "orders.*");       // orders.info, orders.error, etc.
        topic.bind(errorLogs, "*.error");        // cualquier servicio + .error

        topic.publish(new Message("Pedido creado",    "orders.info"));
        topic.publish(new Message("Pago rechazado",   "orders.error"));
        topic.publish(new Message("Auth fallida",     "auth.error"));
        topic.publish(new Message("Deploy OK",        "devops.info"));

        System.out.println("  allLogs contiene:   " + allLogs.size()   + " mensajes");
        System.out.println("  orderLogs contiene: " + orderLogs.size() + " mensajes");
        System.out.println("  errorLogs contiene: " + errorLogs.size() + " mensajes");

        // ── 4. Headers Exchange ──────────────────────────────────────────────
        System.out.println("\n=== Headers Exchange ===");
        Queue premiumQueue = new Queue("premium-users");
        Queue euQueue      = new Queue("eu-region");
        HeadersExchange headers = new HeadersExchange("user.headers");
        headers.bind(premiumQueue, "plan=premium,x-match=all");
        headers.bind(euQueue,      "region=eu,x-match=any");

        headers.publish(new Message("Oferta exclusiva", "notify",
                Map.of("plan", "premium", "region", "us")));
        headers.publish(new Message("GDPR notice", "notify",
                Map.of("plan", "free", "region", "eu")));
        headers.publish(new Message("Premium + EU", "notify",
                Map.of("plan", "premium", "region", "eu")));

        System.out.println("  premiumQueue: " + premiumQueue.size() + " mensajes");
        System.out.println("  euQueue:      " + euQueue.size()      + " mensajes");

        // ── 5. ACK / NACK / DLQ ─────────────────────────────────────────────
        System.out.println("\n=== ACK / NACK / Dead Letter Queue ===");
        Queue dlq        = new Queue("orders-dlq");
        Queue ordersQueue = new Queue("orders-main", dlq, 2); // máx 2 intentos → DLQ

        ordersQueue.enqueue(new Message("Pedido-A", "order.created"));
        ordersQueue.enqueue(new Message("Pedido-B", "order.created")); // este fallará
        ordersQueue.enqueue(new Message("Pedido-C", "order.created"));

        ordersQueue.consume(msg -> {
            if (msg.body().contains("Pedido-B")) {
                msg.nack(false); // fallo permanente → tras maxAttempts va a DLQ
            } else {
                msg.ack();
            }
        }, 3);

        // Segundo intento para el mensaje reencueable
        ordersQueue.consume(msg -> {
            System.out.printf("  [Retry] %s intento #%d%n", msg.id(), msg.deliveryAttempt());
            msg.nack(false); // sigue fallando → DLQ
        }, 3);

        System.out.println("  DLQ contiene: " + dlq.size() + " mensaje(s)");
        dlq.consume(msg -> {
            System.out.println("  [DLQ Worker] Inspeccionando: " + msg.body());
            msg.ack();
        }, 10);

        // ── 6. Competing Consumers con prefetch ──────────────────────────────
        System.out.println("\n=== Competing Consumers (prefetch=2) ===");
        Queue jobQueue = new Queue("job-queue");
        for (int i = 1; i <= 8; i++) {
            jobQueue.enqueue(new Message("Job-" + i, "job"));
        }

        CompetingConsumerPool consumerPool = new CompetingConsumerPool("workers", 2);
        consumerPool.start(jobQueue, 4); // 4 consumers, prefetch 2 cada uno

        System.out.printf("  Jobs procesados: %d%n", consumerPool.processed());

        System.out.println("\n── Resumen ──────────────────────────────────────────────────");
        System.out.println("  Direct  → routing key exacta (un consumer específico)");
        System.out.println("  Fanout  → broadcast a todos (caché, notificaciones globales)");
        System.out.println("  Topic   → wildcards * y # (routing por categoría)");
        System.out.println("  Headers → enrutado por atributos del mensaje, no routing key");
        System.out.println("  ACK     → confirmar procesamiento; broker elimina el mensaje");
        System.out.println("  NACK    → rechazar; requeue=true vuelve a la cola");
        System.out.println("  DLQ     → mensajes que fallaron N veces o fueron rejected");
        System.out.println("  Prefetch → limita mensajes en vuelo por consumer (backpressure)");
        System.out.println("  Competing consumers → varios consumers en la misma queue");
        System.out.println("═".repeat(65));
    }
}
