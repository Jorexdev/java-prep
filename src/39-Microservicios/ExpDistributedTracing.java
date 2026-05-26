import java.util.*;

/**
 * Simulación de Distributed Tracing estilo OpenTelemetry con Java puro.
 *
 * Conceptos demostrados:
 *  - Trace: árbol de spans que representa una operación end-to-end
 *  - Span: unidad de trabajo con traceId, spanId, parentSpanId y timings
 *  - Context propagation: traceId viaja en headers HTTP (traceparent)
 *  - SpanExporter: imprime el árbol de spans formateado
 *  - Análisis: detectar spans lentos en el árbol
 */
public class ExpDistributedTracing {

    // ─────────────────────────────────────────────
    // SPAN
    // ─────────────────────────────────────calls───
    // ─────────────────────────────────────────────

    static class Span {
        private final String traceId;
        private final String spanId;
        private final String parentSpanId; // null si es root span
        private final String operationName;
        private final String serviceName;
        private final long startNanos;
        private long endNanos = -1;
        private final Map<String, String> attributes = new LinkedHashMap<>();
        private String status = "OK";

        Span(String traceId, String spanId, String parentSpanId,
             String operationName, String serviceName) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.parentSpanId = parentSpanId;
            this.operationName = operationName;
            this.serviceName = serviceName;
            this.startNanos = System.nanoTime();
        }

        void end() { endNanos = System.nanoTime(); }
        void setStatus(String status) { this.status = status; }
        void setAttribute(String key, String value) { attributes.put(key, value); }

        long durationMicros() {
            if (endNanos < 0) return -1;
            return (endNanos - startNanos) / 1_000;
        }

        boolean isRoot() { return parentSpanId == null; }

        String traceId() { return traceId; }
        String spanId() { return spanId; }
        String parentSpanId() { return parentSpanId; }
        String operationName() { return operationName; }
        String serviceName() { return serviceName; }
        String status() { return status; }
        Map<String, String> attributes() { return attributes; }

        // Genera el header traceparent: version-traceId-spanId-flags (W3C TraceContext)
        String traceparent() {
            return String.format("00-%s-%s-01", traceId, spanId);
        }
    }

    // ─────────────────────────────────────────────
    // TRACER: crea y almacena spans
    // ─────────────────────────────────────────────

    static class Tracer {
        private final String serviceName;
        private final SpanExporter exporter;
        // Lista global de spans (en producción: exportados async al collector)
        private final List<Span> completedSpans;

        Tracer(String serviceName, SpanExporter exporter, List<Span> completedSpans) {
            this.serviceName = serviceName;
            this.exporter = exporter;
            this.completedSpans = completedSpans;
        }

        // Iniciar un span raíz (nuevo trace)
        Span startRootSpan(String operationName) {
            String traceId = generateId(16);
            String spanId  = generateId(8);
            Span span = new Span(traceId, spanId, null, operationName, serviceName);
            System.out.printf("[%s] → START span='%s' traceId='%s'%n",
                    serviceName, operationName, traceId);
            return span;
        }

        // Iniciar un span hijo con el contexto del padre (propagado por traceparent)
        Span startSpan(String operationName, String parentTraceparent) {
            String[] parts = parentTraceparent.split("-");
            String traceId = parts[1]; // misma traza
            String parentSpanId = parts[2];
            String spanId = generateId(8);
            Span span = new Span(traceId, spanId, parentSpanId, operationName, serviceName);
            System.out.printf("[%s] → START span='%s' parent='%s'%n",
                    serviceName, operationName, parentSpanId);
            return span;
        }

        void endSpan(Span span) {
            span.end();
            completedSpans.add(span);
            System.out.printf("[%s] ← END   span='%s' duration=%dµs status=%s%n",
                    serviceName, span.operationName(), span.durationMicros(), span.status());
        }

        private String generateId(int hexChars) {
            // ID corto y legible para la demo (no criptográfico)
            StringBuilder sb = new StringBuilder();
            Random rnd = new Random();
            for (int i = 0; i < hexChars; i++) {
                sb.append(Integer.toHexString(rnd.nextInt(16)));
            }
            return sb.toString();
        }
    }

    // ─────────────────────────────────────────────
    // SPAN EXPORTER: imprime el árbol de traza
    // ─────────────────────────────────────────────

    static class SpanExporter {
        private final List<Span> spans; // referencia al mismo store del tracer

        SpanExporter(List<Span> spans) {
            this.spans = spans;
        }

        void printTrace(String traceId) {
            System.out.println("\n" + "═".repeat(65));
            System.out.printf("  TRACE %s%n", traceId);
            System.out.println("═".repeat(65));

            // Encontrar el root span
            Optional<Span> rootOpt = spans.stream()
                    .filter(s -> s.traceId().equals(traceId) && s.isRoot())
                    .findFirst();

            if (rootOpt.isEmpty()) {
                System.out.println("  (trace no encontrado)");
                return;
            }

            Span root = rootOpt.get();
            long totalDuration = root.durationMicros();
            printSpanTree(traceId, root.spanId(), 0, totalDuration);

            System.out.println("─".repeat(65));
            System.out.printf("  Total trace duration: %dµs%n", totalDuration);
        }

        private void printSpanTree(String traceId, String spanId, int depth, long totalDuration) {
            Optional<Span> spanOpt = spans.stream()
                    .filter(s -> s.traceId().equals(traceId) && s.spanId().equals(spanId))
                    .findFirst();
            if (spanOpt.isEmpty()) return;

            Span span = spanOpt.get();
            String indent = "  " + "  ".repeat(depth);
            long dur = span.durationMicros();
            double pct = totalDuration > 0 ? (dur * 100.0 / totalDuration) : 0;
            String slowMark = dur > 500 ? " ⚡SLOW" : ""; // marcar spans lentos

            System.out.printf("%s[%s] %s  (%dµs, %.0f%%)%s%n",
                    indent, span.serviceName(), span.operationName(), dur, pct, slowMark);

            if (!span.attributes().isEmpty()) {
                span.attributes().forEach((k, v) ->
                    System.out.printf("%s  attr: %s=%s%n", indent, k, v));
            }

            // Imprimir hijos recursivamente
            spans.stream()
                    .filter(s -> s.traceId().equals(traceId) && spanId.equals(s.parentSpanId()))
                    .forEach(child -> printSpanTree(traceId, child.spanId(), depth + 1, totalDuration));
        }

        List<Span> findSlowSpans(String traceId, long thresholdMicros) {
            return spans.stream()
                    .filter(s -> s.traceId().equals(traceId) && s.durationMicros() > thresholdMicros)
                    .sorted(Comparator.comparingLong(Span::durationMicros).reversed())
                    .toList();
        }
    }

    // ─────────────────────────────────────────────
    // SERVICIOS SIMULADOS
    // ─────────────────────────────────────────────

    static class ApiGatewayService {
        private final Tracer tracer;

        ApiGatewayService(Tracer tracer) { this.tracer = tracer; }

        String handleRequest(String requestPath, ServiceA serviceA) {
            Span span = tracer.startRootSpan("HTTP GET " + requestPath);
            span.setAttribute("http.method", "GET");
            span.setAttribute("http.path", requestPath);
            span.setAttribute("http.client_ip", "10.0.0.42");

            try {
                // Llamar a service-A, pasando el contexto de traza
                String result = serviceA.process(span.traceparent());

                span.setAttribute("http.status_code", "200");
                return result;
            } finally {
                tracer.endSpan(span);
            }
        }
    }

    static class ServiceA {
        private final Tracer tracer;

        ServiceA(Tracer tracer) { this.tracer = tracer; }

        String process(String incomingTraceparent) {
            Span span = tracer.startSpan("processOrder", incomingTraceparent);
            span.setAttribute("service.component", "order-processor");

            try {
                // Simular algo de trabajo
                simulateWork(200);

                // Llamar a service-B
                ServiceB serviceB = new ServiceB(new Tracer("service-b", null, extractSharedStore()));
                String dbResult = serviceB.queryDatabase(span.traceparent());

                return "order-processed: " + dbResult;
            } finally {
                tracer.endSpan(span);
            }
        }

        // Hack para la demo: compartir la lista global de spans
        private List<Span> extractSharedStore() {
            // En una implementación real el tracer inyecta el store global
            return GLOBAL_SPANS;
        }
    }

    static class ServiceB {
        private final Tracer tracer;

        ServiceB(Tracer tracer) { this.tracer = tracer; }

        String queryDatabase(String incomingTraceparent) {
            Span span = tracer.startSpan("queryDatabase", incomingTraceparent);
            span.setAttribute("db.system", "postgresql");
            span.setAttribute("db.statement", "SELECT * FROM orders WHERE id=?");

            try {
                // Simular query lenta para demostrar detección de slow spans
                simulateWork(800);
                return "row{id=123, status=confirmed}";
            } finally {
                tracer.endSpan(span);
            }
        }
    }

    // Store global de spans para que todos los tracers de la demo compartan el mismo
    static final List<Span> GLOBAL_SPANS = new ArrayList<>();

    static void simulateWork(long micros) {
        long nanos = micros * 1_000L;
        long start = System.nanoTime();
        // Busy-wait para simular duración sin Thread.sleep (precisión en µs)
        while (System.nanoTime() - start < nanos) { /* spin */ }
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  DISTRIBUTED TRACING — Java puro (OpenTelemetry style)");
        System.out.println("═".repeat(65));

        SpanExporter exporter = new SpanExporter(GLOBAL_SPANS);
        Tracer gatewayTracer = new Tracer("api-gateway", exporter, GLOBAL_SPANS);
        Tracer serviceATracer = new Tracer("service-a", exporter, GLOBAL_SPANS);

        ApiGatewayService gateway = new ApiGatewayService(gatewayTracer);
        ServiceA serviceA = new ServiceA(serviceATracer);

        // ── Demo: un request que atraviesa 3 servicios ─────────────────
        System.out.println("\n── Ejecutando request: GET /api/orders/123 ──");
        String result = gateway.handleRequest("/api/orders/123", serviceA);
        System.out.printf("  Resultado: %s%n", result);

        // Recuperar el traceId del root span para imprimir la traza
        String traceId = GLOBAL_SPANS.stream()
                .filter(Span::isRoot)
                .findFirst()
                .map(Span::traceId)
                .orElse("unknown");

        // ── Imprimir el árbol de traza ─────────────────────────────────
        exporter.printTrace(traceId);

        // ── Detectar spans lentos ──────────────────────────────────────
        System.out.println("\n── Análisis: spans lentos (> 500µs) ──");
        List<Span> slowSpans = exporter.findSlowSpans(traceId, 500);
        if (slowSpans.isEmpty()) {
            System.out.println("  No hay spans lentos");
        } else {
            slowSpans.forEach(s ->
                System.out.printf("  SLOW: [%s] %s — %dµs%n",
                        s.serviceName(), s.operationName(), s.durationMicros()));
            System.out.println("  → Investigar: " + slowSpans.get(0).serviceName() +
                    " / " + slowSpans.get(0).operationName());
        }

        // ── Mostrar context propagation ────────────────────────────────
        System.out.println("\n── Context propagation via HTTP header ──");
        Span rootSpan = GLOBAL_SPANS.stream().filter(Span::isRoot).findFirst().orElseThrow();
        System.out.println("  Header propagado entre servicios:");
        System.out.printf("    traceparent: %s%n", rootSpan.traceparent());
        System.out.println("  Formato W3C: version-traceId-spanId-flags");
        System.out.println("  → traceId es el mismo en todos los servicios del request");
        System.out.println("  → spanId identifica el span actual (el hijo pone su propio spanId)");

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN DISTRIBUTED TRACING");
        System.out.println("═".repeat(65));
        System.out.println("  traceId    → único por request end-to-end (cross-service)");
        System.out.println("  spanId     → único por operación dentro de la traza");
        System.out.println("  traceparent → header HTTP W3C para propagar contexto");
        System.out.println("  Uso: identificar dónde se gasta el tiempo, ver cascadas de fallos");
        System.out.println("  Herramientas: Jaeger, Zipkin, AWS X-Ray, Grafana Tempo");
        System.out.println("═".repeat(65));
    }
}
