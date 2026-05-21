import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Ejercicio2 {

    static class ConsumerRecord<V> {
        final String topic;
        final int partition;
        final long offset;
        final V value;

        ConsumerRecord(String topic, int partition, long offset, V value) {
            this.topic = topic;
            this.partition = partition;
            this.offset = offset;
            this.value = value;
        }

        @Override
        public String toString() {
            return "ConsumerRecord{topic='" + topic + "', partition=" + partition
                    + ", offset=" + offset + ", value='" + value + "'}";
        }
    }

    static class KafkaConsumer {
        private String subscribedTopic;
        private final Queue<ConsumerRecord<String>> buffer = new LinkedList<>();
        private long offsetCounter = 0;

        void subscribe(String topic) {
            this.subscribedTopic = topic;
            System.out.println("[CONSUMER] Suscrito al topic: " + topic);
        }

        // @KafkaListener(topics = "mensajes")
        void addRecord(String value) {
            buffer.offer(new ConsumerRecord<>(subscribedTopic, 0, offsetCounter++, value));
        }

        List<ConsumerRecord<String>> poll() {
            List<ConsumerRecord<String>> batch = new ArrayList<>();
            while (!buffer.isEmpty()) {
                batch.add(buffer.poll());
            }
            return batch;
        }
    }

    public static void main(String[] args) {
        KafkaConsumer consumer = new KafkaConsumer();
        consumer.subscribe("mensajes");

        for (int i = 1; i <= 10; i++) {
            consumer.addRecord("mensaje-" + i);
        }

        System.out.println("\n[POLL LOOP]");
        List<ConsumerRecord<String>> records;
        do {
            records = consumer.poll();
            for (ConsumerRecord<String> record : records) {
                System.out.println("  " + record);
            }
        } while (!records.isEmpty());

        System.out.println("[DONE] No quedan mensajes.");
    }
}
