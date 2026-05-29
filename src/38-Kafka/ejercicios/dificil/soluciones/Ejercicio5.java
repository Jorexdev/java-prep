import java.util.*;

public class Ejercicio5 {

    // --- Record de entrada del stream ---

    record StreamRecord(String key, long value, long timestamp) {}

    // --- Ventana Tumbling ---

    static class TumblingWindow {
        private final long windowMs;

        TumblingWindow(long windowMs) { this.windowMs = windowMs; }

        // Calcula el inicio de la ventana para un timestamp dado
        long windowStart(long timestamp) {
            return (timestamp / windowMs) * windowMs;
        }
    }

    // --- Agregador con estado stateful ---

    static class KafkaStreamsAggregator {
        private final TumblingWindow window;

        KafkaStreamsAggregator(TumblingWindow window) {
            this.window = window;
        }

        /**
         * Retorna: clave → (inicio_ventana → suma_acumulada)
         */
        Map<String, Map<Long, Long>> aggregate(List<StreamRecord> records) {
            // clave → (inicio_ventana → suma)
            Map<String, Map<Long, Long>> state = new TreeMap<>();

            for (StreamRecord r : records) {
                long winStart = window.windowStart(r.timestamp());
                state
                    .computeIfAbsent(r.key(), k -> new TreeMap<>())
                    .merge(winStart, r.value(), Long::sum);
            }

            return state;
        }
    }

    public static void main(String[] args) {
        // Ventanas de 10 segundos (10 000 ms)
        long windowMs = 10_000L;
        TumblingWindow tumblingWindow = new TumblingWindow(windowMs);
        KafkaStreamsAggregator aggregator = new KafkaStreamsAggregator(tumblingWindow);

        // 15 records distribuidos en 3 ventanas × 3 claves
        // Ventana 1: t = 0 – 9 999 ms
        // Ventana 2: t = 10 000 – 19 999 ms
        // Ventana 3: t = 20 000 – 29 999 ms
        List<StreamRecord> records = List.of(
            // Ventana 1
            new StreamRecord("clicks",   3L,  1_000L),
            new StreamRecord("views",    5L,  2_000L),
            new StreamRecord("purchases",1L,  3_000L),
            new StreamRecord("clicks",   7L,  5_500L),
            new StreamRecord("views",    2L,  8_000L),

            // Ventana 2
            new StreamRecord("clicks",   4L, 10_200L),
            new StreamRecord("purchases",2L, 11_000L),
            new StreamRecord("views",    6L, 13_500L),
            new StreamRecord("clicks",   9L, 15_000L),
            new StreamRecord("purchases",3L, 18_000L),

            // Ventana 3
            new StreamRecord("views",    8L, 20_100L),
            new StreamRecord("clicks",   2L, 21_000L),
            new StreamRecord("purchases",5L, 23_500L),
            new StreamRecord("views",    4L, 25_000L),
            new StreamRecord("clicks",   6L, 28_000L)
        );

        System.out.println("=== Kafka Streams — Stateful Aggregation (Tumbling Window " + windowMs + "ms) ===");
        System.out.println("Records procesados: " + records.size() + "\n");

        Map<String, Map<Long, Long>> resultado = aggregator.aggregate(records);

        for (Map.Entry<String, Map<Long, Long>> keyEntry : resultado.entrySet()) {
            String key = keyEntry.getKey();
            System.out.println("Clave: [" + key + "]");
            long totalKey = 0;
            for (Map.Entry<Long, Long> winEntry : keyEntry.getValue().entrySet()) {
                long winStart = winEntry.getKey();
                long winEnd   = winStart + windowMs - 1;
                long suma     = winEntry.getValue();
                totalKey += suma;
                System.out.printf("  Ventana [%d - %d] → suma = %d%n", winStart, winEnd, suma);
            }
            System.out.println("  Total acumulado: " + totalKey + "\n");
        }

        // Verificación de totales esperados
        System.out.println("=== Verificación ===");
        // clicks:    ventana1=10, ventana2=13, ventana3=8
        // views:     ventana1=7,  ventana2=6,  ventana3=12
        // purchases: ventana1=1,  ventana2=5,  ventana3=5
        Map<String, long[]> expected = Map.of(
            "clicks",    new long[]{10L, 13L, 8L},
            "views",     new long[]{7L,  6L, 12L},
            "purchases", new long[]{1L,  5L,  5L}
        );
        long[] winStarts = {0L, 10_000L, 20_000L};

        for (Map.Entry<String, long[]> exp : new TreeMap<>(expected).entrySet()) {
            String key = exp.getKey();
            long[] expVals = exp.getValue();
            Map<Long, Long> actual = resultado.getOrDefault(key, Map.of());
            for (int i = 0; i < expVals.length; i++) {
                long actualVal = actual.getOrDefault(winStarts[i], 0L);
                boolean ok = actualVal == expVals[i];
                System.out.printf("%s  %-12s ventana%d: esperado=%d actual=%d%n",
                    ok ? "PASS" : "FAIL", key, i + 1, expVals[i], actualVal);
            }
        }
    }
}
