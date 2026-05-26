import java.util.*;

public class ExpGCTuning {

    // -----------------------------------------------------------------------
    // HeapSimulator — models young/old generation sizes and GC events
    // -----------------------------------------------------------------------
    static class HeapSimulator {
        private static final int YOUNG_MAX   = 100; // arbitrary units
        private static final int OLD_MAX     = 300;
        private static final int LARGE_THRESHOLD = 30; // objects >= this go straight to old gen

        private int youngUsed = 0;
        private int oldUsed   = 0;
        private int minorGCs  = 0;
        private int majorGCs  = 0;

        // G1GC: heap divided into equal-sized regions — each region can be any generation type.
        // We model this as a flat list of region "labels" for illustration.
        private final List<String> g1Regions = new ArrayList<>();

        HeapSimulator() {
            // Pre-populate 10 G1 regions
            for (int i = 0; i < 10; i++) g1Regions.add("Free");
        }

        void allocate(String name, int size) {
            if (size >= LARGE_THRESHOLD) {
                // Large objects skip young gen entirely → humongous region in G1 / old gen elsewhere
                System.out.printf("  ALLOC %-20s size=%-3d → OldGen (large object bypass)%n", name, size);
                oldUsed += size;
                markG1Region("Humongous");
            } else {
                System.out.printf("  ALLOC %-20s size=%-3d → YoungGen (Eden)%n", name, size);
                youngUsed += size;
                markG1Region("Eden");
            }

            if (youngUsed >= YOUNG_MAX) minorGC();
            if (oldUsed   >= OLD_MAX)   majorGC();
        }

        private void minorGC() {
            minorGCs++;
            int survived = youngUsed / 4; // 75% short-lived, 25% promoted
            System.out.printf("  [Minor GC #%d] YoungGen full (%d). Cleared %d, promoted %d to OldGen%n",
                    minorGCs, youngUsed, youngUsed - survived, survived);
            oldUsed   += survived;
            youngUsed  = 0;
            updateG1Region("Eden", "OldGen", survived > 0 ? 1 : 0);
        }

        private void majorGC() {
            majorGCs++;
            System.out.printf("  [Major GC #%d] OldGen full (%d/%d). Full collection triggered.%n",
                    majorGCs, oldUsed, OLD_MAX);
            oldUsed = 0; // simplified: all old-gen objects collected
            youngUsed = 0;
            g1Regions.replaceAll(r -> "Free");
        }

        private void markG1Region(String label) {
            for (int i = 0; i < g1Regions.size(); i++) {
                if ("Free".equals(g1Regions.get(i))) { g1Regions.set(i, label); return; }
            }
        }

        private void updateG1Region(String from, String to, int count) {
            int changed = 0;
            for (int i = 0; i < g1Regions.size() && changed < count; i++) {
                if (from.equals(g1Regions.get(i))) { g1Regions.set(i, to); changed++; }
            }
        }

        void printStatus() {
            System.out.printf("  Heap: Young=%d/%d  Old=%d/%d  MinorGCs=%d  MajorGCs=%d%n",
                    youngUsed, YOUNG_MAX, oldUsed, OLD_MAX, minorGCs, majorGCs);
            System.out.println("  G1 regions: " + g1Regions);
        }

        void printSummary() {
            System.out.printf("%n  === GC Summary ===%n");
            System.out.printf("  Minor GCs: %d  |  Major GCs: %d%n", minorGCs, majorGCs);
            System.out.printf("  Tip: high minor GC rate → allocating too fast / young gen too small%n");
            System.out.printf("  Tip: major GC → long pauses; tune old gen size or use G1/ZGC%n");
        }
    }

    // -----------------------------------------------------------------------
    // Scenario: small short-lived objects (high minor GC rate)
    // -----------------------------------------------------------------------
    static void scenarioHighAllocationRate(HeapSimulator sim) {
        System.out.println("\n--- Scenario: high allocation rate (small objects) ---");
        for (int i = 0; i < 20; i++) {
            sim.allocate("request-" + i, 10);
        }
    }

    // -----------------------------------------------------------------------
    // Scenario: large object allocation bypasses young gen
    // -----------------------------------------------------------------------
    static void scenarioLargeObjects(HeapSimulator sim) {
        System.out.println("\n--- Scenario: large object bypass ---");
        sim.allocate("big-image-1",  50);
        sim.allocate("big-image-2",  40);
        sim.allocate("small-dto",    5);
        sim.allocate("big-payload",  80);
    }

    // -----------------------------------------------------------------------
    // Real JVM: actual heap usage
    // -----------------------------------------------------------------------
    static void realHeapSnapshot() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free  = rt.freeMemory();
        long used  = total - free;
        System.out.printf("%n--- Real JVM Heap Snapshot ---%n");
        System.out.printf("  Total: %,d KB  |  Used: %,d KB  |  Free: %,d KB%n",
                total / 1024, used / 1024, free / 1024);
        System.out.printf("  Max:   %,d KB%n", rt.maxMemory() / 1024);
    }

    public static void main(String[] args) {
        System.out.println("=== GC Tuning Simulation ===");
        System.out.println("Key JVM flags (not set here, shown for reference):");
        System.out.println("  -Xms256m -Xmx1g          → initial/max heap");
        System.out.println("  -XX:NewRatio=2            → OldGen 2x YoungGen");
        System.out.println("  -XX:+UseG1GC              → G1 garbage collector");
        System.out.println("  -XX:MaxGCPauseMillis=200  → G1 pause target");
        System.out.println("  -Xlog:gc*                 → verbose GC log");

        HeapSimulator sim = new HeapSimulator();

        scenarioHighAllocationRate(sim);
        sim.printStatus();

        scenarioLargeObjects(sim);
        sim.printStatus();

        sim.printSummary();

        realHeapSnapshot();
    }
}
