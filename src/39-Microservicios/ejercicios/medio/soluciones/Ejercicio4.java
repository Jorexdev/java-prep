import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ejercicio4 {

    static class TraceContext {
        final String traceId;
        final String spanId;
        final String parentSpanId;
        final String operation;
        final List<TraceContext> children = new ArrayList<>();

        TraceContext(String traceId, String spanId, String parentSpanId, String operation) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.parentSpanId = parentSpanId;
            this.operation = operation;
        }
    }

    static class Tracer {
        private static final ThreadLocal<TraceContext> current = new ThreadLocal<>();

        static TraceContext newTrace(String operation) {
            TraceContext ctx = new TraceContext(
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                null,
                operation
            );
            current.set(ctx);
            return ctx;
        }

        static TraceContext newSpan(String operation) {
            TraceContext parent = current.get();
            TraceContext child = new TraceContext(
                parent != null ? parent.traceId : UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                parent != null ? parent.spanId : null,
                operation
            );
            if (parent != null) parent.children.add(child);
            current.set(child);
            return child;
        }

        static void restoreContext(TraceContext ctx) {
            current.set(ctx);
        }

        static TraceContext current() { return current.get(); }
    }

    static class PaymentService {
        TraceContext cobrar(TraceContext parent) {
            Tracer.restoreContext(parent);
            TraceContext span = Tracer.newSpan("PaymentService.cobrar");
            System.out.println("    PaymentService: procesando cobro [span=" + span.spanId + "]");
            return span;
        }
    }

    static class InventoryService {
        private final PaymentService paymentService = new PaymentService();

        TraceContext reservar(TraceContext parent) {
            Tracer.restoreContext(parent);
            TraceContext span = Tracer.newSpan("InventoryService.reservar");
            System.out.println("  InventoryService: reservando stock [span=" + span.spanId + "]");
            paymentService.cobrar(span);
            return span;
        }
    }

    static class OrderService {
        private final InventoryService inventoryService = new InventoryService();

        TraceContext crearPedido() {
            TraceContext span = Tracer.newSpan("OrderService.crearPedido");
            System.out.println("OrderService: creando pedido [span=" + span.spanId + "]");
            inventoryService.reservar(span);
            return span;
        }
    }

    static void printTree(TraceContext ctx, int depth) {
        String indent = "  ".repeat(depth);
        System.out.printf("%s[%s] traceId=%s spanId=%s parent=%s%n",
            indent, ctx.operation, ctx.traceId, ctx.spanId,
            ctx.parentSpanId != null ? ctx.parentSpanId : "ROOT");
        for (TraceContext child : ctx.children) {
            printTree(child, depth + 1);
        }
    }

    public static void main(String[] args) {
        TraceContext root = Tracer.newTrace("request-entrada");
        System.out.println("Iniciando traza: " + root.traceId);
        System.out.println();

        OrderService orderService = new OrderService();
        Tracer.restoreContext(root);
        TraceContext orderSpan = orderService.crearPedido();

        root.children.add(orderSpan);

        System.out.println("\n=== Árbol de spans ===");
        printTree(root, 0);
    }
}
