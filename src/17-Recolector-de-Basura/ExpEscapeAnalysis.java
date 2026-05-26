import java.util.ArrayList;
import java.util.List;

public class ExpEscapeAnalysis {

    // Lightweight value object used in tight loops
    static class Point {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
        int sum() { return x + y; }
    }

    // -----------------------------------------------------------------------
    // Case 1: object does NOT escape the method
    //   JIT can apply scalar replacement: x and y become local int variables,
    //   no heap allocation happens at all.
    // -----------------------------------------------------------------------
    static long noEscape(int iterations) {
        long acc = 0;
        for (int i = 0; i < iterations; i++) {
            Point p = new Point(i, i + 1); // p never returned or stored externally
            acc += p.sum();
        }
        return acc;
    }

    // -----------------------------------------------------------------------
    // Case 2: object escapes to a list → must be heap-allocated
    //   Escape analysis cannot elide the allocation; GC pressure increases.
    // -----------------------------------------------------------------------
    static long escape(int iterations) {
        List<Point> points = new ArrayList<>(iterations);
        for (int i = 0; i < iterations; i++) {
            points.add(new Point(i, i + 1)); // stored in list → escapes
        }
        long acc = 0;
        for (Point p : points) acc += p.sum();
        return acc;
    }

    // -----------------------------------------------------------------------
    // Case 3: object returned from method → escapes, heap-allocated
    // -----------------------------------------------------------------------
    static Point createPoint(int x, int y) {
        return new Point(x, y); // return = escape; JIT cannot stack-allocate
    }

    // -----------------------------------------------------------------------
    // Benchmark: compare wall-clock time for both cases
    // -----------------------------------------------------------------------
    static void benchmark(int warmup, int iterations) {
        // Warm-up: let JIT compile both paths
        for (int i = 0; i < warmup; i++) {
            noEscape(1000);
            escape(1000);
        }

        long t1 = System.nanoTime();
        long r1 = noEscape(iterations);
        long noEscapeMs = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        long r2 = escape(iterations);
        long escapeMs = (System.nanoTime() - t2) / 1_000_000;

        // Print results to prevent dead-code elimination by JIT
        System.out.printf("  noEscape result=%d  time=%d ms%n", r1, noEscapeMs);
        System.out.printf("  escape   result=%d  time=%d ms%n", r2, escapeMs);
        System.out.printf("  Heap pressure ratio: noEscape is ~%.1fx faster%n",
                escapeMs > 0 ? (double) escapeMs / noEscapeMs : 1.0);
    }

    public static void main(String[] args) {
        System.out.println("=== Escape Analysis Demo ===");
        System.out.println();

        System.out.println("--- Case 1: Point used locally (no escape) ---");
        System.out.println("  JIT may apply scalar replacement: Point.x and Point.y");
        System.out.println("  become CPU registers — zero heap allocation.");
        long sum1 = noEscape(5);
        System.out.println("  Sample sum: " + sum1);

        System.out.println();
        System.out.println("--- Case 2: Point stored in list (escapes) ---");
        System.out.println("  JIT must allocate each Point on the heap; GC must later collect them.");
        long sum2 = escape(5);
        System.out.println("  Sample sum: " + sum2);

        System.out.println();
        System.out.println("--- Case 3: Point returned from method (escapes) ---");
        Point p = createPoint(3, 4);
        System.out.println("  Returned point sum: " + p.sum());

        System.out.println();
        System.out.println("--- Benchmark (warmup=200, iterations=2_000_000) ---");
        System.out.println("  Note: results vary; with -server JIT the gap can be significant.");
        benchmark(200, 2_000_000);

        System.out.println();
        System.out.println("--- JVM flags for escape analysis ---");
        System.out.println("  -XX:+DoEscapeAnalysis       (default: on in server JIT)");
        System.out.println("  -XX:+EliminateAllocations   (scalar replacement, default: on)");
        System.out.println("  -XX:-DoEscapeAnalysis       → disable to see allocation cost");
        System.out.println("  -XX:+PrintEscapeAnalysis    → verbose output (debug JVM builds)");
    }
}
