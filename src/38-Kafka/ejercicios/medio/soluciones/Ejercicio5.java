import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio5 {

    static class ConsumerGroup {
        private final int numPartitions;
        private final List<String> consumers = new ArrayList<>();
        private final Map<String, List<Integer>> assignments = new HashMap<>();

        ConsumerGroup(int numPartitions) {
            this.numPartitions = numPartitions;
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
            assignments.clear();
            for (String c : consumers) {
                assignments.put(c, new ArrayList<>());
            }
            for (int p = 0; p < numPartitions; p++) {
                String consumer = consumers.get(p % consumers.size());
                assignments.get(consumer).add(p);
            }
            System.out.println("[REBALANCE] nueva asignación:");
            for (String c : consumers) {
                System.out.println("  " + c + " → particiones " + assignments.get(c));
            }
        }
    }

    public static void main(String[] args) {
        ConsumerGroup group = new ConsumerGroup(6);

        System.out.println("[ESCENARIO 1] 2 consumers");
        group.addConsumer("consumer-A");
        group.addConsumer("consumer-B");
        group.rebalance();

        System.out.println("\n[ESCENARIO 2] añadir tercer consumer");
        group.addConsumer("consumer-C");
        group.rebalance();

        System.out.println("\n[ESCENARIO 3] eliminar consumer-B");
        group.removeConsumer("consumer-B");
        group.rebalance();
    }
}
