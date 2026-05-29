import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Correlacion de logs entre servicios con trace-id propagado a hilos hijos

public class Ejercicio5 {

    // ====== Trace context: trace-id + span-id propagables entre threads ======

    static class TraceContext {
        final String traceId;
        final String spanId;
        final String parentSpanId; // null si es el span raiz

        TraceContext(String traceId, String spanId, String parentSpanId) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.parentSpanId = parentSpanId;
        }

        // Crea un span hijo (hereda traceId, nuevo spanId, parent = este span)
        TraceContext createChild(String childSpanId) {
            return new TraceContext(traceId, childSpanId, spanId);
        }

        @Override public String toString() {
            return String.format("[trace=%s span=%s parent=%s]",
                    traceId, spanId, parentSpanId != null ? parentSpanId : "root");
        }
    }

    // ThreadLocal para el contexto del trace activo en este hilo
    static final ThreadLocal<TraceContext> CURRENT_TRACE = new ThreadLocal<>();

    static class TracePropagator {
        private static final AtomicInteger SPAN_COUNTER = new AtomicInteger(0);

        static String newSpanId() {
            return "s" + String.format("%03d", SPAN_COUNTER.incrementAndGet());
        }

        static String newTraceId() {
            return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        // Obtiene el trace actual del hilo
        static TraceContext current() {
            return CURRENT_TRACE.get();
        }

        // Inicia un nuevo trace (span raiz)
        static TraceContext startTrace() {
            TraceContext ctx = new TraceContext(newTraceId(), newSpanId(), null);
            CURRENT_TRACE.set(ctx);
            return ctx;
        }

        // Crea un span hijo a partir del contexto actual
        static TraceContext startChildSpan() {
            TraceContext parent = CURRENT_TRACE.get();
            if (parent == null) return startTrace(); // sin trace, empezar uno nuevo
            TraceContext child = parent.createChild(newSpanId());
            CURRENT_TRACE.set(child);
            return child;
        }

        // Restaura el contexto padre tras finalizar el span hijo
        static void restoreParent(TraceContext parent) {
            if (parent == null) CURRENT_TRACE.remove();
            else CURRENT_TRACE.set(parent);
        }

        // Limpia el trace del hilo actual
        static void clear() { CURRENT_TRACE.remove(); }

        // Instala un contexto dado en el hilo actual (para propagacion a hilos hijos)
        static void install(TraceContext ctx) { CURRENT_TRACE.set(ctx); }
    }

    // ====== Logger que incluye trace context automaticamente ======

    static class TraceLogger {
        private final String service;

        TraceLogger(String service) { this.service = service; }

        void log(String level, String message) {
            TraceContext ctx = TracePropagator.current();
            String traceInfo = ctx != null
                    ? String.format("[%s span=%s]", ctx.traceId, ctx.spanId)
                    : "[no-trace]";
            System.out.printf("  %-12s %-25s %-8s %s%n",
                    Thread.currentThread().getName(), traceInfo, level, service + ": " + message);
        }

        void info(String msg)  { log("INFO",  msg); }
        void debug(String msg) { log("DEBUG", msg); }
        void error(String msg) { log("ERROR", msg); }
    }

    // ====== Servicios que se llaman entre si ======

    static class GatewayService {
        private final TraceLogger log = new TraceLogger("GatewayService");
        private final OrderService orderService = new OrderService();
        private final AuthService authService   = new AuthService();

        void handleRequest(String customerId, String productId) throws Exception {
            // El trace ya esta instalado por el caller (main thread)
            log.info("Recibida solicitud para customerId=" + customerId);

            // Llamar auth en un VT hijo: propagamos el trace
            TraceContext currentCtx = TracePropagator.current();
            CompletableFuture<Boolean> authFuture = CompletableFuture.supplyAsync(() -> {
                TracePropagator.install(currentCtx.createChild(TracePropagator.newSpanId()));
                try {
                    return authService.authenticate(customerId);
                } finally {
                    TracePropagator.clear();
                }
            }, Executors.newVirtualThreadPerTaskExecutor());

            boolean authenticated = authFuture.get();
            if (!authenticated) {
                log.error("Autenticacion fallida para customerId=" + customerId);
                return;
            }

            // Llamar order service (en el mismo hilo, span hijo)
            TraceContext parent = TracePropagator.current();
            TracePropagator.install(parent.createChild(TracePropagator.newSpanId()));
            try {
                orderService.createOrder(customerId, productId);
            } finally {
                TracePropagator.restoreParent(parent);
            }

            log.info("Solicitud completada para customerId=" + customerId);
        }
    }

    static class AuthService {
        private final TraceLogger log = new TraceLogger("AuthService");

        boolean authenticate(String customerId) {
            log.debug("Verificando token para " + customerId);
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            log.info("Autenticacion OK para " + customerId);
            return true;
        }
    }

    static class OrderService {
        private final TraceLogger log = new TraceLogger("OrderService");
        private final InventoryService inventoryService = new InventoryService();
        private final PaymentService paymentService     = new PaymentService();

        void createOrder(String customerId, String productId) throws Exception {
            log.info("Creando orden: customer=" + customerId + " product=" + productId);

            // Llamar inventory (span hijo)
            TraceContext parent = TracePropagator.current();

            TracePropagator.install(parent.createChild(TracePropagator.newSpanId()));
            try {
                inventoryService.checkStock(productId);
            } finally {
                TracePropagator.restoreParent(parent);
            }

            // Llamar payment (span hijo)
            TracePropagator.install(parent.createChild(TracePropagator.newSpanId()));
            try {
                paymentService.charge(customerId, 99.99);
            } finally {
                TracePropagator.restoreParent(parent);
            }

            log.info("Orden creada correctamente");
        }
    }

    static class InventoryService {
        private final TraceLogger log = new TraceLogger("InventoryService");

        void checkStock(String productId) {
            log.debug("Verificando stock de " + productId);
            try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            log.info("Stock disponible: " + productId + " (qty=42)");
        }
    }

    static class PaymentService {
        private final TraceLogger log = new TraceLogger("PaymentService");

        void charge(String customerId, double amount) {
            log.debug("Iniciando cobro a " + customerId + " por " + amount);
            try { Thread.sleep(15); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            log.info("Cobro procesado: " + customerId + " -> " + amount + " EUR");
        }
    }

    // ====== DEMO ======

    public static void main(String[] args) throws Exception {
        System.out.println("=== Correlacion de logs: trace-id propagado a hilos hijos ===");
        System.out.println();
        System.out.printf("  %-12s %-25s %-8s %s%n",
                "Thread", "Trace context", "Level", "Mensaje");
        System.out.println("  " + "-".repeat(80));

        GatewayService gateway = new GatewayService();

        // --- Request 1: flujo completo ---
        TraceContext trace1 = TracePropagator.startTrace();
        System.out.println("  --- Request 1 (traceId=" + trace1.traceId + ") ---");
        gateway.handleRequest("cust-001", "prod-A");
        TracePropagator.clear();
        System.out.println();

        // --- Request 2: otro traceId independiente ---
        TraceContext trace2 = TracePropagator.startTrace();
        System.out.println("  --- Request 2 (traceId=" + trace2.traceId + ") ---");
        gateway.handleRequest("cust-002", "prod-B");
        TracePropagator.clear();
        System.out.println();

        // --- Demo: 3 requests concurrentes, cada uno con su traceId ---
        System.out.println("  --- 3 requests concurrentes ---");
        CountDownLatch latch = new CountDownLatch(3);
        for (int i = 1; i <= 3; i++) {
            final String custId = "cust-10" + i;
            final String prodId = "prod-X" + i;
            Thread.ofVirtual().name("vt-req-" + i).start(() -> {
                TracePropagator.startTrace();
                try {
                    gateway.handleRequest(custId, prodId);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                } finally {
                    TracePropagator.clear();
                    latch.countDown();
                }
            });
        }
        latch.await();
        System.out.println();

        System.out.println("=== Analisis ===");
        System.out.println("Cada request tiene un traceId unico propagado a todos sus spans.");
        System.out.println("Los spans hijos heredan el traceId pero tienen su propio spanId.");
        System.out.println("Al filtrar por traceId en un log aggregator (ELK, Loki),");
        System.out.println("  se ven todos los logs de todos los servicios para esa peticion.");
        System.out.println("Los requests concurrentes tienen traceIds distintos: no hay contaminacion.");
        System.out.println();
        System.out.println("En produccion: Micrometer Tracing + Zipkin/Jaeger propagan");
        System.out.println("  W3C TraceContext headers (traceparent) entre microservicios.");
    }
}
