import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulación de Kafka con Java puro.
 *
 * Conceptos demostrados:
 *  - Topic con N particiones (cada partición es una BlockingQueue)
 *  - Producer con distribución round-robin y por key hash
 *  - Consumer con poll() y commit manual de offsets
 *  - ConsumerGroup: cada partición asignada a exactamente un consumer
 */
public class ExpKafkaSimulation {

    // ─────────────────────────────────────────────
    // MODELO: Mensaje con offset, key y valor
    // ─────────────────────────────────────────────

    record Mensaje(int offset, String key, String valor) {
        @Override
        public String toString() {
            return String.format("Mensaje{offset=%d, key='%s', valor='%s'}", offset, key, valor);
        }
    }

    // ─────────────────────────────────────────────
    // TOPIC: lista de particiones (BlockingQueues)
    // ─────────────────────────────────────────────

    static class Topic {
        private final String nombre;
        private final List<Queue<Mensaje>> particiones;
        private final int[] contadoresOffset; // offset siguiente por partición

        Topic(String nombre, int numParticiones) {
            this.nombre = nombre;
            this.particiones = new ArrayList<>();
            this.contadoresOffset = new int[numParticiones];
            for (int i = 0; i < numParticiones; i++) {
                particiones.add(new LinkedList<>());
            }
        }

        int numParticiones() {
            return particiones.size();
        }

        // Publicar en una partición concreta con offset auto-asignado
        Mensaje publicar(int particion, String key, String valor) {
            int offset = contadoresOffset[particion]++;
            Mensaje msg = new Mensaje(offset, key, valor);
            particiones.get(particion).add(msg);
            return msg;
        }

        // Leer el siguiente mensaje de una partición (si hay)
        Optional<Mensaje> poll(int particion) {
            return Optional.ofNullable(particiones.get(particion).poll());
        }

        String nombre() { return nombre; }
    }

    // ─────────────────────────────────────────────
    // PRODUCER: publica mensajes al topic
    // ─────────────────────────────────────────────

    static class Producer {
        private final Topic topic;
        private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

        Producer(Topic topic) {
            this.topic = topic;
        }

        // Sin key → round-robin entre particiones
        void enviar(String valor) {
            int particion = roundRobinIndex.getAndIncrement() % topic.numParticiones();
            Mensaje msg = topic.publicar(particion, null, valor);
            System.out.printf("[Producer] → particion=%d  %s%n", particion, msg);
        }

        // Con key → partición determinista por hash de la key
        // Mismo key siempre va a la misma partición (orden garantizado por key)
        void enviar(String key, String valor) {
            int particion = Math.abs(key.hashCode()) % topic.numParticiones();
            Mensaje msg = topic.publicar(particion, key, valor);
            System.out.printf("[Producer] key='%s' → particion=%d  %s%n", key, particion, msg);
        }
    }

    // ─────────────────────────────────────────────
    // CONSUMER OFFSET STORE: simula __consumer_offsets
    // ─────────────────────────────────────────────

    static class OffsetStore {
        // groupId → particionIndex → último offset procesado
        private final Map<String, int[]> store = new HashMap<>();

        void commit(String groupId, int particion, int offset) {
            store.get(groupId)[particion] = offset;
            System.out.printf("    [OffsetStore] COMMIT group='%s' particion=%d offset=%d%n",
                    groupId, particion, offset);
        }

        int getOffset(String groupId, int particion) {
            return store.getOrDefault(groupId, new int[0]).length > particion
                    ? store.get(groupId)[particion] : -1;
        }

        void registrarGrupo(String groupId, int numParticiones) {
            store.put(groupId, new int[numParticiones]);
            Arrays.fill(store.get(groupId), -1);
        }
    }

    // ─────────────────────────────────────────────
    // CONSUMER: hace poll en su partición asignada
    // ─────────────────────────────────────────────

    static class Consumer implements Runnable {
        private final String id;
        private final String groupId;
        private final Topic topic;
        private final int particionAsignada;
        private final OffsetStore offsetStore;

        Consumer(String id, String groupId, Topic topic, int particionAsignada, OffsetStore offsetStore) {
            this.id = id;
            this.groupId = groupId;
            this.topic = topic;
            this.particionAsignada = particionAsignada;
            this.offsetStore = offsetStore;
        }

        @Override
        public void run() {
            System.out.printf("[Consumer %s] Asignado a particion=%d (group='%s')%n",
                    id, particionAsignada, groupId);

            // Poll loop: procesa todos los mensajes disponibles en la partición
            Optional<Mensaje> mensajeOpt;
            while ((mensajeOpt = topic.poll(particionAsignada)).isPresent()) {
                Mensaje msg = mensajeOpt.get();
                System.out.printf("  [Consumer %s] Procesando particion=%d → %s%n",
                        id, particionAsignada, msg);

                // Procesar el mensaje (aquí iría la lógica de negocio)
                procesarMensaje(msg);

                // Manual commit: at-least-once
                // Si el proceso falla antes de este commit, el mensaje se reprocesará
                offsetStore.commit(groupId, particionAsignada, msg.offset());
            }
            System.out.printf("[Consumer %s] Poll completado — no hay más mensajes en particion=%d%n",
                    id, particionAsignada);
        }

        private void procesarMensaje(Mensaje msg) {
            // Simulamos procesamiento (en producción: llamada a servicio, escritura en BD, etc.)
        }
    }

    // ─────────────────────────────────────────────
    // CONSUMER GROUP: asigna particiones a consumers
    // ─────────────────────────────────────────────

    static class ConsumerGroup {
        private final String groupId;
        private final Topic topic;
        private final OffsetStore offsetStore;
        private final List<Consumer> consumers = new ArrayList<>();

        ConsumerGroup(String groupId, Topic topic, OffsetStore offsetStore) {
            this.groupId = groupId;
            this.topic = topic;
            this.offsetStore = offsetStore;
            offsetStore.registrarGrupo(groupId, topic.numParticiones());
        }

        // Añadir un consumer y asignarle una partición (rebalance simplificado)
        void agregarConsumer(String id) {
            int particion = consumers.size(); // asignación secuencial
            if (particion >= topic.numParticiones()) {
                System.out.printf("[ConsumerGroup %s] Consumer '%s' no tiene partición asignada " +
                        "(consumers > particiones → consumer inactivo)%n", groupId, id);
                return;
            }
            consumers.add(new Consumer(id, groupId, topic, particion, offsetStore));
        }

        // Lanzar todos los consumers en threads paralelos
        void iniciar() throws InterruptedException {
            System.out.printf("%n[ConsumerGroup '%s'] Iniciando %d consumers para %d particiones%n",
                    groupId, consumers.size(), topic.numParticiones());
            System.out.println("─".repeat(60));

            ExecutorService executor = Executors.newFixedThreadPool(consumers.size());
            for (Consumer c : consumers) {
                executor.submit(c);
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    // ─────────────────────────────────────────────
    // MAIN: demo completa
    // ─────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {

        System.out.println("═".repeat(60));
        System.out.println("  SIMULACIÓN KAFKA — Java puro");
        System.out.println("═".repeat(60));

        // 1. Crear topic con 3 particiones
        Topic topic = new Topic("pedidos", 3);
        OffsetStore offsetStore = new OffsetStore();

        // 2. Producer publica 9 mensajes (round-robin sin key)
        System.out.println("\n[Producer] Publicando 9 mensajes sin key (round-robin):");
        System.out.println("─".repeat(60));
        Producer producer = new Producer(topic);
        for (int i = 1; i <= 9; i++) {
            producer.enviar("pedido-" + i);
        }

        // 3. Consumer Group con 3 consumers (uno por partición)
        ConsumerGroup grupo = new ConsumerGroup("procesador", topic, offsetStore);
        grupo.agregarConsumer("C1");
        grupo.agregarConsumer("C2");
        grupo.agregarConsumer("C3");
        grupo.iniciar();

        // 4. Demo: más consumers que particiones
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  DEMO: Más consumers que particiones");
        System.out.println("═".repeat(60));

        Topic topicSmall = new Topic("eventos", 2);
        OffsetStore offsetStore2 = new OffsetStore();
        Producer producer2 = new Producer(topicSmall);
        producer2.enviar("evento-1");
        producer2.enviar("evento-2");

        ConsumerGroup grupoGrande = new ConsumerGroup("listeners", topicSmall, offsetStore2);
        grupoGrande.agregarConsumer("L1");
        grupoGrande.agregarConsumer("L2");
        grupoGrande.agregarConsumer("L3"); // sobrante — quedará inactivo
        grupoGrande.iniciar();

        // 5. Demo: distribución por key
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  DEMO: Distribución por key (mismo key → misma partición)");
        System.out.println("═".repeat(60));

        Topic topicKeys = new Topic("transacciones", 3);
        OffsetStore offsetStore3 = new OffsetStore();
        Producer producerKeys = new Producer(topicKeys);

        // Misma key → siempre misma partición (orden garantizado)
        producerKeys.enviar("usuario-42", "deposito 100€");
        producerKeys.enviar("usuario-42", "retiro 50€");
        producerKeys.enviar("usuario-42", "deposito 200€");
        producerKeys.enviar("usuario-99", "deposito 500€");
        producerKeys.enviar("usuario-99", "retiro 100€");

        System.out.println("\n→ Todos los eventos de usuario-42 van a la misma partición (orden garantizado)");
        System.out.println("→ Todos los eventos de usuario-99 van a la misma partición (orden garantizado)");
    }
}
