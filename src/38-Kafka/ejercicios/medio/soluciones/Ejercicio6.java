import java.util.*;

public class Ejercicio6 {

    // --- RebalanceListener ---

    interface RebalanceListener {
        void onPartitionsRevoked(String consumerId, List<Integer> partitions);
        void onPartitionsAssigned(String consumerId, List<Integer> partitions);
    }

    // --- OffsetCommitListener: hace commit de offsets al recibir revocación ---

    static class OffsetCommitListener implements RebalanceListener {
        // consumerId → (partición → offset actual)
        private final Map<String, Map<Integer, Long>> offsets = new HashMap<>();

        void setOffset(String consumerId, int partition, long offset) {
            offsets.computeIfAbsent(consumerId, k -> new HashMap<>()).put(partition, offset);
        }

        @Override
        public void onPartitionsRevoked(String consumerId, List<Integer> partitions) {
            Map<Integer, Long> consumerOffsets = offsets.getOrDefault(consumerId, Map.of());
            System.out.println("  [OffsetCommitListener] " + consumerId
                + " revocando particiones " + partitions + " — committing offsets:");
            for (int p : partitions) {
                long offset = consumerOffsets.getOrDefault(p, 0L);
                System.out.println("    partition=" + p + " offset=" + offset + " → COMMITTED");
            }
        }

        @Override
        public void onPartitionsAssigned(String consumerId, List<Integer> partitions) {
            System.out.println("  [OffsetCommitListener] " + consumerId
                + " recibió asignación: " + partitions);
        }
    }

    // --- ConsumerGroup con listeners ---

    static class ConsumerGroup {
        private final int numPartitions;
        private final List<String> consumers = new ArrayList<>();
        private final Map<String, List<Integer>> assignments = new HashMap<>();
        private final List<RebalanceListener> listeners = new ArrayList<>();

        ConsumerGroup(int numPartitions) {
            this.numPartitions = numPartitions;
        }

        void addListener(RebalanceListener listener) {
            listeners.add(listener);
        }

        void addConsumer(String consumerId) {
            consumers.add(consumerId);
            System.out.println("[GROUP] añadido: " + consumerId);
        }

        void removeConsumer(String consumerId) {
            consumers.remove(consumerId);
            assignments.remove(consumerId);
            System.out.println("[GROUP] eliminado: " + consumerId);
        }

        void rebalance() {
            System.out.println("[REBALANCE] iniciando rebalanceo con " + consumers.size() + " consumer(s)");

            // 1. Notificar revocación de asignaciones actuales
            for (Map.Entry<String, List<Integer>> entry : assignments.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    for (RebalanceListener l : listeners) {
                        l.onPartitionsRevoked(entry.getKey(), entry.getValue());
                    }
                }
            }

            // 2. Calcular nueva distribución round-robin
            assignments.clear();
            for (String c : consumers) {
                assignments.put(c, new ArrayList<>());
            }
            for (int p = 0; p < numPartitions; p++) {
                String consumer = consumers.get(p % consumers.size());
                assignments.get(consumer).add(p);
            }

            // 3. Notificar nuevas asignaciones
            for (String c : consumers) {
                for (RebalanceListener l : listeners) {
                    l.onPartitionsAssigned(c, assignments.get(c));
                }
            }

            System.out.println("[REBALANCE] asignación final:");
            for (String c : consumers) {
                System.out.println("  " + c + " → particiones " + assignments.get(c));
            }
        }
    }

    public static void main(String[] args) {
        ConsumerGroup group = new ConsumerGroup(6);
        OffsetCommitListener commitListener = new OffsetCommitListener();
        group.addListener(commitListener);

        System.out.println("======== ESCENARIO 1: 2 consumers ========");
        group.addConsumer("consumer-A");
        group.addConsumer("consumer-B");
        group.rebalance();

        // Simular progreso de consumo
        commitListener.setOffset("consumer-A", 0, 50L);
        commitListener.setOffset("consumer-A", 1, 43L);
        commitListener.setOffset("consumer-A", 2, 61L);
        commitListener.setOffset("consumer-B", 3, 38L);
        commitListener.setOffset("consumer-B", 4, 55L);
        commitListener.setOffset("consumer-B", 5, 47L);

        System.out.println("\n======== ESCENARIO 2: añadir consumer-C ========");
        group.addConsumer("consumer-C");
        group.rebalance();

        // Actualizar offsets post-rebalanceo
        commitListener.setOffset("consumer-A", 0, 80L);
        commitListener.setOffset("consumer-A", 1, 75L);
        commitListener.setOffset("consumer-B", 2, 90L);
        commitListener.setOffset("consumer-B", 3, 65L);
        commitListener.setOffset("consumer-C", 4, 30L);
        commitListener.setOffset("consumer-C", 5, 20L);

        System.out.println("\n======== ESCENARIO 3: eliminar consumer-B ========");
        group.removeConsumer("consumer-B");
        group.rebalance();
    }
}
