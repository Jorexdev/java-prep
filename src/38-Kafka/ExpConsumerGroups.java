import java.util.*;

/**
 * Simulación de Consumer Group rebalancing con Java puro.
 *
 * Conceptos demostrados:
 *  - Range assignment strategy: particiones contiguas asignadas secuencialmente
 *  - Stop-the-world pause: todos los consumers se detienen durante el rebalance
 *  - Consumer idle: cuando hay más consumers que particiones
 *  - Escenarios: join (1→2→3→4 consumers) para 3 particiones
 */
public class ExpConsumerGroups {

    // ─────────────────────────────────────────────
    // PARTICIÓN: cola de mensajes con offset
    // ─────────────────────────────────────────────

    static class Partition {
        private final int id;
        private final Queue<String> mensajes = new LinkedList<>();
        private String assignedTo = null; // consumer id o null si no asignada

        Partition(int id) { this.id = id; }

        void agregar(String mensaje) { mensajes.add(mensaje); }
        Optional<String> poll() { return Optional.ofNullable(mensajes.poll()); }
        int id() { return id; }
        String assignedTo() { return assignedTo; }
        void asignarA(String consumerId) { this.assignedTo = consumerId; }
        boolean tieneAsignacion() { return assignedTo != null; }
    }

    // ─────────────────────────────────────────────
    // CONSUMER GROUP: gestiona rebalanceos
    // ─────────────────────────────────────────────

    static class ConsumerGroup {
        private final String groupId;
        private final List<Partition> partitions;
        private final List<String> members = new ArrayList<>();

        ConsumerGroup(String groupId, int numPartitions) {
            this.groupId = groupId;
            this.partitions = new ArrayList<>();
            for (int i = 0; i < numPartitions; i++) {
                partitions.add(new Partition(i));
            }
        }

        // Range strategy: divide particiones en rangos contiguos por consumer
        // Consumer i recibe particiones [start..end) donde start/end dependen del rango
        void rebalance() {
            System.out.printf("%n  *** STOP THE WORLD — Rebalance iniciado (group='%s') ***%n", groupId);
            System.out.println("  → Todos los consumers pausados. Ningún mensaje procesado durante el rebalance.");

            // Limpiar asignaciones previas
            partitions.forEach(p -> p.asignarA(null));

            int numPartitions = partitions.size();
            int numConsumers = members.size();

            if (numConsumers == 0) {
                System.out.println("  → Sin consumers activos. Ninguna partición asignada.");
                System.out.println("  *** Rebalance completado ***");
                return;
            }

            // Range assignment: distribuir particiones en bloques contiguos
            int base = numPartitions / numConsumers;
            int extra = numPartitions % numConsumers; // los primeros 'extra' consumers reciben uno más

            int partitionIdx = 0;
            for (int i = 0; i < numConsumers; i++) {
                String consumer = members.get(i);
                int asignadas = base + (i < extra ? 1 : 0);

                if (asignadas == 0) {
                    System.out.printf("  [Range] consumer='%s' → IDLE (más consumers que particiones)%n", consumer);
                    continue;
                }

                List<Integer> rango = new ArrayList<>();
                for (int j = 0; j < asignadas; j++) {
                    partitions.get(partitionIdx).asignarA(consumer);
                    rango.add(partitionIdx);
                    partitionIdx++;
                }
                System.out.printf("  [Range] consumer='%s' → particiones %s%n", consumer, rango);
            }

            System.out.println("  *** Rebalance completado — consumers resumiendo ***");
        }

        void join(String consumerId) {
            System.out.printf("%n[Group '%s'] Consumer '%s' se une al grupo%n", groupId, consumerId);
            members.add(consumerId);
            rebalance();
            mostrarAsignacion();
        }

        void leave(String consumerId) {
            System.out.printf("%n[Group '%s'] Consumer '%s' abandona el grupo%n", groupId, consumerId);
            members.remove(consumerId);
            rebalance();
            mostrarAsignacion();
        }

        void mostrarAsignacion() {
            System.out.printf("%n  Asignación actual (%d consumers, %d particiones):%n",
                    members.size(), partitions.size());
            System.out.println("  " + "─".repeat(40));
            partitions.forEach(p ->
                System.out.printf("  Partition-%d → %s%n", p.id(),
                        p.tieneAsignacion() ? "consumer='" + p.assignedTo() + "'" : "SIN ASIGNAR"));
            System.out.println("  " + "─".repeat(40));
        }

        // Simular procesamiento: cada consumer lee sus particiones asignadas
        void procesarMensajes() {
            System.out.println("\n  [Procesando mensajes con asignación actual]");
            Map<String, List<String>> procesados = new LinkedHashMap<>();

            for (Partition p : partitions) {
                if (!p.tieneAsignacion()) continue;
                String consumer = p.assignedTo();
                List<String> msgs = procesados.computeIfAbsent(consumer, k -> new ArrayList<>());
                Optional<String> msg;
                while ((msg = p.poll()).isPresent()) {
                    msgs.add("P" + p.id() + ":" + msg.get());
                }
            }

            if (procesados.isEmpty()) {
                System.out.println("  (sin mensajes que procesar)");
            } else {
                procesados.forEach((consumer, msgs) ->
                    System.out.printf("  consumer='%s' procesó: %s%n", consumer, msgs));
            }
        }

        List<Partition> partitions() { return partitions; }
    }

    // ─────────────────────────────────────────────
    // PRODUCER SIMULADO
    // ─────────────────────────────────────────────

    static class Producer {
        private int seq = 0;

        // Round-robin sobre las particiones disponibles
        void publicar(ConsumerGroup group, int numMensajes) {
            List<Partition> parts = group.partitions();
            System.out.printf("%n  [Producer] Publicando %d mensajes en %d particiones:%n",
                    numMensajes, parts.size());
            for (int i = 0; i < numMensajes; i++) {
                int idx = i % parts.size();
                String msg = "msg-" + (++seq);
                parts.get(idx).agregar(msg);
                System.out.printf("    → P%d ← %s%n", idx, msg);
            }
        }
    }

    // ─────────────────────────────────────────────
    // MAIN: demo rebalance al añadir/quitar consumers
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  CONSUMER GROUP REBALANCING — Java puro");
        System.out.println("  Estrategia: Range Assignment | Particiones: 3");
        System.out.println("═".repeat(65));

        ConsumerGroup group = new ConsumerGroup("procesador", 3);
        Producer producer = new Producer();

        // ── Paso 1: 1 consumer → recibe las 3 particiones ─────────────
        System.out.println("\n══ PASO 1: Un consumer se une ══");
        group.join("C1");
        producer.publicar(group, 3);
        group.procesarMensajes();

        // ── Paso 2: 2 consumers → 1 partición extra cada uno ──────────
        System.out.println("\n══ PASO 2: Segundo consumer se une ══");
        group.join("C2");
        producer.publicar(group, 3);
        group.procesarMensajes();

        // ── Paso 3: 3 consumers → 1 partición por consumer ────────────
        System.out.println("\n══ PASO 3: Tercer consumer se une ══");
        group.join("C3");
        producer.publicar(group, 3);
        group.procesarMensajes();

        // ── Paso 4: 4 consumers → uno queda IDLE ──────────────────────
        System.out.println("\n══ PASO 4: Cuarto consumer se une (idle esperado) ══");
        group.join("C4");
        producer.publicar(group, 3);
        group.procesarMensajes();

        // ── Paso 5: C1 se va → rebalance, C4 ya no está idle ──────────
        System.out.println("\n══ PASO 5: C1 abandona el grupo ══");
        group.leave("C1");
        producer.publicar(group, 3);
        group.procesarMensajes();

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN RANGE ASSIGNMENT");
        System.out.println("═".repeat(65));
        System.out.println("  Fórmula: base = particiones / consumers");
        System.out.println("           extra = particiones % consumers (primeros consumers reciben +1)");
        System.out.println("  Stop-the-world: cada join/leave pausa TODOS los consumers");
        System.out.println("  Consumer idle: cuando consumers > particiones el sobrante no recibe trabajo");
        System.out.println("  Alternativas: RoundRobin (distribuye 1 a 1), Sticky (minimiza movimientos)");
        System.out.println("═".repeat(65));
    }
}
