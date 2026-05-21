import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Ejercicio5 {

    static class KeyPartitioner {
        static int getPartition(String key, int numPartitions) {
            return Math.abs(key.hashCode()) % numPartitions;
        }
    }

    static class PartitionedProducer {
        private final int numPartitions;
        private final List<Queue<String>> partitions;

        PartitionedProducer(int numPartitions) {
            this.numPartitions = numPartitions;
            this.partitions = new ArrayList<>();
            for (int i = 0; i < numPartitions; i++) {
                partitions.add(new LinkedList<>());
            }
        }

        void send(String key, String value) {
            int partition = KeyPartitioner.getPartition(key, numPartitions);
            partitions.get(partition).offer(value);
            System.out.println("[SEND] key='" + key + "' value='" + value + "' → partición " + partition);
        }

        void printStats() {
            System.out.println("\n[STATS] Mensajes por partición:");
            for (int i = 0; i < numPartitions; i++) {
                System.out.println("  partición " + i + " → " + partitions.get(i).size() + " mensajes");
            }
        }
    }

    public static void main(String[] args) {
        PartitionedProducer producer = new PartitionedProducer(3);

        String[] keys = {"user-A", "user-B", "user-C", "user-D"};
        for (int i = 0; i < 12; i++) {
            String key = keys[i % keys.length];
            producer.send(key, "evento-" + (i + 1));
        }

        System.out.println("\n[KEY → PARTICIÓN]");
        for (String key : keys) {
            System.out.println("  '" + key + "' → partición " + KeyPartitioner.getPartition(key, 3));
        }

        producer.printStats();
    }
}
