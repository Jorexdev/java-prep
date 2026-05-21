import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio4 {

    static class OffsetTracker {
        private final Map<Integer, Long> committed = new HashMap<>();

        void commit(int partition, long offset) {
            committed.put(partition, offset);
            System.out.println("[COMMIT] partición=" + partition + " offset=" + offset);
        }

        long getOffset(int partition) {
            return committed.getOrDefault(partition, 0L);
        }
    }

    static class KafkaConsumerSimple {
        private final String id;
        private final OffsetTracker tracker;

        KafkaConsumerSimple(String id, OffsetTracker tracker) {
            this.id = id;
            this.tracker = tracker;
        }

        void consume(List<String> partition0, long fromOffset) {
            System.out.println("[" + id + "] iniciando desde offset " + fromOffset);
            for (long i = fromOffset; i < partition0.size(); i++) {
                System.out.println("[" + id + "] procesando offset=" + i + " valor='" + partition0.get((int) i) + "'");
            }
        }
    }

    public static void main(String[] args) {
        List<String> partition0 = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            partition0.add("mensaje-" + i);
        }

        OffsetTracker tracker = new OffsetTracker();

        KafkaConsumerSimple consumer1 = new KafkaConsumerSimple("consumer-1", tracker);
        consumer1.consume(partition0, tracker.getOffset(0));
        tracker.commit(0, 4L);

        System.out.println("\n[CRASH] consumer-1 ha fallado. Nueva instancia arranca...\n");

        KafkaConsumerSimple consumer2 = new KafkaConsumerSimple("consumer-2", tracker);
        consumer2.consume(partition0, tracker.getOffset(0));
    }
}
