import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// OpenTelemetry (OTel) es el estándar de observabilidad vendor-neutral:
//   - Traces (spans) + Metrics + Logs con una sola instrumentación
//   - El Collector recibe la telemetría y la reenvía a Jaeger, Zipkin, Datadog, etc.
//
// Relación con Zipkin / Jaeger:
//   - Zipkin es un backend de tracing (almacena y visualiza trazas). Protocolo propio (B3).
//   - Jaeger es otro backend (más reciente, escala mejor). Soporta OTel natively.
//   - OTel es solo el SDK de instrumentación — no almacena nada por sí mismo.
//   - Puedes enviar trazas OTel a Jaeger O a Zipkin configurando el exporter.
//
// Propagación de contexto:
//   - W3C TraceContext (traceparent): estándar moderno, soportado por OTel por defecto.
//   - B3 (Zipkin): cabeceras X-B3-TraceId / X-B3-SpanId / X-B3-Sampled.
//   - OTel soporta ambos formatos — se configura el propagator.
public class ExpOpenTelemetry {

    // ── Span ──────────────────────────────────────────────────────────────────

    static class Span {
        private final String traceId;
        private final String spanId;
        private final String parentSpanId;  // null en el root span
        private final String name;
        private final long   startNs;
        private       long   endNs = -1;
        private final Map<String, String> attributes = new LinkedHashMap<>();
        private final List<SpanEvent>     events     = new ArrayList<>();
        private       String              status     = "OK";

        Span(String traceId, String spanId, String parentSpanId, String name) {
            this.traceId     = traceId;
            this.spanId      = spanId;
            this.parentSpanId = parentSpanId;
            this.name        = name;
            this.startNs     = System.nanoTime();
        }

        void setAttribute(String k, String v) { attributes.put(k, v); }
        void addEvent(String eventName)        { events.add(new SpanEvent(eventName)); }
        void setStatus(String s)               { status = s; }
        void end()                             { endNs = System.nanoTime(); }

        long durationMs() { return endNs < 0 ? -1 : (endNs - startNs) / 1_000_000; }
        boolean isRoot()  { return parentSpanId == null; }

        // ── W3C traceparent ─────────────────────────────────────────────────
        // Formato: 00-{traceId:32hex}-{spanId:16hex}-{flags:2hex}
        // flags: 01 = sampled (incluir en tracing backend)
        String traceparent() {
            return "00-" + traceId + "-" + spanId + "-01";
        }

        // ── B3 headers (Zipkin) ─────────────────────────────────────────────
        // B3 multi-header: cabeceras separadas por cada campo
        // B3 single-header: X-B3-TraceId: {traceId}-{spanId}-1
        Map<String, String> b3Headers() {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("X-B3-TraceId",  traceId);
            headers.put("X-B3-SpanId",   spanId);
            if (parentSpanId != null) {
                headers.put("X-B3-ParentSpanId", parentSpanId);
            }
            headers.put("X-B3-Sampled",  "1");
            return headers;
        }

        // ── Getters mínimos ─────────────────────────────────────────────────
        String traceId()      { return traceId; }
        String spanId()       { return spanId; }
        String parentSpanId() { return parentSpanId; }
        String name()         { return name; }
        String status()       { return status; }
        Map<String, String> attributes() { return attributes; }
        List<SpanEvent> events()         { return events; }
    }

    record SpanEvent(String name, long timestampNs) {
        SpanEvent(String name) { this(name, System.nanoTime()); }
    }

    // ── Tracer + SpanContext ───────────────────────────────────────────────────

    // En OTel: Tracer es stateless — el SpanContext viaja en el Context (ThreadLocal).
    // Aquí usamos ThreadLocal directamente para simular Context propagation.
    static class Tracer {
        private static final AtomicInteger  idCounter = new AtomicInteger(0);
        private static final ThreadLocal<Span> activeSpan = new ThreadLocal<>();
        // Store compartido de spans completados (equivale al batch exporter)
        private final List<Span> completedSpans;

        Tracer(List<Span> store) { this.completedSpans = store; }

        // ── Iniciar root span (nuevo trace) ─────────────────────────────────
        Span startRootSpan(String name) {
            String traceId = newId(32);
            String spanId  = newId(16);
            Span span = new Span(traceId, spanId, null, name);
            activeSpan.set(span);
            return span;
        }

        // ── Iniciar span hijo desde traceparent (W3C) ──────────────────────
        Span startSpanFromTraceparent(String name, String traceparent) {
            String[] parts   = traceparent.split("-");
            String traceId   = parts[1];
            String parentId  = parts[2];
            Span span = new Span(traceId, newId(16), parentId, name);
            activeSpan.set(span);
            return span;
        }

        // ── Iniciar span hijo desde B3 headers (Zipkin) ────────────────────
        Span startSpanFromB3(String name, Map<String, String> b3) {
            String traceId  = b3.get("X-B3-TraceId");
            String parentId = b3.get("X-B3-SpanId");
            Span span = new Span(traceId, newId(16), parentId, name);
            activeSpan.set(span);
            return span;
        }

        void endSpan(Span span) {
            span.end();
            completedSpans.add(span);
            activeSpan.remove();
        }

        Span currentSpan() { return activeSpan.get(); }

        private String newId(int hexLen) {
            StringBuilder sb = new StringBuilder();
            Random rnd = new Random();
            for (int i = 0; i < hexLen; i++) sb.append(Integer.toHexString(rnd.nextInt(16)));
            return sb.toString();
        }
    }

    // ── OTLP Exporter simulado ────────────────────────────────────────────────

    // En producción: OTLPGrpcSpanExporter envía spans al OpenTelemetry Collector,
    // que los reenvía al backend configurado (Jaeger, Zipkin, Tempo, Datadog...).
    static class OtlpExporter {
        void export(List<Span> spans) {
            System.out.println("\n── OTLP Export → OpenTelemetry Collector ─────────────────");
            Map<String, List<Span>> byTrace = new LinkedHashMap<>();
            for (Span s : spans) {
                byTrace.computeIfAbsent(s.traceId(), k -> new ArrayList<>()).add(s);
            }
            byTrace.forEach((traceId, traceSpans) -> {
                System.out.printf("  TRACE %s (%d spans)%n", traceId, traceSpans.size());
                for (Span s : traceSpans) {
                    String indent = s.isRoot() ? "    " : "      └─ ";
                    System.out.printf("%s[%s] span=%s duration=%dms status=%s%n",
                            indent, s.isRoot() ? "ROOT" : "CHILD", s.name(), s.durationMs(), s.status());
                    s.attributes().forEach((k, v) ->
                            System.out.printf("%s    attr: %s=%s%n", indent, k, v));
                    s.events().forEach(e ->
                            System.out.printf("%s    event: %s%n", indent, e.name()));
                }
            });
            System.out.println("  → Exportado a Jaeger (u/Zipkin si se configura B3 propagator)");
        }
    }

    // ── Store global compartido entre servicios ────────────────────────────────
    static final List<Span> STORE = new ArrayList<>();

    // ── Servicios de ejemplo ───────────────────────────────────────────────────

    static class FrontendService {
        private final Tracer tracer = new Tracer(STORE);

        String handleRequest(String path) {
            Span root = tracer.startRootSpan("HTTP GET " + path);
            root.setAttribute("http.method", "GET");
            root.setAttribute("http.url", "https://api.example.com" + path);

            // SpanEvent: un punto de tiempo relevante dentro del span (sin duración)
            root.addEvent("request.received");

            try {
                // Llamar al backend pasando el contexto W3C
                String traceparent = root.traceparent();
                System.out.printf("  [frontend] Propagando W3C traceparent: %s%n", traceparent);

                String backendResult = new BackendService().process(traceparent);

                root.setAttribute("http.status_code", "200");
                root.addEvent("response.sent");
                return backendResult;
            } catch (Exception e) {
                root.setStatus("ERROR");
                root.setAttribute("exception.message", e.getMessage());
                return "error";
            } finally {
                tracer.endSpan(root);
            }
        }
    }

    static class BackendService {
        private final Tracer tracer = new Tracer(STORE);

        String process(String incomingTraceparent) {
            Span span = tracer.startSpanFromTraceparent("processRequest", incomingTraceparent);
            span.setAttribute("db.system", "redis");

            try {
                busyWaitMs(5);
                span.addEvent("cache.hit");

                // Propagar con B3 a un servicio legacy que solo entiende Zipkin
                Map<String, String> b3 = span.b3Headers();
                System.out.println("  [backend]  Propagando B3 headers a servicio legacy:");
                b3.forEach((k, v) -> System.out.printf("             %s: %s%n", k, v));

                new LegacyService().query(b3);
                return "data-from-backend";
            } finally {
                tracer.endSpan(span);
            }
        }
    }

    // Servicio que solo soporta B3 (ej: servicio antiguo integrado con Zipkin)
    static class LegacyService {
        private final Tracer tracer = new Tracer(STORE);

        void query(Map<String, String> b3Headers) {
            Span span = tracer.startSpanFromB3("legacyQuery", b3Headers);
            span.setAttribute("db.system", "postgresql");
            span.setAttribute("db.statement", "SELECT * FROM legacy_table");

            try {
                busyWaitMs(8);
            } finally {
                tracer.endSpan(span);
            }
        }
    }

    static void busyWaitMs(long ms) {
        long end = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < end) { /* spin */ }
    }

    public static void main(String[] args) {
        System.out.println("═".repeat(62));
        System.out.println("  OPENTELEMETRY — Traces + Context Propagation");
        System.out.println("═".repeat(62));

        System.out.println("\n=== Request atravesando 3 servicios ===");
        FrontendService frontend = new FrontendService();
        String result = frontend.handleRequest("/api/products/42");
        System.out.printf("  Resultado: %s%n", result);

        // Exportar spans al "collector"
        OtlpExporter exporter = new OtlpExporter();
        exporter.export(STORE);

        System.out.println("\n=== Formato de cabeceras de propagación ===");
        // Mostrar ambos formatos con un span ficticio
        Span demo = new Span("0af7651916cd43dd8448eb211c80319c",
                             "b7ad6b7169203331", null, "demo");
        System.out.println("  W3C traceparent (OTel default):");
        System.out.println("    traceparent: " + demo.traceparent());
        System.out.println("  B3 multi-header (Zipkin legacy):");
        demo.b3Headers().forEach((k, v) -> System.out.printf("    %s: %s%n", k, v));

        System.out.println("\n── Resumen ─────────────────────────────────────────────────");
        System.out.println("  OTel SDK     → instrumenta tu código (spans, métricas, logs)");
        System.out.println("  OTel Collector → recibe telemetría y la enruta al backend");
        System.out.println("  Jaeger       → backend de tracing (UI + almacenamiento)");
        System.out.println("  Zipkin       → alternativa a Jaeger, usa B3 por defecto");
        System.out.println("  traceparent  → W3C, estándar moderno — usa OTel por defecto");
        System.out.println("  B3 headers   → formato Zipkin, configura B3Propagator en OTel");
        System.out.println("═".repeat(62));
    }
}
