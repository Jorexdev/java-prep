import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ejercicio4 {

    record Span(String traceId, String spanId, String parentSpanId,
                String name, long startMs, long endMs) {
        long duration() { return endMs - startMs; }
    }

    static class Tracer {
        private final String traceId = UUID.randomUUID().toString().substring(0, 8);
        private final List<Span> spans = new ArrayList<>();
        private final ThreadLocal<String> activeSpanId = new ThreadLocal<>();

        Span startSpan(String name) {
            String spanId = UUID.randomUUID().toString().substring(0, 6);
            String parentId = activeSpanId.get(); // null si es la raíz
            Span span = new Span(traceId, spanId, parentId, name,
                System.currentTimeMillis(), 0);
            spans.add(span);
            activeSpanId.set(spanId);
            return span;
        }

        void finishSpan(Span span) {
            int idx = spans.indexOf(span);
            spans.set(idx, new Span(span.traceId(), span.spanId(), span.parentSpanId(),
                span.name(), span.startMs(), System.currentTimeMillis()));
            // Restaurar el padre como activo
            activeSpanId.set(span.parentSpanId());
        }

        void printTree() {
            System.out.println("\n=== Trace: " + traceId + " ===");
            printNode(null, 0);
        }

        private void printNode(String parentId, int depth) {
            for (Span s : spans) {
                boolean isRoot = parentId == null && s.parentSpanId() == null;
                boolean isChild = parentId != null && parentId.equals(s.parentSpanId());
                if (isRoot || isChild) {
                    System.out.printf("  %s[%s] %s — %dms%n",
                        "  ".repeat(depth), s.spanId(), s.name(), s.duration());
                    printNode(s.spanId(), depth + 1);
                }
            }
        }
    }

    static void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) throws Exception {
        Tracer tracer = new Tracer();

        System.out.println("=== Distributed Tracing — árbol de spans ===");

        // Raíz
        Span root = tracer.startSpan("HTTP GET /api/checkout");
        simulateWork(10);

        // Hijo 1
        Span auth = tracer.startSpan("AuthService.validate");
        simulateWork(5);

            // Nieto 1.1
            Span tokenCheck = tracer.startSpan("TokenRepository.findByToken");
            simulateWork(3);
            tracer.finishSpan(tokenCheck);

        tracer.finishSpan(auth);

        // Hijo 2
        Span payment = tracer.startSpan("PaymentService.charge");
        simulateWork(15);

            // Nieto 2.1
            Span dbWrite = tracer.startSpan("OrderRepository.save");
            simulateWork(4);
            tracer.finishSpan(dbWrite);

            // Nieto 2.2
            Span notify = tracer.startSpan("NotificationService.send");
            simulateWork(6);
            tracer.finishSpan(notify);

        tracer.finishSpan(payment);

        tracer.finishSpan(root);

        tracer.printTree();
    }
}
