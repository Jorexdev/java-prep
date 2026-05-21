import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio4 {

    static class Record {
        final String key;
        final String value;

        Record(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Record{key='" + key + "', value=" + (value == null ? "null(tombstone)" : "'" + value + "'") + "}";
        }
    }

    static class CompactedTopic {
        private final List<Record> log = new ArrayList<>();
        private final Map<String, String> compactedView = new LinkedHashMap<>();

        void append(String key, String value) {
            log.add(new Record(key, value));
        }

        void compact() {
            Map<String, String> seen = new LinkedHashMap<>();
            for (int i = log.size() - 1; i >= 0; i--) {
                Record r = log.get(i);
                if (!seen.containsKey(r.key)) {
                    seen.put(r.key, r.value);
                }
            }
            compactedView.clear();
            for (Map.Entry<String, String> entry : seen.entrySet()) {
                if (entry.getValue() != null) {
                    compactedView.put(entry.getKey(), entry.getValue());
                }
            }
            System.out.println("[COMPACT] log=" + log.size() + " registros → compacted view=" + compactedView.size() + " keys");
        }

        Map<String, String> getCompactedView() {
            return compactedView;
        }

        int logSize() {
            return log.size();
        }
    }

    public static void main(String[] args) {
        CompactedTopic topic = new CompactedTopic();

        String[] keys = {"k1", "k2", "k3", "k4", "k5"};
        for (int round = 0; round < 4; round++) {
            for (String key : keys) {
                topic.append(key, key + "-v" + (round + 1));
            }
        }
        topic.append("k5", null);

        System.out.println("[LOG] total registros antes de compactar: " + topic.logSize());

        topic.compact();

        Map<String, String> view = topic.getCompactedView();
        System.out.println("[COMPACTED VIEW] " + view.size() + " keys:");
        for (Map.Entry<String, String> entry : view.entrySet()) {
            System.out.println("  " + entry.getKey() + " → " + entry.getValue());
        }

        System.out.println("\n[VERIFICACIÓN]");
        System.out.println("  keys activas: " + view.size() + " (esperado 4)");
        System.out.println("  k5 eliminada (tombstone): " + !view.containsKey("k5"));
        System.out.println("  k1 valor final: " + view.get("k1") + " (esperado k1-v4)");
        System.out.println("  k4 valor final: " + view.get("k4") + " (esperado k4-v4)");
    }
}
