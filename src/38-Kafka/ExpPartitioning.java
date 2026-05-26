import java.util.*;

/**
 * Simulación de estrategias de particionado en Kafka con Java puro.
 *
 * Estrategias demostradas:
 *  - DefaultPartitioner: round-robin sin key (máxima distribución uniforme)
 *  - KeyPartitioner: hash(key) % numPartitions (orden por key garantizado)
 *  - StickyPartitioner: batchea hacia la misma partición hasta agotar el lote
 *
 * Por qué importa la estrategia de particionado:
 *  - El orden de mensajes solo está garantizado DENTRO de una partición
 *  - KeyPartitioner garantiza que todos los eventos de una key van a la misma partición
 *  - StickyPartitioner reduce el overhead de pequeños lotes distribuidos
 */
public class ExpPartitioning {

    static final int NUM_PARTITIONS = 3;
    static final int NUM_MENSAJES = 12;

    // ─────────────────────────────────────────────
    // MENSAJE
    // ─────────────────────────────────────────────

    record Mensaje(String key, String valor) {}

    // ─────────────────────────────────────────────
    // INTERFAZ PARTITIONER
    // ─────────────────────────────────────────────

    interface Partitioner {
        int partition(Mensaje mensaje, int numPartitions);
        String nombre();
    }

    // ─────────────────────────────────────────────
    // ESTRATEGIA 1: DefaultPartitioner — round-robin
    // ─────────────────────────────────────────────
    // Sin key: reparte mensajes en ciclo. Máxima distribución,
    // pero no garantiza orden entre mensajes relacionados.

    static class DefaultPartitioner implements Partitioner {
        private int counter = 0;

        @Override
        public int partition(Mensaje mensaje, int numPartitions) {
            return (counter++) % numPartitions;
        }

        @Override
        public String nombre() { return "DefaultPartitioner (round-robin)"; }
    }

    // ─────────────────────────────────────────────
    // ESTRATEGIA 2: KeyPartitioner — hash por key
    // ─────────────────────────────────────────────
    // Misma key → siempre misma partición.
    // Garantía: eventos del mismo key llegan en orden al consumer.
    // Riesgo: distribución desigual si las keys no están balanceadas.

    static class KeyPartitioner implements Partitioner {
        @Override
        public int partition(Mensaje mensaje, int numPartitions) {
            if (mensaje.key() == null) {
                // Sin key: fallback a partición 0 (comportamiento real de Kafka)
                return 0;
            }
            // Math.abs para evitar índices negativos con hashes negativos
            return Math.abs(mensaje.key().hashCode()) % numPartitions;
        }

        @Override
        public String nombre() { return "KeyPartitioner (hash(key) % partitions)"; }
    }

    // ─────────────────────────────────────────────
    // ESTRATEGIA 3: StickyPartitioner — batch por partición
    // ─────────────────────────────────────────────
    // Acumula mensajes en la misma partición hasta llenar el lote (batchSize).
    // Reduce el número de requests al broker. Útil cuando linger.ms > 0.

    static class StickyPartitioner implements Partitioner {
        private final int batchSize;
        private int currentPartition = 0;
        private int countInBatch = 0;

        StickyPartitioner(int batchSize) {
            this.batchSize = batchSize;
        }

        @Override
        public int partition(Mensaje mensaje, int numPartitions) {
            if (countInBatch >= batchSize) {
                // Lote lleno: cambiar a la siguiente partición
                currentPartition = (currentPartition + 1) % numPartitions;
                countInBatch = 0;
            }
            countInBatch++;
            return currentPartition;
        }

        @Override
        public String nombre() { return "StickyPartitioner (batch=" + batchSize + " por partición)"; }
    }

    // ─────────────────────────────────────────────
    // TOPIC SIMULADO
    // ─────────────────────────────────────────────

    static class Topic {
        private final String nombre;
        private final int[][] conteo; // [particion][0] = mensajes enviados
        private final List<List<String>> log; // log por partición

        Topic(String nombre, int numPartitions) {
            this.nombre = nombre;
            this.conteo = new int[numPartitions][1];
            this.log = new ArrayList<>();
            for (int i = 0; i < numPartitions; i++) {
                log.add(new ArrayList<>());
            }
        }

        void enviar(Mensaje msg, int particion) {
            conteo[particion][0]++;
            String entrada = String.format("key='%s' val='%s'",
                    msg.key() != null ? msg.key() : "(null)", msg.valor());
            log.get(particion).add(entrada);
        }

        void mostrarDistribucion(String estrategia) {
            System.out.printf("%n  Estrategia: %s%n", estrategia);
            System.out.println("  " + "─".repeat(55));
            int total = 0;
            for (int i = 0; i < conteo.length; i++) {
                int n = conteo[i][0];
                total += n;
                String barra = "█".repeat(n);
                System.out.printf("  P%d [%2d msgs] %s%n", i, n, barra);
            }
            System.out.printf("  Total enviados: %d%n", total);
        }

        void mostrarLog() {
            System.out.println("  Log por partición:");
            for (int i = 0; i < log.size(); i++) {
                System.out.printf("    P%d: %s%n", i, log.get(i));
            }
        }
    }

    // ─────────────────────────────────────────────
    // PRODUCER GENÉRICO
    // ─────────────────────────────────────────────

    static class Producer {
        private final Topic topic;
        private final Partitioner partitioner;

        Producer(Topic topic, Partitioner partitioner) {
            this.topic = topic;
            this.partitioner = partitioner;
        }

        void enviar(Mensaje msg) {
            int p = partitioner.partition(msg, NUM_PARTITIONS);
            topic.enviar(msg, p);
        }
    }

    // ─────────────────────────────────────────────
    // MAIN: comparativa de 3 estrategias, 12 mensajes cada una
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  KAFKA PARTITIONING STRATEGIES — Java puro");
        System.out.printf("  %d mensajes, %d particiones, 3 estrategias%n",
                NUM_MENSAJES, NUM_PARTITIONS);
        System.out.println("═".repeat(65));

        // Preparar mensajes: keys variadas para demostrar hashing
        List<Mensaje> mensajesSinKey = new ArrayList<>();
        List<Mensaje> mensajesConKey = new ArrayList<>();
        String[] keys = {"usuario-1", "usuario-2", "usuario-3"};
        for (int i = 0; i < NUM_MENSAJES; i++) {
            mensajesSinKey.add(new Mensaje(null, "evento-" + (i + 1)));
            mensajesConKey.add(new Mensaje(keys[i % keys.length], "evento-" + (i + 1)));
        }

        // ── Estrategia 1: DefaultPartitioner ──────────────────────────
        System.out.println("\n══ Estrategia 1: DefaultPartitioner (round-robin) ══");
        Topic topic1 = new Topic("pedidos", NUM_PARTITIONS);
        Producer p1 = new Producer(topic1, new DefaultPartitioner());
        mensajesSinKey.forEach(p1::enviar);
        topic1.mostrarDistribucion(new DefaultPartitioner().nombre());
        System.out.println("  → Sin key: distribución uniforme pero sin garantía de orden por entidad");

        // ── Estrategia 2: KeyPartitioner ──────────────────────────────
        System.out.println("\n══ Estrategia 2: KeyPartitioner (hash) ══");
        Topic topic2 = new Topic("pedidos", NUM_PARTITIONS);
        Producer p2 = new Producer(topic2, new KeyPartitioner());
        mensajesConKey.forEach(p2::enviar);
        topic2.mostrarDistribucion(new KeyPartitioner().nombre());
        topic2.mostrarLog();
        System.out.println("  → Todos los eventos de usuario-1 van siempre a la misma partición");
        System.out.println("  → El consumer que lee esa partición ve los eventos EN ORDEN");

        // Demo: verificar que el hash es determinista
        System.out.println("\n  Verificación de determinismo (misma key → misma partición siempre):");
        KeyPartitioner kp = new KeyPartitioner();
        for (String key : keys) {
            int p = kp.partition(new Mensaje(key, "cualquier-valor"), NUM_PARTITIONS);
            System.out.printf("    key='%s' → siempre partición %d (hash=%d)%n",
                    key, p, Math.abs(key.hashCode()) % NUM_PARTITIONS);
        }

        // ── Estrategia 3: StickyPartitioner ───────────────────────────
        System.out.println("\n══ Estrategia 3: StickyPartitioner (batch de 4) ══");
        Topic topic3 = new Topic("pedidos", NUM_PARTITIONS);
        Producer p3 = new Producer(topic3, new StickyPartitioner(4));
        mensajesSinKey.forEach(p3::enviar);
        topic3.mostrarDistribucion(new StickyPartitioner(4).nombre());
        System.out.println("  → Primeros 4 mensajes van a P0, siguientes 4 a P1, últimos 4 a P2");
        System.out.println("  → Reduce overhead de red: menos ProduceRequests al broker");

        // ── Tabla resumen ──────────────────────────────────────────────
        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN COMPARATIVO");
        System.out.println("═".repeat(65));
        System.out.println("  DefaultPartitioner  → uniforme, sin orden por entidad, sin key");
        System.out.println("  KeyPartitioner      → orden garantizado por key, riesgo de hot partition");
        System.out.println("  StickyPartitioner   → minimiza requests al broker, buen throughput");
        System.out.println();
        System.out.println("  Clave: el ORDEN en Kafka solo se garantiza dentro de una partición.");
        System.out.println("  Si necesitas que 'crear-pedido' llegue antes de 'pagar-pedido'");
        System.out.println("  para el mismo pedido → usa KeyPartitioner con pedidoId como key.");
        System.out.println("═".repeat(65));
    }
}
