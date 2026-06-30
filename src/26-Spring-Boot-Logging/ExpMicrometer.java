import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

// Simula Micrometer: librería de métricas de Spring Boot.
// En producción: spring-boot-starter-actuator + micrometer-registry-prometheus
// Las métricas se exponen en GET /actuator/metrics/{nombre}
// Spring auto-configura MeterRegistry; aquí lo construimos manualmente.
public class ExpMicrometer {

    // ── Tags (dimensiones) ────────────────────────────────────────────────────

    // En Micrometer: Tags.of("key", "value")
    // Los tags permiten filtrar métricas: ¿cuántos errores tiene el endpoint /orders?
    record Tag(String key, String value) {
        @Override public String toString() { return key + "=" + value; }
    }

    static Tag[] tags(String... kv) {
        Tag[] result = new Tag[kv.length / 2];
        for (int i = 0; i < kv.length; i += 2) {
            result[i / 2] = new Tag(kv[i], kv[i + 1]);
        }
        return result;
    }

    // ── MeterRegistry ─────────────────────────────────────────────────────────

    // En Spring: io.micrometer.core.instrument.MeterRegistry
    // Guarda todos los meters por nombre+tags — la clave es compuesta
    static class MeterRegistry {

        private final Map<String, Counter>   counters = new LinkedHashMap<>();
        private final Map<String, Gauge>     gauges   = new LinkedHashMap<>();
        private final Map<String, Timer>     timers   = new LinkedHashMap<>();

        private static String key(String name, Tag[] tags) {
            if (tags == null || tags.length == 0) return name;
            return name + "{" + Arrays.stream(tags).map(Tag::toString)
                    .reduce((a, b) -> a + "," + b).orElse("") + "}";
        }

        // En Micrometer: Counter.builder(name).tags(...).register(registry)
        Counter counter(String name, Tag... tags) {
            return counters.computeIfAbsent(key(name, tags),
                    k -> new Counter(name, tags));
        }

        // En Micrometer: Gauge.builder(name, obj, fn).tags(...).register(registry)
        // Gauge no almacena un valor propio: lee del objeto cada vez que se consulta
        Gauge gauge(String name, Supplier<Double> valueSupplier, Tag... tags) {
            return gauges.computeIfAbsent(key(name, tags),
                    k -> new Gauge(name, tags, valueSupplier));
        }

        // En Micrometer: Timer.builder(name).tags(...).register(registry)
        Timer timer(String name, Tag... tags) {
            return timers.computeIfAbsent(key(name, tags),
                    k -> new Timer(name, tags));
        }

        // Simula GET /actuator/metrics — expone todas las métricas registradas
        void printActuatorMetrics() {
            System.out.println("\n── GET /actuator/metrics ──────────────────────────────────");
            counters.forEach((k, c) ->
                System.out.printf("  counter  %-45s  count=%.0f%n", k, c.count()));
            gauges.forEach((k, g) ->
                System.out.printf("  gauge    %-45s  value=%.2f%n", k, g.value()));
            timers.forEach((k, t) ->
                System.out.printf("  timer    %-45s  count=%d  mean=%.2fms  max=%.2fms%n",
                        k, t.count(), t.meanMs(), t.maxMs()));
        }
    }

    // ── Counter ───────────────────────────────────────────────────────────────

    // Contador monotónico — solo sube, nunca baja.
    // Uso típico: número de requests, errores, eventos procesados.
    static class Counter {
        private final String name;
        private final Tag[]  tags;
        // AtomicLong para thread-safety sin synchronized
        private final AtomicLong value = new AtomicLong(0);

        Counter(String name, Tag[] tags) {
            this.name = name;
            this.tags = tags;
        }

        // En Micrometer: counter.increment()
        void increment() { value.incrementAndGet(); }

        // En Micrometer: counter.increment(amount)
        void increment(double amount) { value.addAndGet((long) amount); }

        double count() { return value.get(); }
    }

    // ── Gauge ─────────────────────────────────────────────────────────────────

    // Valor instantáneo que puede subir o bajar.
    // Uso típico: conexiones activas, tamaño de cola, uso de memoria.
    // No almacena el valor — lo lee del objeto de negocio en cada scrape.
    // Esto evita que la métrica quede desincronizada del estado real.
    static class Gauge {
        private final String name;
        private final Tag[]  tags;
        private final Supplier<Double> valueSupplier;

        Gauge(String name, Tag[] tags, Supplier<Double> valueSupplier) {
            this.name = name;
            this.tags = tags;
            this.valueSupplier = valueSupplier;
        }

        // Se invoca en cada scrape de Prometheus
        double value() { return valueSupplier.get(); }
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    // Mide duración de operaciones. Registra: count, sum, max (ventana deslizante).
    // En producción: Timer.Sample sample = Timer.start(registry); ... sample.stop(timer)
    static class Timer {
        private final String name;
        private final Tag[]  tags;
        private final List<Long> durationsNs = new ArrayList<>();

        Timer(String name, Tag[] tags) {
            this.name = name;
            this.tags = tags;
        }

        // Ejecuta la tarea y registra su duración automáticamente
        // En Micrometer: timer.record(() -> { ... })
        void record(Runnable task) {
            long start = System.nanoTime();
            try {
                task.run();
            } finally {
                durationsNs.add(System.nanoTime() - start);
            }
        }

        int    count()  { return durationsNs.size(); }
        double meanMs() {
            return durationsNs.isEmpty() ? 0
                : durationsNs.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;
        }
        double maxMs()  {
            return durationsNs.stream().mapToLong(Long::longValue).max().orElse(0) / 1_000_000.0;
        }
    }

    // ── Servicios simulados que usan métricas ─────────────────────────────────

    static class OrderService {
        private final Counter   requestCounter;
        private final Counter   errorCounter;
        private final Timer     requestTimer;
        // Gauge lee directamente este Set — no necesita llamada explícita para actualizar
        private final Set<String> activeOrders = ConcurrentHashMap.newKeySet();

        OrderService(MeterRegistry registry) {
            // Tags: diferenciar métricas por endpoint/método sin crear un counter por cada uno
            this.requestCounter = registry.counter("http.requests",
                    tags("endpoint", "/orders", "method", "POST"));
            this.errorCounter   = registry.counter("http.errors",
                    tags("endpoint", "/orders", "status", "500"));
            this.requestTimer   = registry.timer("http.request.duration",
                    tags("endpoint", "/orders"));

            // El gauge apunta al size() del Set — Micrometer llamará a valueSupplier en cada scrape
            registry.gauge("orders.active", () -> (double) activeOrders.size(),
                    tags("service", "orders"));
        }

        void processOrder(String orderId, boolean shouldFail) {
            requestCounter.increment();
            requestTimer.record(() -> {
                activeOrders.add(orderId);
                try {
                    simulateWork(orderId.hashCode() % 5 + 3); // 3-7ms simulados
                    if (shouldFail) {
                        errorCounter.increment();
                        throw new RuntimeException("Error procesando " + orderId);
                    }
                    System.out.printf("  [OrderService] Orden %s procesada%n", orderId);
                } catch (RuntimeException e) {
                    System.out.printf("  [OrderService] ERROR en %s: %s%n", orderId, e.getMessage());
                } finally {
                    activeOrders.remove(orderId);
                }
            });
        }

        private void simulateWork(int ms) {
            long end = System.currentTimeMillis() + ms;
            while (System.currentTimeMillis() < end) { /* spin */ }
        }
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        MeterRegistry registry = new MeterRegistry();
        OrderService  service  = new OrderService(registry);

        System.out.println("=== Procesando órdenes ===");
        service.processOrder("ORD-001", false);
        service.processOrder("ORD-002", false);
        service.processOrder("ORD-003", true);   // falla → errorCounter sube
        service.processOrder("ORD-004", false);

        System.out.println("\n=== Counter manual ===");
        // Counter independiente para eventos de login
        Counter loginCounter = registry.counter("auth.logins",
                tags("method", "oauth2"));
        loginCounter.increment();
        loginCounter.increment();
        loginCounter.increment(5); // bulk increment
        System.out.printf("  Logins registrados: %.0f%n", loginCounter.count());

        System.out.println("\n=== Gauge de tamaño de cola ===");
        // Gauge que apunta a una variable mutable
        final int[] queueSize = {0};
        registry.gauge("queue.size", () -> (double) queueSize[0], tags("queue", "payments"));
        queueSize[0] = 42;
        System.out.println("  Cola de pagos: " + queueSize[0] + " mensajes (el gauge leerá esto)");

        // Simula un scrape de Prometheus leyendo /actuator/metrics
        registry.printActuatorMetrics();

        System.out.println("\n── Resumen conceptual ──────────────────────────────────────");
        System.out.println("  Counter  → solo sube (requests, errores, eventos)");
        System.out.println("  Gauge    → sube y baja (memoria, conexiones, cola size)");
        System.out.println("  Timer    → duración + histograma (latencia por percentil)");
        System.out.println("  Tags     → dimensiones para filtrar en Grafana/Prometheus");
        System.out.println("  Backends → Prometheus, Datadog, CloudWatch, Graphite, InfluxDB");
        System.out.println("  Endpoint → /actuator/metrics/{nombre}?tag=key:value");
    }
}
