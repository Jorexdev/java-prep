import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Ejercicio3 {

    static class PartitionedTopic {
        final String name;
        final int numPartitions;
        final List<Queue<String>> partitions;

        PartitionedTopic(String name, int numPartitions) {
            this.name = name;
            this.numPartitions = numPartitions;
            this.partitions = new ArrayList<>();
            for (int i = 0; i < numPartitions; i++) {
                partitions.add(new LinkedList<>());
            }
        }

        void produce(String message, int partition) {
            partitions.get(partition).offer(message);
        }

        List<String> poll(int partition) {
            List<String> batch = new ArrayList<>();
            Queue<String> q = partitions.get(partition);
            while (!q.isEmpty()) {
                batch.add(q.poll());
            }
            return batch;
        }
    }

    static class GroupConsumer {
        final String id;
        int assignedPartition = -1;

        GroupConsumer(String id) {
            this.id = id;
        }
    }

    static class ConsumerGroup {
        final List<GroupConsumer> consumers;
        final PartitionedTopic topic;

        ConsumerGroup(PartitionedTopic topic) {
            this.topic = topic;
            this.consumers = new ArrayList<>();
        }

        void addConsumer(GroupConsumer c) {
            consumers.add(c);
        }

        void subscribe() {
            // round-robin: consumer i → partición i
            for (int i = 0; i < consumers.size(); i++) {
                consumers.get(i).assignedPartition = i % topic.numPartitions;
            }
        }

        void consumeAll() {
            for (GroupConsumer c : consumers) {
                List<String> msgs = topic.poll(c.assignedPartition);
                for (String msg : msgs) {
                    System.out.println("  [" + c.id + "] partición=" + c.assignedPartition + " → " + msg);
                }
            }
        }
    }

    public static void main(String[] args) {
        PartitionedTopic topic = new PartitionedTopic("pedidos", 3);

        ConsumerGroup group = new ConsumerGroup(topic);
        group.addConsumer(new GroupConsumer("consumer-A"));
        group.addConsumer(new GroupConsumer("consumer-B"));
        group.addConsumer(new GroupConsumer("consumer-C"));
        group.subscribe();

        System.out.println("[PRODUCE] 9 mensajes distribuidos por partición (índice % 3)");
        for (int i = 1; i <= 9; i++) {
            int partition = (i - 1) % 3;
            topic.produce("msg-" + i, partition);
            System.out.println("  msg-" + i + " → partición " + partition);
        }

        System.out.println("\n[CONSUME]");
        group.consumeAll();
    }
}
