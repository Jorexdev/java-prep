public class Ejercicio4 {

    static class LagMonitor {
        private final int numPartitions;
        private final long[] latestOffset;
        private final long[] committedOffset;

        LagMonitor(int numPartitions) {
            this.numPartitions = numPartitions;
            this.latestOffset = new long[numPartitions];
            this.committedOffset = new long[numPartitions];
        }

        void produce(int partition, int count) {
            latestOffset[partition] += count;
        }

        void commit(int partition, long offset) {
            committedOffset[partition] = offset;
        }

        long lag(int partition) {
            return latestOffset[partition] - committedOffset[partition];
        }

        long totalLag() {
            long total = 0;
            for (int i = 0; i < numPartitions; i++) {
                total += lag(i);
            }
            return total;
        }

        void printTable() {
            System.out.printf("%-12s %-10s %-12s %-6s%n", "partition", "latest", "committed", "lag");
            System.out.println("-".repeat(44));
            for (int i = 0; i < numPartitions; i++) {
                System.out.printf("%-12d %-10d %-12d %-6d%n", i, latestOffset[i], committedOffset[i], lag(i));
            }
            System.out.println("-".repeat(44));
            System.out.printf("%-12s %-10s %-12s %-6d%n", "TOTAL", "", "", totalLag());
        }
    }

    public static void main(String[] args) {
        LagMonitor monitor = new LagMonitor(3);

        monitor.produce(0, 10);
        monitor.produce(1, 10);
        monitor.produce(2, 10);
        System.out.println("[PRODUCE] 30 mensajes producidos (10 por partición)");

        monitor.commit(0, 7L);
        monitor.commit(1, 10L);
        System.out.println("[CONSUME] partición 0: 7 procesados, partición 1: 10 procesados, partición 2: 0 procesados");

        System.out.println();
        monitor.printTable();
    }
}
